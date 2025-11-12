package com.travelplanner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "api.keys")
public class ApiKeyConfig {
    private String xfyunAppId;
    private String xfyunApiKey;
    private String xfyunApiSecret;
    private String amapKey;
    private String llmApiKey;
    private String llmApiSecret;
    private String llmAppId;
    
    // Getters and Setters
    public String getXfyunAppId() {
        return xfyunAppId;
    }
    
    public void setXfyunAppId(String xfyunAppId) {
        this.xfyunAppId = xfyunAppId;
    }
    
    public String getXfyunApiKey() {
        return xfyunApiKey;
    }
    
    public void setXfyunApiKey(String xfyunApiKey) {
        this.xfyunApiKey = xfyunApiKey;
    }
    
    public String getXfyunApiSecret() {
        return xfyunApiSecret;
    }
    
    public void setXfyunApiSecret(String xfyunApiSecret) {
        this.xfyunApiSecret = xfyunApiSecret;
    }
    
    public String getAmapKey() {
        return amapKey;
    }
    
    public void setAmapKey(String amapKey) {
        this.amapKey = amapKey;
    }
    
    public String getLlmApiKey() {
        return llmApiKey;
    }
    
    public void setLlmApiKey(String llmApiKey) {
        this.llmApiKey = llmApiKey;
    }
    
    public String getLlmApiSecret() {
        return llmApiSecret;
    }
    
    public void setLlmApiSecret(String llmApiSecret) {
        this.llmApiSecret = llmApiSecret;
    }
    
    public String getLlmAppId() {
        return llmAppId;
    }
    
    public void setLlmAppId(String llmAppId) {
        this.llmAppId = llmAppId;
    }
}