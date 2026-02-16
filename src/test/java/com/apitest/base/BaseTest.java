package com.apitest.base;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public abstract class BaseTest {

    protected static final String CONTENT_TYPE_JSON = "application/json";

    @BeforeAll
    static void beforeAllTests() {
        log.info("*****TEST RUN STARTED*****");
    }

    @BeforeEach
    void beforeEachTest(TestInfo testInfo) {
        log.info("Starting Test: {} ", testInfo.getDisplayName());
    }

}
