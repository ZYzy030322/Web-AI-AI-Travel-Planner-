package com.travelplanner.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.travelplanner.config.ApiKeyConfig;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class LLMService extends WebSocketListener {

    // 星火大模型API配置 (根据B_WsXModel更新)
    public static final String HOST_URL = "https://spark-api.xf-yun.com/v1/x1";
    public static final String DOMAIN = "spark-x";

    @Autowired
    private ApiKeyConfig apiKeyConfig;
//    private String appid = "b4d63f8a";
//    private String apiSecret = "ZWVlM2QxOWI3ZmMyNTZkMjMxOWU4NjFj";
//    private String apiKey = "a7514dcbc8bb17ded7ea3438cb8e7fbd";
    String appId;
    String apiKey;
    String apiSecret;

    
    private static final Gson gson = new Gson();
    private Boolean wsCloseFlag = false;
    private CompletableFuture<String> resultFuture;
    private StringBuilder resultBuilder = new StringBuilder();
    private String question;

    /**
     * 向讯飞星火大模型发送问题并获取回答
     * @param question 用户提出的问题（可以是语音识别后的文本）
     * @return 大模型的回答
     */
    public String getLLMResponse(String question) {
        try {
            this.question = question;
            
            // 获取API配置
            // 如果配置了自定义API密钥则使用，否则使用默认值
            if (apiKeyConfig.getLlmAppId() != null && !apiKeyConfig.getLlmAppId().isEmpty()) {
                this.appId = apiKeyConfig.getLlmAppId();
            }
            if (apiKeyConfig.getLlmApiKey() != null && !apiKeyConfig.getLlmApiKey().isEmpty()) {
                this.apiKey = apiKeyConfig.getLlmApiKey();
            }
            if (apiKeyConfig.getLlmApiSecret() != null && !apiKeyConfig.getLlmApiSecret().isEmpty()) {
                this.apiSecret = apiKeyConfig.getLlmApiSecret();
            }

            // 初始化结果Future
            resultFuture = new CompletableFuture<>();
            resultBuilder = new StringBuilder();
            wsCloseFlag = false;
            
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
            WebSocket webSocket = client.newWebSocket(request, this);
            
            // 等待识别结果，最多等待300秒
            return resultFuture.get(300, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return "大模型调用失败: " + e.getMessage();
        }
    }
    
    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        super.onOpen(webSocket, response);
        System.out.print("大模型：");
        // 连接打开后发送消息
        new Thread(new MyThread(webSocket)).start();
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        System.out.println(text);
        JsonParse myJsonParse = gson.fromJson(text, JsonParse.class);
        if (myJsonParse.header.code != 0) {
            System.out.println("发生错误，错误码为：" + myJsonParse.header.code);
            System.out.println("本次请求的sid为：" + myJsonParse.header.sid);
            resultFuture.complete("大模型调用失败: 错误码=" + myJsonParse.header.code);
            webSocket.close(1000, "");
            return;
        }
        
        // 处理返回的结果
        if (myJsonParse.payload.choices != null && myJsonParse.payload.choices.text != null) {
            for (Text t : myJsonParse.payload.choices.text) {
                resultBuilder.append(t.content);
                System.out.print(t.content);
            }
        }
        
        // 最后一帧数据，返回结果
        if (myJsonParse.header.status == 2) {
            System.out.println();
            System.out.println("*************************************************************************************");
            String rawResult = resultBuilder.toString();
            // 清理结果中的多余字符
            String cleanResult = cleanResult(rawResult);
            resultFuture.complete(cleanResult);
            wsCloseFlag = true;
            webSocket.close(1000, "");
        }
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        System.out.println("WebSocket连接失败: " + t.getMessage());
        resultFuture.complete("大模型连接失败: " + t.getMessage());
    }
    
    // 清理大模型返回的结果，去除多余的null字符和其他无关内容
    private String cleanResult(String rawResult) {
        if (rawResult == null || rawResult.isEmpty()) {
            return rawResult;
        }
        
        // 去除字符串中的"null"字符
        String cleaned = rawResult.replace("null", "");
        
        // 查找JSON的开始和结束位置
        int jsonStart = cleaned.indexOf("{");
        int jsonEnd = cleaned.lastIndexOf("}") + 1;
        
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return cleaned.substring(jsonStart, jsonEnd);
        }
        
        return cleaned.trim();
    }
    
    // 线程来发送音频与参数
    class MyThread extends Thread {
        private final WebSocket webSocket;

        public MyThread(WebSocket webSocket) {
            this.webSocket = webSocket;
        }

        public void run() {
            try {
                JSONObject requestJson = new JSONObject();

                JSONObject header = new JSONObject();  // header参数
                header.put("app_id", appId);
                header.put("uid", UUID.randomUUID().toString().substring(0, 10));

                JSONObject parameter = new JSONObject(); // parameter参数
                JSONObject chat = new JSONObject();
                chat.put("domain", DOMAIN);
                chat.put("temperature", 0.5);
                chat.put("max_tokens", 4096);
                parameter.put("chat", chat);

                JSONObject payload = new JSONObject(); // payload参数
                JSONObject message = new JSONObject();
                JSONArray text = new JSONArray();

                RoleContent roleContent = new RoleContent(); // 问题
                roleContent.role = "user";
                roleContent.content = question;
                text.add(JSON.toJSON(roleContent));

                message.put("text", text);
                payload.put("message", message);

                requestJson.put("header", header); // 组合
                requestJson.put("parameter", parameter);
                requestJson.put("payload", payload);
                // System.err.println(requestJson); // 可以打印看每次的传参明细
                webSocket.send(requestJson.toString());
                // 等待服务端返回完毕后关闭
                while (!wsCloseFlag) {
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 构建鉴权URL
     */
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" + "date: " + date + "\n" + "GET " + url.getPath() + " HTTP/1.1";
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).
                addQueryParameter("date", date).
                addQueryParameter("host", url.getHost()).
                build();

        return httpUrl.toString();
    }

    //返回的json结果拆解
    static class JsonParse {
        Header header;
        Payload payload;
    }

    static class Header {
        int code;
        int status;
        String sid;
    }

    static class Payload {
        Choices choices;
    }

    static class Choices {
        List<Text> text;
    }

    static class Text {
        String role;
        String content;
    }

    static class RoleContent {
        String role;
        String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}