package com.netflix.watchspaces.service;

import com.netflix.watchspaces.domain.dto.request.CreateWatchSpaceRequest;
import com.netflix.watchspaces.domain.dto.response.WatchSpaceResponse;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.entity.WatchSpaceParticipant;
import com.netflix.watchspaces.domain.enums.PlaybackState;
import com.netflix.watchspaces.domain.enums.WatchSpaceStatus;
import com.netflix.watchspaces.repository.TitleRepository;
import com.netflix.watchspaces.repository.UserRepository;
import com.netflix.watchspaces.repository.WatchSpaceParticipantRepository;
import com.netflix.watchspaces.repository.WatchSpaceRepository;
import com.netflix.watchspaces.websocket.WatchSpaceRoomManager;
import com.netflix.watchspaces.websocket.WatchSpaceRoomSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WatchSpaceService {

    private final WatchSpaceRepository watchSpaceRepository;
    private final WatchSpaceParticipantRepository participantRepository;
    private final TitleRepository titleRepository;
    private final UserRepository userRepository;
    private final WatchSpaceRoomManager roomManager;

    public WatchSpaceService(
            WatchSpaceRepository watchSpaceRepository,
            WatchSpaceParticipantRepository participantRepository,
            TitleRepository titleRepository,
            UserRepository userRepository,
            WatchSpaceRoomManager roomManager) {
        this.watchSpaceRepository = watchSpaceRepository;
        this.participantRepository = participantRepository;
        this.titleRepository = titleRepository;
        this.userRepository = userRepository;
        this.roomManager = roomManager;
    }

    @Transactional
    public Mono<WatchSpaceResponse> createWatchSpace(CreateWatchSpaceRequest request, Long hostUserId) {
        return Mono.fromCallable(() -> {
            User host = userRepository.findById(hostUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host user not found"));

            Title title = titleRepository.findById(request.getTitleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Title not found with ID: " + request.getTitleId()));

            String spaceId = "ws_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            String inviteCode = generateInviteCode();

            WatchSpace space = WatchSpace.builder()
                    .id(spaceId)
                    .title(title)
                    .hostUser(host)
                    .name(request.getName().trim())
                    .status(WatchSpaceStatus.LIVE)
                    .privacy(request.getPrivacy())
                    .inviteCode(inviteCode)
                    .maxParticipants(request.getMaxParticipants())
                    .isLocked(false)
                    .aiVerbosity(request.getAiVerbosity())
                    .votingEnabled(request.isVotingEnabled())
                    .playbackState(PlaybackState.PAUSED)
                    .playbackPositionSeconds(0.0)
                    .playbackUpdatedAt(LocalDateTime.now())
                    .build();

            WatchSpace savedSpace = watchSpaceRepository.save(space);

            // Add host as first participant
            WatchSpaceParticipant hostParticipant = WatchSpaceParticipant.builder()
                    .watchSpace(savedSpace)
                    .user(host)
                    .roleInRoom("HOST")
                    .isMuted(false)
                    .build();
            participantRepository.save(hostParticipant);

            return WatchSpaceResponse.fromEntity(savedSpace, title, host, 1, hostUserId);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<WatchSpaceResponse> getWatchSpaceById(String id, Long currentUserId) {
        return Mono.fromCallable(() -> {
            WatchSpace space = watchSpaceRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch Space not found with ID: " + id));

            int activeCount = participantRepository.findActiveByWatchSpaceId(id).size();
            return WatchSpaceResponse.fromEntity(space, Math.max(activeCount, 1), currentUserId);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<WatchSpaceResponse> getActivePublicSpaces(Long currentUserId) {
        return Mono.fromCallable(() -> {
            List<WatchSpace> spaces = watchSpaceRepository.findByStatus(WatchSpaceStatus.LIVE);
            return spaces.stream()
                    .filter(s -> s.getPrivacy() != null && s.getPrivacy().name().equals("PUBLIC"))
                    .map(s -> {
                        int count = participantRepository.findActiveByWatchSpaceId(s.getId()).size();
                        return WatchSpaceResponse.fromEntity(s, count, currentUserId);
                    })
                    .collect(Collectors.toList());
        }).subscribeOn(Schedulers.boundedElastic()).flatMapMany(Flux::fromIterable);
    }

    @Transactional
    public Mono<WatchSpaceResponse> joinByInviteCode(String inviteCode, Long currentUserId) {
        return Mono.fromCallable(() -> {
            String code = inviteCode.trim().toUpperCase();
            WatchSpace space = watchSpaceRepository.findByInviteCode(code)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No Watch Space found with invite code: " + code));

            if (space.getStatus() == WatchSpaceStatus.ENDED) {
                throw new ResponseStatusException(HttpStatus.GONE, "This Watch Space has already ended");
            }

            if (space.isLocked()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This Watch Space is locked by the host");
            }

            List<WatchSpaceParticipant> activeParticipants = participantRepository.findActiveByWatchSpaceId(space.getId());
            if (activeParticipants.size() >= space.getMaxParticipants()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Watch Space has reached maximum participant capacity");
            }

            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Check if already active participant
            boolean alreadyJoined = activeParticipants.stream().anyMatch(p -> p.getUser().getId().equals(currentUserId));
            if (!alreadyJoined) {
                WatchSpaceParticipant participant = WatchSpaceParticipant.builder()
                        .watchSpace(space)
                        .user(user)
                        .roleInRoom(user.getId().equals(space.getHostUser().getId()) ? "HOST" : "PARTICIPANT")
                        .isMuted(false)
                        .build();
                participantRepository.save(participant);
            }

            int totalCount = participantRepository.findActiveByWatchSpaceId(space.getId()).size();
            return WatchSpaceResponse.fromEntity(space, totalCount, currentUserId);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Map<String, Object>> pinFact(String watchSpaceId, Map<String, Object> fact, Long userId, boolean isAdmin) {
        return Mono.fromCallable(() -> {
            WatchSpace space = watchSpaceRepository.findById(watchSpaceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch Space not found with ID: " + watchSpaceId));

            boolean isHost = (space.getHostUser() != null && space.getHostUser().getId().equals(userId)) || isAdmin;
            if (!isHost) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the host can pin facts");
            }

            WatchSpaceRoomSession room = roomManager.getOrCreateRoom(watchSpaceId);
            room.pinFact(fact, userId, isAdmin);
            return fact != null ? fact : Collections.<String, Object>emptyMap();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> unpinFact(String watchSpaceId, Long userId, boolean isAdmin) {
        return Mono.fromRunnable(() -> {
            WatchSpace space = watchSpaceRepository.findById(watchSpaceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch Space not found with ID: " + watchSpaceId));

            boolean isHost = (space.getHostUser() != null && space.getHostUser().getId().equals(userId)) || isAdmin;
            if (!isHost) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the host can unpin facts");
            }

            WatchSpaceRoomSession room = roomManager.getOrCreateRoom(watchSpaceId);
            room.unpinFact(userId, isAdmin);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Map<String, Object>> getPinnedFact(String watchSpaceId) {
        return Mono.fromCallable(() -> {
            WatchSpaceRoomSession room = roomManager.getOrCreateRoom(watchSpaceId);
            Map<String, Object> fact = room.getPinnedFact();
            return fact != null ? fact : Collections.<String, Object>emptyMap();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Map<String, Object>> approveVariant(String watchSpaceId, Map<String, Object> variant, Long userId, boolean isAdmin) {
        return Mono.fromCallable(() -> {
            WatchSpace space = watchSpaceRepository.findById(watchSpaceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch Space not found with ID: " + watchSpaceId));

            boolean isHost = (space.getHostUser() != null && space.getHostUser().getId().equals(userId)) || isAdmin;
            if (!isHost) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the host can approve variants");
            }

            WatchSpaceRoomSession room = roomManager.getOrCreateRoom(watchSpaceId);
            room.approveVariant(variant, userId, isAdmin);
            return variant != null ? variant : Collections.<String, Object>emptyMap();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Map<String, Object>> getApprovedVariant(String watchSpaceId) {
        return Mono.fromCallable(() -> {
            WatchSpaceRoomSession room = roomManager.getOrCreateRoom(watchSpaceId);
            Map<String, Object> variant = room.getApprovedVariant();
            return variant != null ? variant : Collections.<String, Object>emptyMap();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<Map<String, Object>>> getEligibleVariants(String watchSpaceId) {
        return Mono.fromCallable(() -> {
            WatchSpace space = watchSpaceRepository.findById(watchSpaceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch Space not found with ID: " + watchSpaceId));

            Long titleId = space.getTitle() != null ? space.getTitle().getId() : 1L;
            return getEligibleVariantsForTitle(titleId);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public List<Map<String, Object>> getEligibleVariantsForTitle(Long titleId) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (titleId != null && titleId == 2L) { // Sintel
            list.add(Map.of("id", "en", "label", "English [CC]", "lang", "en", "type", "SUBTITLE", "isDefault", true, "description", "Original audio with English closed captions"));
            list.add(Map.of("id", "es", "label", "Español", "lang", "es", "type", "SUBTITLE", "isDefault", false, "description", "Spanish localized subtitle asset"));
        } else if (titleId != null && titleId == 3L) { // Big Buck Bunny
            list.add(Map.of("id", "en", "label", "English [CC]", "lang", "en", "type", "SUBTITLE", "isDefault", true, "description", "Original audio with English closed captions"));
        } else { // Tears of Steel / Title 1 (Default)
            list.add(Map.of("id", "en", "label", "English [CC]", "lang", "en", "type", "SUBTITLE", "isDefault", true, "description", "Original audio with English closed captions"));
            list.add(Map.of("id", "es", "label", "Español", "lang", "es", "type", "SUBTITLE", "isDefault", false, "description", "Spanish localized subtitle asset"));
            list.add(Map.of("id", "fr", "label", "Français", "lang", "fr", "type", "SUBTITLE", "isDefault", false, "description", "French localized subtitle asset"));
        }
        return list;
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        sb.append("-");
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }
}
