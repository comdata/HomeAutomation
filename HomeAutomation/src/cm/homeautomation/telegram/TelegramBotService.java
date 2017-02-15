package cm.homeautomation.telegram;

import java.util.List;

import javax.persistence.EntityManager;
import javax.websocket.EncodeException;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.TelegramUser;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.eventbus.EventTranscoder;

public class TelegramBotService {

	private static TelegramBotService instance;
	private TelegramBotsApi telegramBotApi;
	private CommandsHandler bot;
	private static String token;
	private static String user;

	public TelegramBotService() {
		token = ConfigurationService.getConfigurationProperty("telegram", "token");
		user = ConfigurationService.getConfigurationProperty("telegram", "user");
	}

	public static TelegramBotService getInstance() {
		if (instance == null) {
			instance = new TelegramBotService();
		}
		return instance;
	}

	public void init() {
		try {
			ApiContextInitializer.init();
			telegramBotApi = new TelegramBotsApi();
			try {
				bot = new CommandsHandler(user, token);
				telegramBotApi.registerBot(bot);

			} catch (TelegramApiException e) {

			}
		} catch (Exception e) {
		}
	}

	/**
	 * send a message
	 * 
	 * @param message
	 */
	public void sendMessage(String message) {
		EntityManager em = EntityManagerService.getNewManager();
		List<TelegramUser> resultList = (List<TelegramUser>) em.createQuery("select t from TelegramUser t")
				.getResultList();

		if (resultList != null && !resultList.isEmpty()) {

			for (TelegramUser telegramUser : resultList) {

				SendMessage sendMessage = new SendMessage();
				sendMessage.enableMarkdown(true);

				sendMessage.setChatId(telegramUser.getUserId());
				sendMessage.setText(message);
				try {
					bot.sendMessage(sendMessage);
				} catch (TelegramApiException e) {
				}
			}
		}
	}

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		TelegramBotService.token = token;
	}

	public static String getUser() {
		return user;
	}

	public static void setUser(String user) {
		TelegramBotService.user = user;
	}

	@Subscribe
	public void handleEvent(EventObject eventObject) {
		try {

			EventTranscoder transcoder = new EventTranscoder();

			String message = transcoder.encode(eventObject);

			TelegramBotService.getInstance().sendMessage(message);
		} catch (EncodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
