package com.tinker.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class SpringBootKafkaProducer implements Consumer<Message> {

  private static String OUTGOING_TOPIC = "test-out-topic";

  private KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  SpringBootKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void accept(Message message) {
    kafkaTemplate.send(OUTGOING_TOPIC, message.key(), message.value());
  }
}
