package com.shopsphere.orderservice1;

import com.shopsphere.orderservice1.event.OrderEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class OrderService1ApplicationTests {

    @MockitoBean
    OrderEventPublisher orderEventPublisher;

    @Test
    void contextLoads() {
    }
}
