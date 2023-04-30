package service

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import dao.NoteDao.findAllByNotificationTime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalTime
import java.time.temporal.ChronoUnit

object NotificationScheduler {

    fun checkNotificationTime(bot: Bot) {
        while (true) {
            val currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
            val notes = findAllByNotificationTime(currentTime.toString())
            for (note in notes) {
                transaction {
                    bot.sendMessage(ChatId.fromId(note.user.chatId), note.text)
                }
            }
            takeTimeOut(currentTime);
        }
    }

    private fun takeTimeOut(timeBeforeNotification: LocalTime) {
        val currentTimeAfterNotification = LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toSecondOfDay()
        val sleepTimeAfterNotification =
            (timeBeforeNotification.toSecondOfDay() + 60 - currentTimeAfterNotification) * 1000
        try {
            Thread.sleep(sleepTimeAfterNotification.toLong());
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }

}