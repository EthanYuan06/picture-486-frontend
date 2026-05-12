package com.yuluo.picture486backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "websocket.enabled=false",
        "ai.description.mq.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
})
class Picture486BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
