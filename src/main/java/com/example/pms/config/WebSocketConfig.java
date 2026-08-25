package com.example.pms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(
            WebSocketAuthInterceptor webSocketAuthInterceptor
    ) {
        this.webSocketAuthInterceptor =
                webSocketAuthInterceptor;
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {

        registration.interceptors(
                webSocketAuthInterceptor
        );
    }

    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry config
    ) {

        config.enableSimpleBroker(
                "/queue",
                "/topic"
        );

        config.setApplicationDestinationPrefixes(
                "/app"
        );

        config.setUserDestinationPrefix(
                "/user"
        );
    }

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {

        registry
                .addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:5173"
                );
    }

}


//private final WebSocketAuthInterceptor webSocketAuthInterceptor;
//
//public WebSocketConfig(
//        WebSocketAuthInterceptor webSocketAuthInterceptor
//) {
//    this.webSocketAuthInterceptor =
//            webSocketAuthInterceptor;
//}
//
//@Override
//public void configureClientInboundChannel(
//        ChannelRegistration registration
//) {
//    registration.interceptors(
//            webSocketAuthInterceptor
//    );
//}
//
//@Override
//public void configureMessageBroker(
//        MessageBrokerRegistry config
//) {
//
//    config.enableSimpleBroker("/queue", "/topic");
//
//    config.setApplicationDestinationPrefixes("/app");
//
//    config.setUserDestinationPrefix("/user");
//}
//
//@Override
//public void registerStompEndpoints(
//        StompEndpointRegistry registry
//) {
//
//    registry
//            .addEndpoint("/ws")
//            .setAllowedOrigins("http://localhost:5173");
//}
