package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final TelegramBot bot;
    private final NotificationTaskRepository repository;

    public TelegramBotUpdatesListener(TelegramBot bot, NotificationTaskRepository repository) {
        this.bot = bot;
        this.repository = repository;
        bot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() != null) {
                String text = update.message().text();
                long chatId = update.message().chat().id();

                if ("/start".equals(text)) {
                    SendMessage message = new SendMessage(chatId, "Добро пожаловать в бот-напоминалку! Отправляйте напоминания в формате: ДД.ММ.ГГГГ ЧЧ:ММ Ваша задача");
                    bot.execute(message);
                } else {
                    Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.matches()) {
                        String dateTimeStr = matcher.group(1);
                        String taskText = matcher.group(3);
                        LocalDateTime notificationTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                        NotificationTask task = new NotificationTask(chatId, taskText, notificationTime);
                        repository.save(task);
                        SendMessage message = new SendMessage(chatId, "Напоминание сохранено!");
                        bot.execute(message);
                    } else {
                        SendMessage message = new SendMessage(chatId, "Неверный формат! Используйте: ДД.ММ.ГГГГ ЧЧ:ММ Ваша задача");
                        bot.execute(message);
                    }
                }
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}