package cm.homeautomation.telegram;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import cm.homeautomation.telegram.commands.AuthenticateCommand;
import cm.homeautomation.telegram.commands.HelpCommand;
import cm.homeautomation.telegram.services.Emoji;

/**
 * This handler mainly works with commands to demonstrate the Commands feature
 * of the API
 *
 * @author Timo Schulz (Mit0x2)
 */
public class CommandsHandler extends TelegramLongPollingCommandBot {

	public static final String LOGTAG = "COMMANDSHANDLER";
	private String token;
	private String user;

	/**
	 * Constructor.
	 */
	public CommandsHandler(String user, String token) {
		this.user = user;
		this.token = token;
		register(new AuthenticateCommand());
		HelpCommand helpCommand = new HelpCommand(this);
		register(helpCommand);

		registerDefaultAction((absSender, message) -> {
			SendMessage commandUnknownMessage = new SendMessage();
			commandUnknownMessage.setChatId(message.getChatId());
			commandUnknownMessage.setText("The command '" + message.getText()
					+ "' is not known by this bot. Here comes some help " + Emoji.AMBULANCE);
			try {
				absSender.sendMessage(commandUnknownMessage);
			} catch (TelegramApiException e) {
				BotLogger.error(LOGTAG, e);
			}
			helpCommand.execute(absSender, message.getFrom(), message.getChat(), new String[] {});

		});
	}

	@Override
	public void processNonCommandUpdate(Update update) {

		if (update.hasMessage()) {
			Message message = update.getMessage();

			if (message.hasText()) {
				SendMessage echoMessage = new SendMessage();
				echoMessage.setChatId(message.getChatId());
				echoMessage.setText("Hey heres your message:\n" + message.getText());

				try {
					sendMessage(echoMessage);
				} catch (TelegramApiException e) {
					BotLogger.error(LOGTAG, e);
				}
			}
		}
	}

	@Override
	public String getBotUsername() {
		return user;
	}

	@Override
	public String getBotToken() {
		return token;
	}
}