package cm.homeautomation.telegram;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.TelegramFilter;
import cm.homeautomation.entities.TelegramUser;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import cm.homeautomation.services.base.AutoCreateInstance;
import io.quarkus.scheduler.Scheduled;

@AutoCreateInstance
public class TelegramBotService {

	private static final String USER = "user";
	private static final String TOKEN = "token";
	private static final String TELEGRAM = "telegram";
	private static TelegramBotService instance;

	public static TelegramBotService getInstance() {
		if (instance == null) {
			instance = new TelegramBotService();
		}
		return instance;
	}

	private static TelegramBotsApi telegramBotApi;

	private static CommandsHandler bot;
	private static boolean enabled;
	private static List<TelegramFilter> filterList;

	public TelegramBotService() {

		this.enabled = Boolean.parseBoolean(ConfigurationService.getConfigurationProperty(TELEGRAM, "enabled"));

		if (enabled) {
			instance = this;
			init();

			EventBusService.getEventBus().register(this);
		}
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void handleEvent(final EventObject eventObject) {
		if (eventObject.getData() instanceof HumanMessageGenerationInterface) {
			final HumanMessageGenerationInterface humanMessage = (HumanMessageGenerationInterface) eventObject
					.getData();

			boolean ignore = humanMessage.getClass().isAnnotationPresent(TelegramIgnore.class);

			String messageString = humanMessage.getMessageString();
			if (!ignore) {
				boolean filtered = checkMessageFiltered(messageString);

				// message must not be set to ignore and not be filtered
				if (!filtered) {
					TelegramBotService.getInstance().sendMessage(messageString);
				}
			}

		}
	}

	private boolean checkMessageFiltered(String message) {
		if (message != null) {
			for (TelegramFilter telegramFilter : filterList) {
				if (message.contains(telegramFilter.getMessagePart())) {
					return true;
				}
			}
		}

		return false;
	}

	public void init() {
		try {

			updateTelegramFilter();

			if (this.enabled) {
				ApiContextInitializer.init();
				telegramBotApi = new TelegramBotsApi();
				registerBot();
			}
		} catch (final Exception e) {
			LogManager.getLogger(this.getClass()).error(e);
		}
	}

	@Scheduled(every = "120s")
	public void updateTelegramFilter() {
		EntityManager em = EntityManagerService.getManager();

		filterList = em.createQuery("select f from TelegramFilter f", TelegramFilter.class).getResultList();
	}

	private void registerBot() {

		try {
			String token = ConfigurationService.getConfigurationProperty(TELEGRAM, TOKEN);
			String user = ConfigurationService.getConfigurationProperty(TELEGRAM, USER);
			bot = new CommandsHandler(user, token);
			telegramBotApi.registerBot(bot);

			sendMessage("Bot is alive");

		} catch (final TelegramApiException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}
	}

	/**
	 * send a message
	 *
	 * @param message
	 */
	public void sendMessage(final String message) {
		final EntityManager em = EntityManagerService.getManager();

		final List<TelegramUser> resultList = em.createQuery("select t from TelegramUser t", TelegramUser.class)
				.getResultList();

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
						LogManager.getLogger(this.getClass()).error(e);
					}
				});
			}
		}

	}
}
