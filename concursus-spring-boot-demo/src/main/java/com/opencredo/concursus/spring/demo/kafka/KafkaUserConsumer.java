package com.opencredo.concursus.spring.demo.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.spring.demo.commands.UserCommands;
import com.opencredo.concursus.spring.demo.views.CreateUserRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@Component
@Profile("kafka")
public class KafkaUserConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaUserConsumer.class);
    public static final String KAFKA_USER_GROUP = "user";

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private UserCommands userCommands;

    @Value("${kafka.user.topic}")
    @NotNull
    private String topic;

    @Value("${kafka.poll.timeout:10000}")
    private long timeout;

    private KafkaConsumerRunner runner;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private class KafkaConsumerRunner implements Runnable {

        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final KafkaConsumer<String, String> consumer;
        private final ObjectMapper objectMapper = new ObjectMapper();

        public KafkaConsumerRunner(KafkaConsumer<String, String> consumer) {
            this.consumer = consumer;
        }

        public void run() {
            try {
                consumer.subscribe(Arrays.asList(topic));
                while (!closed.get()) {
                    ConsumerRecords<String, String> records = consumer.poll(timeout);
                    for (ConsumerRecord<String, String> record : records) {
                        try {
                            CreateUserRequest request = readRequest(record);
                            UUID id = UUID.randomUUID();
                            userCommands.create(
                                    StreamTimestamp.of("admin", Instant.now()),
                                    id,
                                    request.getName(),
                                    MessageDigest.getInstance("MD5").digest(request.getPassword().getBytes()))
                                    .thenAccept(userId -> LOGGER.debug("user created: {}", userId));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            } catch (WakeupException e) {
                // Ignore exception if closing
                if (!closed.get()) throw e;
            } finally {
                consumer.close();
            }
        }

        private CreateUserRequest readRequest(ConsumerRecord<String, String> record) throws IOException {
            String data = record.value();
            CreateUserRequest request = objectMapper.readValue(data, CreateUserRequest.class);
            return request;
        }

        // Shutdown hook which can be called from a separate thread
        public void shutdown() {
            closed.set(true);
            consumer.wakeup();
        }
    }

    @PostConstruct
    public void start() {
        Properties props = kafkaProperties();
        KafkaConsumerRunner runner = new KafkaConsumerRunner(new KafkaConsumer<>(props));
        this.runner = runner;

        executor.submit(runner);
    }

    @PreDestroy
    public void stop() throws Exception {
        runner.shutdown();
        executor.awaitTermination(1L, TimeUnit.SECONDS);
    }

    private Properties kafkaProperties() {
        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        props.put(GROUP_ID_CONFIG, KAFKA_USER_GROUP);
        props.put(ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }
}
