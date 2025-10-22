package com.transacciones.transaction_ingestor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;  // <- IMPORT NECESARIO
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = { "transactions-topic-test" }, brokerProperties = {
    "listeners=PLAINTEXT://localhost:9093",
    "port=9093"
})
class TransactionIngestorApplicationTests {

	@Test
	void contextLoads() {
	}

}


