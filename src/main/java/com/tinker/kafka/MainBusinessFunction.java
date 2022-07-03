package com.tinker.kafka;

import java.util.function.Function;

/**
 * This is the main business function. In this case all it does is convert the
 * message 'value' to upper case!
 *
 * But obviously it would normally be a detailed set of operations and activities using
 * a full domain model or something.
 */
public final class MainBusinessFunction implements Function<Message, Message> {
    @Override
    public Message apply(Message message) {
        assert (message != null);
        return new Message(message.key(), message.value().toUpperCase());
    }
}
