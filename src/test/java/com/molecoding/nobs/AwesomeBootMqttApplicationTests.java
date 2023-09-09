package com.molecoding.nobs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class AwesomeBootMqttApplicationTests {
  @Container
  public static GenericContainer<?> emqx =
    new GenericContainer<>("emqx:latest")
      .withExposedPorts(1883)
      .waitingFor(new HostPortWaitStrategy());

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("mqtt.uri", () -> String.format("tcp://localhost:%d", emqx.getMappedPort(1883)));
  }

  @Test
  void contextLoads() {
  }
}
