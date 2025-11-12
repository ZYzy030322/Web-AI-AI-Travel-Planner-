package com.travelplanner.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.travelplanner.config.ApiKeyConfig;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class XfyunWebSocketService extends WebSocketListener {
    private static final String HOST_URL = "https://iat-api.xfyun.cn/v2/iat";
    private static final int StatusFirstFrame = 0;
    private static final int StatusContinueFrame = 1;
    private static final int StatusLastFrame = 2;
    private static final Gson json = new Gson();
    
    // 开始时间
    private static Date dateBegin = new Date();
    // 结束时间
    private static Date dateEnd = new Date();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
    
    @Autowired
    private ApiKeyConfig apiKeyConfig;
    
    private WebSocket webSocket;
    private ResultCallback resultCallback;
    private Decoder decoder = new Decoder();
    private boolean isRealTimeMode = false;
    private String currentAppId;
    
    /**
     * 通过WebSocket连接发送音频数据并获取识别结果
     * @param audioData 音频数据
     * @param resultCallback 结果回调函数
     */
    public void recognizeSpeech(byte[] audioData, ResultCallback resultCallback) {
        try {
            // 清理之前的连接
            closeConnection();
            
            this.resultCallback = resultCallback;
            String appId = apiKeyConfig.getXfyunAppId();
            String apiKey = apiKeyConfig.getXfyunApiKey();
            String apiSecret = apiKeyConfig.getXfyunApiSecret();
            
            if (appId == null || appId.isEmpty() || 
                apiKey == null || apiKey.isEmpty() || 
                apiSecret == null || apiSecret.isEmpty()) {
                resultCallback.onError(-1, "讯飞API配置不完整");
                return;
            }
            
            // 构建鉴权url
            String authUrl = getAuthUrl(HOST_URL, apiKey, apiSecret);
            
            // 创建WebSocket客户端
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            
            // 将url中的 schema http://和https://分别替换为ws:// 和 wss://
            String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
            
            Request request = new Request.Builder().url(url).build();
            
            // 建立WebSocket连接
            this.webSocket = client.newWebSocket(request, this);
            
            // 发送音频数据
            sendAudioData(audioData, appId);
            
        } catch (Exception e) {
            resultCallback.onError(-1, "建立连接时发生错误: " + e.getMessage());
        }
    }
    
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        System.out.println("WebSocket连接已打开");
        if (isRealTimeMode) {
            // 发送初始帧
            sendInitialFrame();
        }
    }
    
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        ResponseData resp = json.fromJson(text, ResponseData.class);
        if (resp != null) {
            if (resp.getCode() != 0) {
                System.out.println("code=>" + resp.getCode() + " error=>" + resp.getMessage() + " sid=" + resp.getSid());
                System.out.println("错误码查询链接：https://www.xfyun.cn/document/error-code");
                resultCallback.onError(resp.getCode(), resp.getMessage());
                return;
            }
            if (resp.getData() != null) {
                if (resp.getData().getResult() != null) {
                    Text te = resp.getData().getResult().getText();
                    try {
                        decoder.decode(te);
                        String partialResult = decoder.toString();
                        System.out.println("中间识别结果 ==》" + partialResult);
                        resultCallback.onPartialResult(partialResult);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (resp.getData().getStatus() == 2) {
                    // 数据全部返回完毕
                    System.out.println("session end ");
                    dateEnd = new Date();
                    System.out.println(sdf.format(dateBegin) + "开始");
                    System.out.println(sdf.format(dateEnd) + "结束");
                    System.out.println("耗时:" + (dateEnd.getTime() - dateBegin.getTime()) + "ms");
                    System.out.println("最终识别结果 ==》" + decoder.toString());
                    System.out.println("本次识别sid ==》" + resp.getSid());
                    resultCallback.onFinalResult(decoder.toString());
                    decoder.discard();
                    webSocket.close(1000, "");
                }
            }
        }
    }
    
    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        try {
            if (null != response) {
                int code = response.code();
                System.out.println("onFailure code:" + code);
                System.out.println("onFailure body:" + response.body().string());
                if (101 != code) {
                    System.out.println("connection failed");
                    resultCallback.onError(code, "连接失败");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        resultCallback.onError(-1, "WebSocket连接失败: " + t.getMessage());
    }
    
    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        System.out.println("WebSocket连接已关闭，代码: " + code + "，原因: " + reason);
    }
    
    /**
     * 发送音频数据到讯飞语音识别服务（实时模式）
     * @param audioData 音频数据
     */
    public void sendRealTimeAudioData(byte[] audioData) {
        if (webSocket != null && isRealTimeMode) {
            JsonObject frame = new JsonObject();
            JsonObject data = new JsonObject();
            data.addProperty("status", StatusContinueFrame);
            data.addProperty("format", "audio/L16;rate=16000");
            data.addProperty("encoding", "raw");
            data.addProperty("audio", Base64.getEncoder().encodeToString(audioData));
            frame.add("data", data);
            webSocket.send(frame.toString());
        }
    }
    
    /**
     * 发送结束帧
     */
    public void sendFinishFrame() {
        if (webSocket != null && isRealTimeMode) {
            JsonObject frame = new JsonObject();
            JsonObject data = new JsonObject();
            data.addProperty("status", StatusLastFrame);
            data.addProperty("audio", "");
            data.addProperty("format", "audio/L16;rate=16000");
            data.addProperty("encoding", "raw");
            frame.add("data", data);
            webSocket.send(frame.toString());
            
            // 关闭WebSocket连接
            webSocket.close(1000, "识别完成");
        }
    }
    
    /**
     * 发送初始帧
     */
    private void sendInitialFrame() {
        if (webSocket != null && isRealTimeMode) {
            JsonObject frame = new JsonObject();
            JsonObject business = new JsonObject();
            JsonObject common = new JsonObject();
            JsonObject data = new JsonObject();
            
            // 填充common
            common.addProperty("app_id", currentAppId);
            // 填充business
            business.addProperty("language", "zh_cn");
            business.addProperty("domain", "iat");
            business.addProperty("accent", "mandarin");
            business.addProperty("dwa", "wpgs");
            // 填充data
            data.addProperty("status", StatusFirstFrame);
            data.addProperty("format", "audio/L16;rate=16000");
            data.addProperty("encoding", "raw");
            data.addProperty("audio", "");
            
            // 填充frame
            frame.add("common", common);
            frame.add("business", business);
            frame.add("data", data);
            webSocket.send(frame.toString());
        }
    }
    
    /**
     * 开始实时语音识别会话
     * @param resultCallback 结果回调函数
     */
    public void startRealTimeRecognition(ResultCallback resultCallback) {
        try {
            // 清理之前的连接
            closeConnection();
            
            this.resultCallback = resultCallback;
            this.isRealTimeMode = true;
            this.decoder = new Decoder(); // 重新初始化解码器
            
            String appId = apiKeyConfig.getXfyunAppId();
            String apiKey = apiKeyConfig.getXfyunApiKey();
            String apiSecret = apiKeyConfig.getXfyunApiSecret();
            this.currentAppId = appId;
            
            if (appId == null || appId.isEmpty() || 
                apiKey == null || apiKey.isEmpty() || 
                apiSecret == null || apiSecret.isEmpty()) {
                resultCallback.onError(-1, "讯飞API配置不完整");
                return;
            }
            
            // 构建鉴权url
            String authUrl = getAuthUrl(HOST_URL, apiKey, apiSecret);
            
            // 创建WebSocket客户端
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            
            // 将url中的 schema http://和https://分别替换为ws:// 和 wss://
            String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
            
            Request request = new Request.Builder().url(url).build();
            
            // 建立WebSocket连接
            this.webSocket = client.newWebSocket(request, this);
            
        } catch (Exception e) {
            resultCallback.onError(-1, "建立连接时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 发送音频数据到讯飞语音识别服务
     * @param audioData 音频数据
     * @param appId 应用ID
     */
    private void sendAudioData(byte[] audioData, String appId) {
        new Thread(() -> {
            try {
                int frameSize = 1280; // 每一帧音频的大小,建议每 40ms 发送 122B
                int interval = 40;
                int status = 0;  // 音频的状态
                int offset = 0;
                
                dateBegin = new Date();
                
                while (true) {
                    int len = Math.min(frameSize, audioData.length - offset);
                    if (len <= 0) {
                        status = StatusLastFrame;  //文件读完，改变status 为 2
                    }
                    
                    byte[] buffer = Arrays.copyOfRange(audioData, offset, offset + len);
                    
                    switch (status) {
                        case StatusFirstFrame:   // 第一帧音频status = 0
                            JsonObject frame = new JsonObject();
                            JsonObject business = new JsonObject();  //第一帧必须发送
                            JsonObject common = new JsonObject();  //第一帧必须发送
                            JsonObject data = new JsonObject();  //每一帧都要发送
                            // 填充common
                            common.addProperty("app_id", appId);
                            //填充business
                            business.addProperty("language", "zh_cn");
                            business.addProperty("domain", "iat");
                            business.addProperty("accent", "mandarin");
                            business.addProperty("dwa", "wpgs");
                            //填充data
                            data.addProperty("status", StatusFirstFrame);
                            data.addProperty("format", "audio/L16;rate=16000");
                            data.addProperty("encoding", "raw");
                            data.addProperty("audio", Base64.getEncoder().encodeToString(buffer));
                            //填充frame
                            frame.add("common", common);
                            frame.add("business", business);
                            frame.add("data", data);
                            webSocket.send(frame.toString());
                            status = StatusContinueFrame;  // 发送完第一帧改变status 为 1
                            break;
                        case StatusContinueFrame:  //中间帧status = 1
                            JsonObject frame1 = new JsonObject();
                            JsonObject data1 = new JsonObject();
                            data1.addProperty("status", StatusContinueFrame);
                            data1.addProperty("format", "audio/L16;rate=16000");
                            data1.addProperty("encoding", "raw");
                            data1.addProperty("audio", Base64.getEncoder().encodeToString(buffer));
                            frame1.add("data", data1);
                            webSocket.send(frame1.toString());
                            break;
                        case StatusLastFrame:    // 最后一帧音频status = 2 ，标志音频发送结束
                            JsonObject frame2 = new JsonObject();
                            JsonObject data2 = new JsonObject();
                            data2.addProperty("status", StatusLastFrame);
                            data2.addProperty("audio", "");
                            data2.addProperty("format", "audio/L16;rate=16000");
                            data2.addProperty("encoding", "raw");
                            frame2.add("data", data2);
                            webSocket.send(frame2.toString());
                            System.out.println("sendlast");
                            // 关闭WebSocket连接
                            webSocket.close(1000, "识别完成");
                            return;
                    }
                    if (status != StatusLastFrame) {
                        offset += len;
                    }
                    Thread.sleep(interval); //模拟音频采样延时
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * 关闭现有连接
     */
    private void closeConnection() {
        if (webSocket != null) {
            try {
                webSocket.close(1000, "客户端主动关闭");
            } catch (Exception e) {
                System.err.println("关闭WebSocket连接时出错: " + e.getMessage());
            } finally {
                webSocket = null;
            }
        }
    }
    
    /**
     * 构建鉴权URL
     */
    private String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").
                append("date: ").append(date).append("\n").
                append("GET ").append(url.getPath()).append(" HTTP/1.1");
        Charset charset = Charset.forName("UTF-8");
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder().
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(charset))).
                addQueryParameter("date", date).
                addQueryParameter("host", url.getHost()).
                build();
        return httpUrl.toString();
    }
    
    // 响应数据结构类
    public static class ResponseData {
        private int code;
        private String message;
        private String sid;
        private Data data;
        public int getCode() {
            return code;
        }
        public String getMessage() {
            return this.message;
        }
        public String getSid() {
            return sid;
        }
        public Data getData() {
            return data;
        }
    }
    
    public static class Data {
        private int status;
        private Result result;
        public int getStatus() {
            return status;
        }
        public Result getResult() {
            return result;
        }
    }
    
    public static class Result {
        int bg;
        int ed;
        String pgs;
        int[] rg;
        int sn;
        Ws[] ws;
        boolean ls;
        JsonObject vad;
        public Text getText() {
            Text text = new Text();
            StringBuilder sb = new StringBuilder();
            if (this.ws != null) {
                for (Ws ws : this.ws) {
                    if (ws != null && ws.cw != null && ws.cw.length > 0 && ws.cw[0].w != null) {
                        sb.append(ws.cw[0].w);
                    }
                }
            }
            text.sn = this.sn;
            text.text = sb.toString();
            text.sn = this.sn;
            text.rg = this.rg;
            text.pgs = this.pgs;
            text.bg = this.bg;
            text.ed = this.ed;
            text.ls = this.ls;
            text.vad = this.vad==null ? null : this.vad;
            return text;
        }
    }
    
    public static class Ws {
        Cw[] cw;
        int bg;
        int ed;
    }
    
    public static class Cw {
        int sc;
        String w;
    }
    
    public static class Text {
        int sn;
        int bg;
        int ed;
        String text;
        String pgs;
        int[] rg;
        boolean deleted;
        boolean ls;
        JsonObject vad;
        public String getText() {
            return text;
        }
        @Override
        public String toString() {
            return "Text{" +
                    "bg=" + bg +
                    ", ed=" + ed +
                    ", ls=" + ls +
                    ", sn=" + sn +
                    ", text='" + text + '\'' +
                    ", pgs=" + pgs +
                    ", rg=" + Arrays.toString(rg) +
                    ", deleted=" + deleted +
                    ", vad=" + (vad==null ? "null" : vad.getAsJsonArray("ws").toString()) +
                    '}';
        }
    }
    
    //解析返回数据，仅供参考
    public static class Decoder {
        private Text[] texts;
        private int defc = 10;
        public Decoder() {
            this.texts = new Text[this.defc];
        }
        public synchronized void decode(Text text) {
            if (text == null) return;
            
            if (text.sn >= this.defc) {
                this.resize();
            }
            if ("rpl".equals(text.pgs)) {
                if (text.rg != null && text.rg.length >= 2) {
                    for (int i = text.rg[0]; i <= text.rg[1]; i++) {
                        if (i < this.texts.length && this.texts[i] != null) {
                            this.texts[i].deleted = true;
                        }
                    }
                }
            }
            this.texts[text.sn] = text;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Text t : this.texts) {
                if (t != null && !t.deleted && t.text != null) {
                    sb.append(t.text);
                }
            }
            return sb.toString();
        }
        public void resize() {
            int oc = this.defc;
            this.defc <<= 1;
            Text[] old = this.texts;
            this.texts = new Text[this.defc];
            for (int i = 0; i < oc; i++) {
                this.texts[i] = old[i];
            }
        }
        public void discard(){
            for(int i=0;i<this.texts.length;i++){
                this.texts[i]= null;
            }
        }
    }
    
    /**
     * 识别结果回调接口
     */
    public interface ResultCallback {
        /**
         * 部分识别结果回调
         * @param partialResult 部分识别结果
         */
        void onPartialResult(String partialResult);
        
        /**
         * 最终识别结果回调
         * @param finalResult 最终识别结果
         */
        void onFinalResult(String finalResult);
        
        /**
         * 错误回调
         * @param errorCode 错误码
         * @param errorMessage 错误信息
         */
        void onError(int errorCode, String errorMessage);
    }
}