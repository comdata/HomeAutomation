package cm.homeautomation.telegram;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;

import org.greenrobot.eventbus.Subscribe;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.TelegramUser;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

//@AutoCreateInstance
public class TelegramBotService {

	private static TelegramBotService instance;
	private static String token;
	private static String user;

	public static TelegramBotService getInstance() {
		if (instance == null) {
			instance = new TelegramBotService();
		}
		return instance;
	}

	public static String getToken() {
		return token;
	}

	public static String getUser() {
		return user;
	}

	public static void setToken(final String token) {
		TelegramBotService.token = token;
	}

	public static void setUser(final String user) {
		TelegramBotService.user = user;
	}

	private TelegramBotsApi telegramBotApi;

	private CommandsHandler bot;

	public TelegramBotService() {
		token = ConfigurationService.getConfigurationProperty("telegram", "token");
		user = ConfigurationService.getConfigurationProperty("telegram", "user");

		instance = this;
		init();

		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void handleEvent(final EventObject eventObject) {
		// try {
		if (eventObject.getData() instanceof HumanMessageGenerationInterface) {
			final HumanMessageGenerationInterface humanMessage = (HumanMessageGenerationInterface) eventObject
					.getData();
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

	public void init() {
		try {
			ApiContextInitializer.init();
			telegramBotApi = new TelegramBotsApi();
			try {
				bot = new CommandsHandler(user, token);
				telegramBotApi.registerBot(bot);

				sendMessage("Bot is alive");

			} catch (final TelegramApiException e) {

			}
		} catch (final Exception e) {
		}
	}

	/**
	 * send a message
	 *
	 * @param message
	 */
	public void sendMessage(final String message) {
		final EntityManager em = EntityManagerService.getNewManager();
		@SuppressWarnings("unchecked")
		final List<TelegramUser> resultList = em.createQuery("select t from TelegramUser t").getResultList();

		final String pattern = "yyyy-MM-dd HH:mm:ss";
		final Date date = new Date();
		final String defaultFmt = new SimpleDateFormat(pattern).format(date);

		if ((resultList != null) && !resultList.isEmpty()) {

			for (final TelegramUser telegramUser : resultList) {

				final SendMessage sendMessage = new SendMessage();
				sendMessage.enableMarkdown(true);

				sendMessage.setChatId(telegramUser.getUserId());
				sendMessage.setText(defaultFmt + ": " + message);

				Executors.newSingleThreadExecutor().execute(() -> {
					try {
						bot.sendMessage(sendMessage);
					} catch (final TelegramApiException e) {
					}
				});
			}
		}

		em.close();
	}
}
