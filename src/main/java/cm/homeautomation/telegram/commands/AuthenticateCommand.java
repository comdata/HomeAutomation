package cm.homeautomation.telegram.commands;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import cm.homeautomation.configuration.ConfigurationService;

import cm.homeautomation.entities.TelegramUser;

public class AuthenticateCommand extends BotCommand {

	private static final String LOGTAG = "AUTHENTICATECOMMAND";
	private String authSecret;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	public AuthenticateCommand() {
		super("authenticate", "Authenticate using a token");

		authSecret = configurationService.getConfigurationProperty("telegram", "auth-secret");
	}

	@Override
	
	public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

		String userId = user.getId().toString();

		SendMessage answer = new SendMessage();
		answer.setChatId(chat.getId().toString());

		LogManager.getLogger(this.getClass()).info("Request from user: {}", String.valueOf(user.getId()));

		if (arguments != null && arguments.length == 1) {

			if (authSecret.equals(arguments[0])) {

				answer.setText("user authenticated: " + user.getId());

				List<TelegramUser> result = em
						.createQuery("select u from TelegramUser u where u.userId=:userId", TelegramUser.class)
						.setParameter("userId", userId).getResultList();

				if (result == null || result.isEmpty()) {

					TelegramUser telegramUser = new TelegramUser();

					telegramUser.setUserId(userId);
					telegramUser.setAuthenticated(true);

					em.persist(telegramUser);

				}

			} else {
				answer.setText("wrong credentials");
			}

		} else {
			answer.setText("not authenticated");
		}

		try {
			absSender.sendMessage(answer);
		} catch (TelegramApiException e) {
			BotLogger.error(LOGTAG, e);
		}

	}

}
