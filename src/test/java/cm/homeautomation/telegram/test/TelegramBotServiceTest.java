package cm.homeautomation.telegram.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.telegram.TelegramBotService;

public class TelegramBotServiceTest {
	
	@BeforeEach
	public void setup() {
		
	}
	
	@Test
	public void testGetInstance() {
		TelegramBotService instance = TelegramBotService.getInstance();
		
		assertNotNull(instance);
	}
}
