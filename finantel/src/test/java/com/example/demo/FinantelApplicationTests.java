package com.example.demo;

import com.finantel.FinantelApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = FinantelApplication.class)
@ActiveProfiles("test")
class FinantelApplicationTests {

	@Test
	void contextLoads() {
	}

}
