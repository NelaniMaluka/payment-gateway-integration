package com.nelani.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Disabled in CI because it fails without real DB")
class PaymentGatewayIntegrationApplicationTests {

	@Test
	void contextLoads() {
	}

}
