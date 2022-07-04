package com.tinker.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SpringBootKafkaProducer {

  private static String OUTGOING_TOPIC = "test-out-topic";

  private KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  SpringBootKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendMessage(Message message) {
    kafkaTemplate.send(OUTGOING_TOPIC, message.key(), message.value());
  }
}
