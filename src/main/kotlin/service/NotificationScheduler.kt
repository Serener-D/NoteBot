package service

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import dao.QuoteDao
import java.time.LocalTime
import java.time.temporal.ChronoUnit

object NotificationScheduler {

    fun checkNotificationTime(bot: Bot) {
        while (true) {
            val currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
            val quotes = QuoteDao.findAllWhereNotificationTime(currentTime.toString())
            for (quote in quotes) {
                if (quote.chatId != null && quote.text != null) {
                    bot.sendMessage(ChatId.fromId(quote.chatId), quote.text)
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