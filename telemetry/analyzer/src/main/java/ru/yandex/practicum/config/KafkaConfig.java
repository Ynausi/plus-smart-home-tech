package ru.yandex.practicum.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.practicum.kafka.HubEventDeserializer;
import ru.practicum.kafka.SensorSnapshotDeserializer;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorSnapshotAvro;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, HubEventAvro> hubConsumerFactory() {
        Map<String,Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG,"analyzer-hub");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,HubEventAvro> hubKafkaListenerContainerFactory(
            ConsumerFactory<String,HubEventAvro> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String,HubEventAvro> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        return factory;

    }

    @Bean
    public ConsumerFactory<String, SensorSnapshotAvro> snapshotConsumerFactory() {
        Map<String,Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorSnapshotDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG,"analyzer-snapshot");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,SensorSnapshotAvro> snapshotKafkaListenerContainerFactory(
            ConsumerFactory<String,SensorSnapshotAvro> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String,SensorSnapshotAvro> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}
