package com.fooddelivery.order.kafka;

import com.fooddelivery.order.kafka.events.DeliveryStatusUpdatedEvent;
import com.fooddelivery.order.kafka.events.OrderReadyEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

/**
 * order-service consumes two different event types (from two different topics, produced by two
 * different services), so it can't rely on Spring Boot's single auto-configured JSON deserializer
 * default type. Each listener gets its own typed factory instead, and ignores the producer's type
 * header entirely since that header would reference a class from the producing service's package.
 */
@Configuration
public class KafkaConsumerConfig {

    private static final String GROUP_ID = "order-service";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderReadyEvent> orderReadyListenerFactory() {
        return listenerFactory(OrderReadyEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeliveryStatusUpdatedEvent> deliveryStatusListenerFactory() {
        return listenerFactory(DeliveryStatusUpdatedEvent.class);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> listenerFactory(Class<T> targetType) {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType);
        deserializer.ignoreTypeHeaders();

        ConsumerFactory<String, T> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps(), new StringDeserializer(), deserializer);

        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    private Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}
