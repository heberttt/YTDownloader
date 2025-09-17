package com.hebert.hdownloader.Config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.hebert.hdownloader.Message.MusicDownloadRequestMessage;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
  @Value(value = "${spring.kafka.consumer.bootstrap-servers}")
  private String bootstrapAddress;

  @Bean
  public ConsumerFactory<String, MusicDownloadRequestMessage> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
        bootstrapAddress);
    props.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        JsonDeserializer.class);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "music-download-service");

    return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
        new JsonDeserializer<>(MusicDownloadRequestMessage.class, false));
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, MusicDownloadRequestMessage> kafkaListenerContainerFactory() {

    ConcurrentKafkaListenerContainerFactory<String, MusicDownloadRequestMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());

    return factory;
  }
}
