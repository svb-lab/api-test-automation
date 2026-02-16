package com.apitest.config;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Component;

@Component
public class RestAssuredConfig {

    private final ApiConfig apiConfig;

    public RestAssuredConfig(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public RequestSpecification getRequestSpecification() {
        return new RequestSpecBuilder()
                .setBaseUri(apiConfig.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .log(LogDetail.ALL)
                .build();
    }
}
