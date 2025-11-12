package com.travelplanner.controller;

import com.travelplanner.service.VoiceRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/voice")
@CrossOrigin
public class VoiceController {
    
    @Autowired
    private VoiceRecognitionService voiceRecognitionService;
    
    /**
     * 上传语音文件并识别
     * @param audioFile 语音文件
     * @return 识别结果文本
     */
    @PostMapping("/recognize")
    public String recognizeVoice(@RequestParam("audio") MultipartFile audioFile) throws IOException {
        byte[] audioData = audioFile.getBytes();
        String recognizedText = voiceRecognitionService.recognizeSpeech(audioData);
        return recognizedText;
    }
    
    /**
     * 开始实时语音识别
     * @return 操作结果
     */
    @PostMapping("/realtime/start")
    public Map<String, Object> startRealTimeRecognition() {
        Map<String, Object> result = new HashMap<>();
        boolean success = voiceRecognitionService.startRealTimeRecognition();
        result.put("success", success);
        result.put("message", success ? "开始录音" : "无法开始录音");
        return result;
    }
    
    /**
     * 停止实时语音识别
     * @return 操作结果
     */
    @PostMapping("/realtime/stop")
    public Map<String, Object> stopRealTimeRecognition() {
        Map<String, Object> result = new HashMap<>();
        voiceRecognitionService.stopRealTimeRecognition();
        result.put("success", true);
        result.put("message", "录音已停止");
        return result;
    }
    
    /**
     * 获取实时识别结果
     * @return 识别结果
     */
    @GetMapping("/realtime/result")
    public Map<String, Object> getRealTimeResult() {
        Map<String, Object> result = new HashMap<>();
        String text = voiceRecognitionService.getRealTimeResult();
        result.put("success", true);
        result.put("text", text);
        return result;
    }
    
    /**
     * 实时语音识别（WebSocket方式）
     * @return WebSocket连接信息
     */
    @GetMapping("/realtime")
    public Map<String, String> getRealtimeRecognitionInfo(HttpServletRequest request) {
        // 返回WebSocket连接地址和相关信息
        Map<String, String> info = new HashMap<>();
        String wsUrl = "ws://" + request.getServerName() + ":" + request.getServerPort() + 
                      request.getContextPath() + "/ws/voice";
        info.put("websocketUrl", wsUrl);
        info.put("description", "连接到此WebSocket端点以进行实时语音识别");
        return info;
    }
}