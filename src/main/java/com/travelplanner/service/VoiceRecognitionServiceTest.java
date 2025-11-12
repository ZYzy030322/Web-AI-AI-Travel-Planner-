//package com.travelplanner.service;
//
//import com.travelplanner.config.ApiKeyConfig;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.context.annotation.ComponentScan;
//
//import java.io.InputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//
///**
// * 语音识别服务测试类
// * 用于验证语音识别服务的基本功能
// */
//@SpringBootApplication
//@ComponentScan(basePackages = "com.travelplanner")
//public class VoiceRecognitionServiceTest {
//
//    /**
//     * 主方法，用于直接运行测试
//     */
//
//    /**
//     * Java 1.8兼容的方式读取输入流的所有字节
//     * @param inputStream 输入流
//     * @return 流中的所有字节
//     * @throws IOException IO异常
//     */
//    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        int nRead;
//        byte[] data = new byte[1024];
//        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
//            buffer.write(data, 0, nRead);
//        }
//        return buffer.toByteArray();
//    }
//    public static void main(String[] args) {
//        // 启动Spring Boot应用上下文
//        ConfigurableApplicationContext context = SpringApplication.run(VoiceRecognitionServiceTest.class, args);
//
//        try {
//            // 获取语音识别服务
//            VoiceRecognitionService voiceRecognitionService = context.getBean(VoiceRecognitionService.class);
//
//            // 获取API配置
//            ApiKeyConfig apiKeyConfig = context.getBean(ApiKeyConfig.class);
//
//            // 检查是否配置了讯飞API密钥
//            if (apiKeyConfig.getXfyunAppId() == null || apiKeyConfig.getXfyunAppId().isEmpty() ||
//                apiKeyConfig.getXfyunApiKey() == null || apiKeyConfig.getXfyunApiKey().isEmpty() ||
//                apiKeyConfig.getXfyunApiSecret() == null || apiKeyConfig.getXfyunApiSecret().isEmpty()) {
//                System.err.println("错误：未配置讯飞API密钥");
//                System.err.println("请设置以下环境变量：");
//                System.err.println("  XFYUN_APP_ID=你的讯飞应用ID");
//                System.err.println("  XFYUN_API_KEY=你的讯飞API Key");
//                System.err.println("  XFYUN_API_SECRET=你的讯飞API Secret");
//                System.err.println("你可以通过以下方式之一设置环境变量：");
//                System.err.println("  1. 在操作系统中设置环境变量");
//                System.err.println("  2. 在运行命令前添加环境变量，例如：");
//                System.err.println("     XFYUN_APP_ID=xxx XFYUN_API_KEY=yyy XFYUN_API_SECRET=zzz mvn exec:java -Dexec.mainClass=\"com.travelplanner.service.VoiceRecognitionServiceTest\"");
//                System.err.println("  3. 在IDE的运行配置中设置环境变量");
//                return;
//            }
//
//            // 加载测试音频文件
//            InputStream inputStream = VoiceRecognitionServiceTest.class.getClassLoader()
//                .getResourceAsStream("iat/16k_10.pcm");
//
//            if (inputStream == null) {
//                System.err.println("无法找到测试音频文件: iat/16k_10.pcm");
//                return;
//            }
//
//            // 读取音频数据
//            byte[] audioData = readAllBytes(inputStream);
//            inputStream.close();
//
//            System.out.println("加载音频文件成功，文件大小: " + audioData.length + " 字节");
//
//            // 调用语音识别服务
//            System.out.println("开始语音识别...");
//            String result = voiceRecognitionService.recognizeSpeech(audioData);
//
//            // 输出识别结果
//            System.out.println("语音识别结果: " + result);
//
//        } catch (Exception e) {
//            System.err.println("测试过程中发生错误: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            // 关闭应用上下文
//            System.exit(SpringApplication.exit(context));
//        }
//    }
//}