package com.netflix.watchspaces.service;

import com.netflix.watchspaces.domain.dto.response.ChatMessageResponse;
import com.netflix.watchspaces.domain.entity.ChatMessage;
import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.domain.entity.WatchSpace;
import com.netflix.watchspaces.domain.entity.WatchSpaceParticipant;
import com.netflix.watchspaces.domain.enums.MessageType;
import com.netflix.watchspaces.domain.enums.RoleType;
import com.netflix.watchspaces.repository.ChatMessageRepository;
import com.netflix.watchspaces.repository.UserRepository;
import com.netflix.watchspaces.repository.WatchSpaceParticipantRepository;
import com.netflix.watchspaces.repository.WatchSpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private WatchSpaceRepository watchSpaceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WatchSpaceParticipantRepository participantRepository;

    private ChatService chatService;

    @BeforeEach
    public void setUp() {
        chatService = new ChatService(chatMessageRepository, watchSpaceRepository, userRepository, participantRepository);
    }

    @Test
    public void testSaveMessageSuccess() {
        String spaceId = "ws_test_chat";
        Long userId = 10L;

        WatchSpace space = WatchSpace.builder().id(spaceId).build();
        User user = User.builder().id(userId).displayName("Elena").role(RoleType.HOST).build();

        when(participantRepository.findActiveByWatchSpaceIdAndUserId(spaceId, userId))
                .thenReturn(Optional.of(WatchSpaceParticipant.builder().isMuted(false).build()));
        when(watchSpaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage input = invocation.getArgument(0);
            return new ChatMessage(101L, input.getWatchSpace(), input.getUser(), input.getSenderName(),
                    input.getMsgType(), input.getBody(), input.getTsSeconds(), LocalDateTime.now());
        });

        StepVerifier.create(chatService.saveMessage(spaceId, userId, "Elena", "Hello <b>everyone</b>!", 12.5, MessageType.USER))
                .assertNext(res -> {
                    assertEquals(101L, res.getId());
                    assertEquals("Hello everyone!", res.getBody());
                    assertEquals("Elena", res.getSenderName());
                    assertEquals(12.5, res.getTsSeconds());
                })
                .verifyComplete();
    }

    @Test
    public void testMutedUserCannotSendMessage() {
        String spaceId = "ws_test_chat";
        Long userId = 20L;

        when(participantRepository.findActiveByWatchSpaceIdAndUserId(spaceId, userId))
                .thenReturn(Optional.of(WatchSpaceParticipant.builder().isMuted(true).build()));

        StepVerifier.create(chatService.saveMessage(spaceId, userId, "Troll", "Spam", 0.0, MessageType.USER))
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        throwable.getMessage().contains("You are muted"))
                .verify();

        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    public void testGetRecentMessagesReturnsChronologicalOrder() {
        String spaceId = "ws_test_chat";
        ChatMessage msg1 = ChatMessage.builder().id(1L).body("First").createdAt(LocalDateTime.now().minusMinutes(2)).build();
        ChatMessage msg2 = ChatMessage.builder().id(2L).body("Second").createdAt(LocalDateTime.now().minusMinutes(1)).build();

        // findRecentByWatchSpaceId returns DESC
        when(chatMessageRepository.findRecentByWatchSpaceId(eq(spaceId), any(PageRequest.class)))
                .thenReturn(List.of(msg2, msg1));

        StepVerifier.create(chatService.getRecentMessages(spaceId, 50))
                .assertNext(res -> assertEquals("First", res.getBody()))
                .assertNext(res -> assertEquals("Second", res.getBody()))
                .verifyComplete();
    }
}
