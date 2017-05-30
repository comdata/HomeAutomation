package cm.homeautomation.telegram;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.TelegramUser;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

public class TelegramBotService {

	private static TelegramBotService instance;
	private TelegramBotsApi telegramBotApi;
	private CommandsHandler bot;
	private static String token;
	private static String user;

	public TelegramBotService() {
		token = ConfigurationService.getConfigurationProperty("telegram", "token");
		user = ConfigurationService.getConfigurationProperty("telegram", "user");

		instance = this;

		EventBusService.getEventBus().register(this);
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

				sendMessage("Bot is alive");

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
		@SuppressWarnings("unchecked")
		List<TelegramUser> resultList = (List<TelegramUser>) em.createQuery("select t from TelegramUser t")
				.getResultList();

		String pattern = "yyyy-MM-dd HH:mm:ss";
		Date date = new Date();
		String defaultFmt = new SimpleDateFormat(pattern).format(date);

		if (resultList != null && !resultList.isEmpty()) {

			for (TelegramUser telegramUser : resultList) {

				SendMessage sendMessage = new SendMessage();
				sendMessage.enableMarkdown(true);

				sendMessage.setChatId(telegramUser.getUserId());
				sendMessage.setText(defaultFmt + ": " + message);

				Executors.newSingleThreadExecutor().execute(new Runnable() {
					@Override
					public void run() {
						try {
							bot.sendMessage(sendMessage);
						} catch (TelegramApiException e) {
						}
					}
				});
			}
		}

		em.close();
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
		// try {
		if (eventObject.getData() instanceof HumanMessageGenerationInterface) {
			HumanMessageGenerationInterface humanMessage = (HumanMessageGenerationInterface) eventObject.getData();
			TelegramBotService.getInstance().sendMessage(humanMessage.getMessageString());
		}

		// if (eventObject.getData() instanceof PresenceState) {
		//
		// EventTranscoder transcoder = new EventTranscoder();
		//
		// String message = transcoder.encode(eventObject);
		//
		// TelegramBotService.getInstance().sendMessage(message);
		// }
		// } catch (EncodeException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}
