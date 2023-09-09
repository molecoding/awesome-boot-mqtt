package com.molecoding.nobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.Mqttv5PahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@SpringBootApplication
public class AwesomeBootMqttApplication {
  final private OutboundGateway gateway;
  @Value("${mqtt.uri}")
  private String uri;
  @Value("${mqtt.username}")
  private String user;
  @Value("${mqtt.password}")
  private String password;
  @Value("${mqtt.topic.sub}")
  private String topicSub;
  @Value("${mqtt.topic.pub}")
  private String topicPub;

  public static void main(String[] args) {
    SpringApplication.run(AwesomeBootMqttApplication.class, args);
  }

  @Bean
  public MqttConnectionOptions mqttConnectionOptions() {
    MqttConnectionOptions options = new MqttConnectionOptions();
    options.setServerURIs(new String[]{uri});
    options.setUserName(user);
    options.setPassword(password.getBytes());
    options.setAutomaticReconnect(true);
    options.setKeepAliveInterval(1);
    return options;
  }

  @Bean
  public IntegrationFlow mqttInFlow(MqttConnectionOptions options) {
    Mqttv5PahoMessageDrivenChannelAdapter messageProducer =
      new Mqttv5PahoMessageDrivenChannelAdapter(options, UUID.randomUUID().toString(), topicSub);
    messageProducer.setPersistence(new MemoryPersistence());
    messageProducer.setPayloadType(String.class);
    messageProducer.setMessageConverter(new ByteArrayMessageConverter());
    messageProducer.setManualAcks(false);

    return IntegrationFlows.from(messageProducer)
      .<byte[], String>transform(String::new)
      .log()
      .nullChannel();
  }

  @Bean
  public IntegrationFlow mqttOutFlow(MqttConnectionOptions options) {
    Mqttv5PahoMessageHandler messagePublisher =
      new Mqttv5PahoMessageHandler(options, UUID.randomUUID().toString());
    messagePublisher.setPersistence(new MemoryPersistence());
    messagePublisher.setDefaultTopic(topicPub);
    messagePublisher.setDefaultQos(1);
    messagePublisher.setDefaultRetained(false);

    return IntegrationFlows.from("outbound")
      .log()
      .enrichHeaders(enricher -> enricher.headerExpression(MqttHeaders.TOPIC, "headers['topic']"))
      .handle(messagePublisher)
      .get();
  }

  @PostMapping(path = "/{topic}")
  public void sendMessage(@PathVariable String topic, @RequestBody String message) {
    log.info("Send [{}] to topic [{}]", message, topic);
    gateway.sendOut(message, topic);
  }

  @Service
  @MessagingGateway(defaultRequestChannel = "outbound")
  public interface OutboundGateway {
    void sendOut(@Payload String message, @Header("topic") String topic);
  }
}
