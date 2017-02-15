package cm.homeautomation.telegram.commands;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

public class AuthenticateCommand extends BotCommand {

	private static final String LOGTAG = "AUTHENTICATECOMMAND";

	public AuthenticateCommand() {
		super("authenticate", "Authenticate using a token");
	}

	@Override
	public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage answer = new SendMessage();
        answer.setChatId(chat.getId().toString());
        answer.setText("not authenticated");

        try {
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
        
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        System.out.println(String.valueOf(user.getId()));
        sendMessage.setChatId(String.valueOf(user.getId()));
        sendMessage.setText("Test");
        try {
        absSender.sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
	}

}
