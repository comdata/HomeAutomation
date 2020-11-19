package cm.homeautomation.telegram;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.TelegramFilter;
import cm.homeautomation.entities.TelegramUser;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@Singleton
@Startup
public class TelegramBotService {

	@Inject
	EventBus bus;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	private static final String USER = "user";
	private static final String TOKEN = "token";
	private static final String TELEGRAM = "telegram";



	private static TelegramBotsApi telegramBotApi;

	private static CommandsHandler bot;
	private static boolean enabled;
	private static List<TelegramFilter> filterList;

	public void startup(@Observes StartupEvent event) {
				enabled = Boolean.parseBoolean(configurationService.getConfigurationProperty(TELEGRAM, "enabled"));
		if (enabled) {

			init();
		}
	}

	@ConsumeEvent(value = "EventObject", blocking = true)
	public void handleEvent(final EventObject eventObject) {

		if (enabled) {
			if (eventObject.getData() instanceof HumanMessageGenerationInterface) {
				final HumanMessageGenerationInterface humanMessage = (HumanMessageGenerationInterface) eventObject
						.getData();

				boolean ignore = humanMessage.getClass().isAnnotationPresent(TelegramIgnore.class);

				String messageString = humanMessage.getMessageString();
				if (!ignore) {
					boolean filtered = checkMessageFiltered(messageString);

					// message must not be set to ignore and not be filtered
					if (!filtered) {
						sendMessage(messageString);
					}
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

			if (enabled) {
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

		filterList = em.createQuery("select f from TelegramFilter f", TelegramFilter.class).getResultList();
	}

	private void registerBot() {

		try {
			String token = configurationService.getConfigurationProperty(TELEGRAM, TOKEN);
			String user = configurationService.getConfigurationProperty(TELEGRAM, USER);
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
