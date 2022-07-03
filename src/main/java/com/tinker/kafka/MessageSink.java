package com.tinker.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.function.Consumer;

public class MessageSink implements Consumer<Message> {
    private final String topic;
    private final Producer<String, String> producer;

    public MessageSink(String topic, Producer<String, String> producer) {
        this.topic = topic;
        this.producer = producer;
    }

    @Override
    public void accept(final Message message) {
        ProducerRecord record = new ProducerRecord(topic, message.key(), message.value());
        producer.send(record);
    }
}
