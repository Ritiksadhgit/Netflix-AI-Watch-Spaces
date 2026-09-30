package com.netflix.watchspaces.controller;

import com.netflix.watchspaces.domain.dto.response.ChatMessageResponse;
import com.netflix.watchspaces.domain.enums.MessageType;
import com.netflix.watchspaces.security.UserPrincipal;
import com.netflix.watchspaces.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/watch-spaces/{watchSpaceId}/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public Flux<ChatMessageResponse> getRecentMessages(
            @PathVariable String watchSpaceId,
            @RequestParam(defaultValue = "50") int limit) {
        return chatService.getRecentMessages(watchSpaceId, limit);
    }

    @PostMapping
    public Mono<ChatMessageResponse> postMessage(
            @PathVariable String watchSpaceId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
        }
        Long userId = principal.getId();
        String displayName = principal.getDisplayName() != null ? principal.getDisplayName() : "Participant";
        String messageBody = (String) body.get("body");
        Number tsSecondsNum = (Number) body.get("tsSeconds");
        Double tsSeconds = tsSecondsNum != null ? tsSecondsNum.doubleValue() : 0.0;

        return chatService.saveMessage(watchSpaceId, userId, displayName, messageBody, tsSeconds, MessageType.USER);
    }
}
