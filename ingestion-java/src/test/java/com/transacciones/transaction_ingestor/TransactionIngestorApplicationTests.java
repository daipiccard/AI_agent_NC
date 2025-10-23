package com.transacciones.transaction_ingestor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // <-- ESTO DEBE ESTAR
class TransactionIngestorApplicationTests {

	@Test
	void contextLoads() {
	}

}