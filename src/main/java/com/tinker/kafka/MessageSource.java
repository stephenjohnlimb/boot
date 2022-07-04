package com.tinker.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.Collections;
import java.util.stream.StreamSupport;

/**
 * Wrapper around a kafka Consumer, that calls out to both
 * an exceptions function Consumer and a normal processing Consumer for each
 * incoming message it receives on a specific topic.
 *
 * com.tinker.kafka.KafkaTest has example of this use.
 */
public final class MessageSource {

    private final String topic;
    private final Consumer<String, String> consumer;
    private java.util.function.Consumer<RuntimeException> exceptionConsumer;

    public MessageSource(final String topic, final Consumer<String, String> consumer) {
        assert (topic != null);
        assert (consumer != null);

        this.topic = topic;
        this.consumer = consumer;
    }

    public MessageSource withExceptionsTo(final java.util.function.Consumer<RuntimeException> exceptionConsumer) {
        assert (exceptionConsumer != null);
        this.exceptionConsumer = exceptionConsumer;
        return this;
    }

    public void sendMessagesTo(final java.util.function.Consumer<Message> messageConsumer) {
        assert (messageConsumer != null);

        boolean continuePolling = true;

        while (continuePolling) {
            try {
                consumer.subscribe(Collections.singleton(topic));
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                StreamSupport.stream(records.spliterator(), false)
                        .map(record -> new Message(record.key(), record.value()))
                        .forEach(messageConsumer);
                consumer.commitSync();
            } catch (WakeupException e) {
                continuePolling = false;
            } catch (RuntimeException ex) {
                if (exceptionConsumer != null)
                    exceptionConsumer.accept(ex);
            } finally {
                if(!continuePolling)
                    consumer.close();
            }
        }
    }

    public void stop() {
        consumer.wakeup();
    }
}
