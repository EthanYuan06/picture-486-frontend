package com.yuluo.picture486backend.ws;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端
 * 监听地址：/ws/picture/review/{userId}
 */
@Component
@ServerEndpoint("/ws/picture/review/{userId}")
@Slf4j
public class WebSocketServer {

    /**
     * 存放在线用户的 Session
     * Key: userId
     * Value: Session
     */
    private static final Map<Long, Session> ONLINE_USERS = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        if (userId != null) {
            ONLINE_USERS.put(userId, session);
            log.info("WebSocket 连接建立: userId={}, sessionId={}", userId, session.getId());
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        if (userId != null) {
            ONLINE_USERS.remove(userId);
            log.info("WebSocket 连接关闭: userId={}, sessionId={}", userId, session.getId());
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        if ("PING".equalsIgnoreCase(message)) {
            try {
                session.getBasicRemote().sendText("PONG");
            } catch (IOException e) {
                log.error("发送 PONG 失败", e);
            }
        }
    }

    /**
     * 发送消息给指定用户
     *
     * @param userId  用户ID
     * @param message 消息内容
     */
    public static void sendMessage(Long userId, String message) {
        Session session = ONLINE_USERS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.info("发送消息给用户 {}: {}", userId, message);
            } catch (IOException e) {
                log.error("发送消息给用户 {} 失败", userId, e);
            }
        } else {
            // 用户不在线
            log.warn("用户 {} 不在线，消息未发送", userId);
        }
    }
}
