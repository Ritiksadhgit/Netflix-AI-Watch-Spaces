package com.netflix.watchspaces.service;

import com.netflix.watchspaces.domain.dto.response.ChatMessageResponse;
import com.netflix.watchspaces.domain.entity.ChatMessage;
import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.enums.MessageType;
import com.netflix.watchspaces.repository.ChatMessageRepository;
import com.netflix.watchspaces.repository.UserRepository;
import com.netflix.watchspaces.repository.WatchSpaceParticipantRepository;
import com.netflix.watchspaces.repository.WatchSpaceRepository;
import com.netflix.watchspaces.util.InputSanitizer;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final WatchSpaceRepository watchSpaceRepository;
    private final UserRepository userRepository;
    private final WatchSpaceParticipantRepository participantRepository;

    public ChatService(
            ChatMessageRepository chatMessageRepository,
            WatchSpaceRepository watchSpaceRepository,
            UserRepository userRepository,
            WatchSpaceParticipantRepository participantRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.watchSpaceRepository = watchSpaceRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
    }

    public Flux<ChatMessageResponse> getRecentMessages(String watchSpaceId, int limit) {
        return Mono.fromCallable(() -> {
            int maxLimit = Math.min(Math.max(limit, 1), 100);
            List<ChatMessage> list = chatMessageRepository.findRecentByWatchSpaceId(
                    watchSpaceId, PageRequest.of(0, maxLimit)
            );
            // Reverse so earliest is first
            List<ChatMessage> chronological = new ArrayList<>(list);
            Collections.reverse(chronological);
            return chronological.stream()
                    .map(ChatMessageResponse::fromEntity)
                    .collect(Collectors.toList());
        }).subscribeOn(Schedulers.boundedElastic()).flatMapMany(Flux::fromIterable);
    }

    @Transactional
    public Mono<ChatMessageResponse> saveMessage(
            String watchSpaceId,
            Long userId,
            String senderName,
            String body,
            Double tsSeconds,
            MessageType msgType) {
        return Mono.fromCallable(() -> {
            if (userId != null) {
                participantRepository.findActiveByWatchSpaceIdAndUserId(watchSpaceId, userId)
                        .ifPresent(p -> {
                            if (p.isMuted()) {
                                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are muted in this Watch Space");
                            }
                        });
            }

            String sanitized = InputSanitizer.sanitizeChat(body);
            if (sanitized.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message body cannot be empty");
            }

            WatchSpace watchSpace = watchSpaceRepository.findById(watchSpaceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch space not found"));

            User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

            ChatMessage chatMessage = ChatMessage.builder()
                    .watchSpace(watchSpace)
                    .user(user)
                    .senderName(senderName != null ? senderName : "Anonymous")
                    .msgType(msgType != null ? msgType : MessageType.USER)
                    .body(sanitized)
                    .tsSeconds(tsSeconds != null ? tsSeconds : 0.0)
                    .build();

            ChatMessage saved = chatMessageRepository.save(chatMessage);
            return ChatMessageResponse.fromEntity(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
