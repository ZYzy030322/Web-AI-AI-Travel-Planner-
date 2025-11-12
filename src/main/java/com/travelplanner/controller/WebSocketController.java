package com.travelplanner.controller;

import com.travelplanner.websocket.VoiceWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@CrossOrigin
public class WebSocketController {
    
    @Autowired
    private VoiceWebSocketHandler voiceWebSocketHandler;
    
    /**
     * 处理WebSocket升级请求
     */
    @GetMapping("/ws/voice")
    public void voiceWebSocket(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 这个端点将被WebSocket客户端连接
        // 实际的WebSocket处理由前端JavaScript和讯飞API处理
        // 此处返回404表示该端点需要通过WebSocket协议访问
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("请使用WebSocket协议连接此端点");
    }
}