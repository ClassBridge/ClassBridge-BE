package com.linked.classbridge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = ClassBridgeApplication.class)
@TestPropertySource(locations = "classpath:application-test.yml")
class ClassBridgeApplicationTests {

    @Test
    void contextLoads() {
    }

}
