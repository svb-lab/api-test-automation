package com.apitest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "api")
@Getter
@Setter
public class ApiConfig {

    private BaseConfig base = new BaseConfig();

    @Getter
    @Setter
    public static class BaseConfig {
        private String url;
    }

    public String getBaseUrl() {
        return base.getUrl();
    }

}
