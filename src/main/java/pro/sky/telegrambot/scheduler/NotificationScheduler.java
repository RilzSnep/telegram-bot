package pro.sky.telegrambot.scheduler;

import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@EnableScheduling
public class NotificationScheduler {
    private final NotificationTaskRepository repository;
    private final TelegramBot bot;

    public NotificationScheduler(NotificationTaskRepository repository, TelegramBot bot) {
        this.repository = repository;
        this.bot = bot;
    }

    @Scheduled(cron = "0 0/1 * * * *") // Каждую минуту
    public void checkNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = repository.findByNotificationTime(now);
        for (NotificationTask task : tasks) {
            SendMessage message = new SendMessage(task.getChatId(), task.getTaskText());
            bot.execute(message);
            repository.delete(task); // Удаляем задачу после отправки
        }
    }
}