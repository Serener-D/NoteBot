package service

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import dto.QuoteDto
import dto.convertToDto
import entity.Quote
import entity.QuoteTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalTime
import java.time.temporal.ChronoUnit

object NotificationScheduler {

    fun checkNotificationTime(bot: Bot) {
        while (true) {
            val currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
            val quotes = findAllWhereNotificationTime(currentTime.toString())
            for (quote in quotes) {
                if (quote.userDto?.chatId != null && quote.text != null) {
                    bot.sendMessage(ChatId.fromId(quote.userDto.chatId), quote.text)
                }
            }
            takeTimeOut(currentTime);
        }
    }

    private fun findAllWhereNotificationTime(notificationTime: String): List<QuoteDto> {
        val quotesList = ArrayList<QuoteDto>()
        transaction {
            val quotes = Quote.find {
                QuoteTable.notificationTime eq notificationTime and
                        (QuoteTable.notificationEnabled eq true)
            }
            for (quote in quotes) {
                quotesList.add(convertToDto(quote))
            }
            commit()
        }
        return quotesList
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