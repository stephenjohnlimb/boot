package com.tinker.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public class KafkaTest {

    /**
     * Function that sets up a mockConsumer with a number of messages, key value pairs.
     */
    private static final Function<List<Message>, MessageSource> messageSourceCreator = messages -> {
        var topic = "general_messages";
        var mockConsumer = new MockConsumer<String, String>(OffsetResetStrategy.EARLIEST);
        var messageSource = new MessageSource(topic, mockConsumer);

        mockConsumer.schedulePollTask(() -> {
            mockConsumer.rebalance(Collections.singletonList(new TopicPartition(topic, 0)));
            for(int i=0; i<messages.size(); i++)
                mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, i, messages.get(i).key(), messages.get(i).value()));
        });
        mockConsumer.schedulePollTask(messageSource::stop);

        HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
        TopicPartition tp = new TopicPartition(topic, 0);
        startOffsets.put(tp, 0L);
        mockConsumer.updateBeginningOffsets(startOffsets);
        return messageSource;
    };

    /**
     * A bit convoluted, but just enables checking of message and producerRecords to ensure they
     * have the same key and values.
     */
    private static final Function<Map.Entry<Message, ProducerRecord<String, String>>, Boolean> equalityChecker =
            entry -> entry.getKey().key().equals(entry.getValue().key())
                     && entry.getKey().value().equals(entry.getValue().value());

    @Test
    public void checkSinkRetainsMessages() {
        var topic = "general_messages";

        //Make a mock producer - that just gathers the messages it gets sent
        MockProducer<String, String> mockProducer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());

        //Plug that mock producer into the sink - this iw what we will normally use to send messages.
        var sink = new MessageSink(topic, mockProducer);
        var expected = List.of(new Message("rugby", "Playoffs"), new Message("soccer", "worldCup"));
        expected.forEach(sink);

        //Check the messages.
        int historySize = mockProducer.history().size();
        Assertions.assertEquals(2, historySize);

        Assertions.assertTrue(IntStream
                .range(0,historySize)
                .mapToObj(index -> Map.entry(expected.get(index), mockProducer.history().get(index)))
                .map(equalityChecker)
                .reduce(true, (existing, current) -> existing && current));
    }

    @Test
    public void checkSourceProducesMessages()
    {
        var messageSource = messageSourceCreator.apply(List.of(new Message("rugby", "Playoffs"), new Message("soccer", "worldCup")));

        List<Message> messages = new ArrayList<>();

        var businessOperation = new MainBusinessFunction();

        //just how nice is this?
        Consumer<Message> recordMessage = messages::add;
        Consumer<Message> composed = message -> recordMessage.accept(businessOperation.apply(message));

        messageSource
                .withExceptionsTo(System.err::println)
                .sendMessagesTo(composed);

        System.out.println("Complete with: " + messages);
    }
}
