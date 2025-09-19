package com.hebert.hdownloader.Config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.hebert.hdownloader.Message.MusicDownloadRequestMessage;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
  @Value(value = "${spring.kafka.bootstrap-servers}")
  private String bootstrapAddress;

  @Bean
  public ConsumerFactory<String, MusicDownloadRequestMessage> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
        bootstrapAddress);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "music-download-service");

    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

    props.put(
        ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, 
        StringDeserializer.class);
    props.put(
        ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, 
        JsonDeserializer.class);

    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, MusicDownloadRequestMessage.class.getName());

    return new DefaultKafkaConsumerFactory<>(
        props,
        new StringDeserializer(),
        new ErrorHandlingDeserializer<>(new JsonDeserializer<>(MusicDownloadRequestMessage.class, false))
    );
  }

  @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

  @Bean
    public ProducerFactory<Object, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, MusicDownloadRequestMessage> kafkaListenerContainerFactory(
    ConsumerFactory<String, MusicDownloadRequestMessage> consumerFactory,
    KafkaTemplate<Object, Object> template
  ) {

    ConcurrentKafkaListenerContainerFactory<String, MusicDownloadRequestMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());

    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> {
                    System.out.println("Error parsing, moving message to dead letter queue...");
                    // route to topicName-dlt
                    return new org.apache.kafka.common.TopicPartition("music-download-requests-dlt", record.partition());
                });

    DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer);

    factory.setCommonErrorHandler(errorHandler);

    return factory;
  }
}
