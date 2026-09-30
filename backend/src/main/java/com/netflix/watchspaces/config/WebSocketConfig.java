package com.netflix.watchspaces.config;

import com.netflix.watchspaces.websocket.WatchSpaceWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

@Configuration
public class WebSocketConfig {

    private final WatchSpaceWebSocketHandler webSocketHandler;

    public WebSocketConfig(WatchSpaceWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Map.of("/ws/watch-space", webSocketHandler));
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
