package com.travelplanner.service;

import com.travelplanner.config.ApiKeyConfig;
import com.travelplanner.util.MicrophoneRecorderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sound.sampled.LineUnavailableException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class VoiceRecognitionService {
    
    @Autowired
    private ApiKeyConfig apiKeyConfig;
    
    @Autowired
    private XfyunWebSocketService xfyunWebSocketService;
    
    private MicrophoneRecorderUtil microphoneRecorder;
    private PipedInputStream audioInputStream;
    private PipedOutputStream audioOutputStream;
    private volatile boolean isRecording = false;
    private volatile StringBuilder realTimeResult = new StringBuilder();
    private Thread audioSendThread;
    
    /**
     * 使用讯飞语音识别API将语音转换为文本
     * @param audioData 语音数据
     * @return 识别后的文本
     */
    public String recognizeSpeech(byte[] audioData) {
        // 获取讯飞API配置
        String appId = apiKeyConfig.getXfyunAppId();
        String apiKey = apiKeyConfig.getXfyunApiKey();
        String apiSecret = apiKeyConfig.getXfyunApiSecret();
        
        // 检查配置是否完整
        if (appId == null || appId.isEmpty() || 
            apiKey == null || apiKey.isEmpty() || 
            apiSecret == null || apiSecret.isEmpty()) {
            return "语音识别服务未正确配置，请检查API密钥设置";
        }
        
        // 使用CompletableFuture来等待异步识别结果
        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        
        try {
            xfyunWebSocketService.recognizeSpeech(audioData, new XfyunWebSocketService.ResultCallback() {
                private final StringBuilder resultBuilder = new StringBuilder();
                
                @Override
                public void onPartialResult(String partialResult) {
                    // 累积部分结果
                    resultBuilder.append(partialResult);
                }
                
                @Override
                public void onFinalResult(String finalResult) {
                    // 返回最终结果
                    resultFuture.complete(finalResult);
                }
                
                @Override
                public void onError(int errorCode, String errorMessage) {
                    resultFuture.complete("语音识别失败: 错误码=" + errorCode + ", 错误信息=" + errorMessage);
                }
            });
            
            // 等待识别结果，最多等待30秒
            return resultFuture.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return "语音识别失败: " + e.getMessage();
        }
    }
    
    /**
     * 开始实时语音识别
     * @return 是否成功启动
     */
    public boolean startRealTimeRecognition() {
        if (isRecording) {
            return true; // 已经在录音中
        }
        
        try {
            // 清理之前的资源
            cleanupResources();
            
            // 初始化录音工具
            microphoneRecorder = new MicrophoneRecorderUtil();
            
            // 创建带缓冲的音频管道流
            audioInputStream = new PipedInputStream();
            audioOutputStream = new PipedOutputStream(audioInputStream);
            
            // 初始化结果构建器
            realTimeResult = new StringBuilder();
            
            // 启动实时识别服务
            xfyunWebSocketService.startRealTimeRecognition(new XfyunWebSocketService.ResultCallback() {
                @Override
                public void onPartialResult(String partialResult) {
                    System.out.println("收到部分识别结果: " + partialResult);
                    realTimeResult.append(partialResult);
                }
                
                @Override
                public void onFinalResult(String finalResult) {
                    System.out.println("收到最终识别结果: " + finalResult);
                    realTimeResult = new StringBuilder(finalResult);
                }
                
                @Override
                public void onError(int errorCode, String errorMessage) {
                    System.err.println("实时语音识别错误: " + errorCode + ", " + errorMessage);
                }
            });
            
            // 开始录音
            microphoneRecorder.startRecording(audioOutputStream);
            
            // 在新线程中持续发送音频数据
            audioSendThread = new Thread(this::sendAudioDataContinuously);
            audioSendThread.setDaemon(true);
            audioSendThread.start();
            
            isRecording = true;
            System.out.println("开始实时语音识别");
            return true;
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 停止实时语音识别
     */
    public void stopRealTimeRecognition() {
        if (!isRecording) {
            return;
        }
        
        try {
            System.out.println("停止实时语音识别");
            // 停止录音
            if (microphoneRecorder != null) {
                microphoneRecorder.stopRecording();
            }
            
            isRecording = false;
            
            // 等待音频发送线程结束
            if (audioSendThread != null) {
                audioSendThread.join(1000); // 等待最多1秒
            }
            
            // 发送结束帧
            xfyunWebSocketService.sendFinishFrame();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 清理资源
            cleanupResources();
        }
    }
    
    /**
     * 获取实时识别结果
     * @return 当前识别结果
     */
    public String getRealTimeResult() {
        String result = realTimeResult.toString();
        System.out.println("获取实时识别结果: " + result);
        return result;
    }
    
    /**
     * 持续发送音频数据
     */
    private void sendAudioDataContinuously() {
        // 等待WebSocket连接建立完成
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        byte[] buffer = new byte[1280];
        try {
            while (isRecording && audioInputStream != null) {
                int bytesRead = audioInputStream.read(buffer);
                if (bytesRead > 0) {
                    // 发送音频数据
                    xfyunWebSocketService.sendRealTimeAudioData(
                        java.util.Arrays.copyOf(buffer, bytesRead)
                    );
                }
            }
        } catch (Exception e) {
            if (isRecording) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 清理资源
     */
    private void cleanupResources() {
        try {
            // 关闭流
            if (audioOutputStream != null) {
                audioOutputStream.close();
                audioOutputStream = null;
            }
            if (audioInputStream != null) {
                audioInputStream.close();
                audioInputStream = null;
            }
        } catch (Exception e) {
            System.err.println("关闭音频流时出错: " + e.getMessage());
        }
    }
    
    /**
     * 初始化语音识别服务
     */
    public void initializeService() {
        // 初始化讯飞语音识别服务
        // 设置APPID、API Key等配置信息
    }
}