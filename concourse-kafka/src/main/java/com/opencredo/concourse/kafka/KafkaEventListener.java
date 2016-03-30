package com.opencredo.concourse.kafka;

import com.opencredo.concourse.domain.events.channels.EventsInChannel;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaEventListener implements Runnable {

    public static KafkaEventListener using(EventsInChannel<String> inChannel, KafkaConsumer<String, String> kafkaConsumer, long timeout, List<String> topics) {
        return new KafkaEventListener(inChannel, kafkaConsumer, timeout, topics);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaEventListener.class);

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final EventsInChannel<String> inChannel;
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final long timeout;
    private final List<String> topics;

    private KafkaEventListener(EventsInChannel<String> inChannel, KafkaConsumer<String, String> kafkaConsumer, long timeout, List<String> topics) {
        this.inChannel = inChannel;
        this.kafkaConsumer = kafkaConsumer;
        this.timeout = timeout;
        this.topics = topics;
    }

    @Override
    public void run() {
        try {
            kafkaConsumer.subscribe(topics);
            while (!closed.get()) {
                kafkaConsumer.poll(timeout).forEach(this::processRecord);
            }
        } catch (WakeupException e) {
            if (!closed.get()) throw e;
        } finally {
            kafkaConsumer.close();
        }
    }

    private void processRecord(ConsumerRecord<String, String> record) {
        try {
            inChannel.accept(record.value());
        } catch (Exception e) {
            LOGGER.warn("Exception processing message: {}", e);
        }
    }

    public void shutdown() {
        closed.set(true);
        kafkaConsumer.wakeup();
    }

}
