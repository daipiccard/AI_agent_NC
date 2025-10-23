package com.transacciones.transaction_ingestor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"test-topic"}, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class TransactionKafkaIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void shouldSendAndConsumeKafkaMessage() throws Exception {
        String topic = "test-topic";

        // Configurar consumidor
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);

        // Contenedor con listener moderno
        BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();

        ContainerProperties containerProps = new ContainerProperties(topic);
        containerProps.setMessageListener((org.springframework.kafka.listener.MessageListener<String, String>) record -> {
            System.out.println("⚡ Mensaje recibido: " + record.value());
            records.add(record);
        });

        ConcurrentMessageListenerContainer<String, String> container =
                new ConcurrentMessageListenerContainer<>(cf, containerProps);
        container.getContainerProperties().setPollTimeout(3000);
        container.setConcurrency(1);
        container.setBeanName("testContainer");
        container.start();

        // Esperar que se asigne partición
        embeddedKafka.consumeFromAnEmbeddedTopic(cf.createConsumer(), topic);

        // Enviar mensaje - usar sendDefault o especificar el serializer
        String message = "------- Hola Kafka desde test!---------";
        System.out.println("🚀🚀🚀🚀🚀🚀🚀🚀 Enviando mensaje: " + message);
        
        // Opción 1: Usar send con parámetros explícitos
        kafkaTemplate.send(topic, message);
        
        // Opción 2: Si el problema persiste, prueba con esto:
        // kafkaTemplate.setDefaultTopic(topic);
        // kafkaTemplate.sendDefault(message);

        // Esperar recepción
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        
        // Limpiar las comillas dobles extra si existen
        String receivedValue = received.value();
        if (receivedValue.startsWith("\"") && receivedValue.endsWith("\"")) {
            receivedValue = receivedValue.substring(1, receivedValue.length() - 1);
        }
        
        assertThat(receivedValue).isEqualTo(message);

        System.out.println("✅✅✅✅✅✅✅ Test completado, mensaje confirmado.");
        container.stop();
    }
}