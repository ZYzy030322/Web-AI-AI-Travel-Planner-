package com.travelplanner.websocket;

import com.google.gson.Gson;
import com.travelplanner.config.ApiKeyConfig;
import com.travelplanner.service.XfyunWebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VoiceWebSocketHandler extends AbstractWebSocketHandler {
    private static final Gson gson = new Gson();
    
    // 存储所有活跃的WebSocket会话
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, ByteArrayOutputStream> audioBuffers = new ConcurrentHashMap<>();
    
    @Autowired
    private ApiKeyConfig apiKeyConfig;
    
    @Autowired
    private XfyunWebSocketService xfyunWebSocketService;
    
    /**
     * 处理WebSocket连接建立后事件
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        audioBuffers.put(session.getId(), new ByteArrayOutputStream());
        System.out.println("WebSocket连接已建立，会话ID: " + session.getId());
        
        // 发送连接成功消息
        Map<String, Object> message = new HashMap<>();
        message.put("type", "connected");
        message.put("message", "WebSocket连接已建立");
        session.sendMessage(new TextMessage(gson.toJson(message)));
    }
    
    /**
     * 处理接收到的二进制消息（音频数据）
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        // 将接收到的音频数据添加到缓冲区
        ByteArrayOutputStream audioBuffer = audioBuffers.get(session.getId());
        if (audioBuffer != null) {
            audioBuffer.write(message.getPayload().array());
        }
    }
    
    /**
     * 处理接收到的文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("收到文本消息: " + message.getPayload());
        
        // 如果收到开始识别的消息
        if ("startRecognition".equals(message.getPayload())) {
            // 开始处理音频数据
            processAudioData(session);
        } else {
            // 其他控制消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "echo");
            response.put("message", "收到消息: " + message.getPayload());
            session.sendMessage(new TextMessage(gson.toJson(response)));
        }
    }
    
    /**
     * 处理音频数据并进行语音识别
     * @param session WebSocket会话
     */
    private void processAudioData(WebSocketSession session) {
        ByteArrayOutputStream audioBuffer = audioBuffers.get(session.getId());
        if (audioBuffer == null || audioBuffer.size() == 0) {
            sendErrorMessage(session, "没有音频数据");
            return;
        }
        
        // 获取音频数据
        byte[] audioData = audioBuffer.toByteArray();
        audioBuffer.reset();
        
        // 调用语音识别服务
        xfyunWebSocketService.recognizeSpeech(audioData, new XfyunWebSocketService.ResultCallback() {
            @Override
            public void onPartialResult(String partialResult) {
                // 发送部分识别结果
                try {
                    Map<String, Object> response = new HashMap<>();
                    response.put("type", "partialResult");
                    response.put("data", partialResult);
                    session.sendMessage(new TextMessage(gson.toJson(response)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onFinalResult(String finalResult) {
                // 发送最终识别结果
                try {
                    Map<String, Object> response = new HashMap<>();
                    response.put("type", "finalResult");
                    response.put("data", finalResult);
                    session.sendMessage(new TextMessage(gson.toJson(response)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onError(int errorCode, String errorMessage) {
                sendErrorMessage(session, "语音识别错误: " + errorMessage);
            }
        });
    }
    
    /**
     * 发送错误消息
     * @param session WebSocket会话
     * @param errorMessage 错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("type", "error");
            response.put("message", errorMessage);
            session.sendMessage(new TextMessage(gson.toJson(response)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 处理WebSocket连接关闭事件
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        audioBuffers.remove(session.getId());
        System.out.println("WebSocket连接已关闭，会话ID: " + session.getId() + "，状态: " + status);
    }
    
    /**
     * 处理WebSocket传输错误事件
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket传输错误，会话ID: " + session.getId() + "，错误: " + exception.getMessage());
        sessions.remove(session.getId());
        audioBuffers.remove(session.getId());
    }
    
    /**
     * 发送消息给指定会话
     */
    public void sendMessageToSession(String sessionId, Map<String, Object> message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(gson.toJson(message)));
            } catch (Exception e) {
                System.err.println("发送消息失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
}