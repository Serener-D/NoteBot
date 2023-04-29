package service

import ConversationState
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import dao.QuoteDao
import dao.UserDao
import dto.QuoteDto
import dto.UserDto
import userStates
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

object MessageHandler {

    fun handleMessage(message: Message, bot: Bot) {
        var messageToUser: String
        if (userStates[message.chat.id] == null) {
            QuoteDao.create(QuoteDto(text = message.text.toString(), userDto = UserDto(chatId = message.chat.id)))
            userStates[message.chat.id] = ConversationState.WAITING_NOTIFICATION_TIME
            messageToUser = "Quote saved. Enter notification time, ex: 9:00"
        } else {
            try {
                QuoteDao.updateNotificationTimeForLastAddedQuote(
                    message.chat.id,
                    validateNotificationTime(message).toString()
                )
                messageToUser = "Notification set"
                userStates.remove(message.chat.id)
            } catch (e: Exception) {
                messageToUser = "Enter valid notification time, ex: 9:00";
                e.printStackTrace()
                userStates[message.chat.id] = ConversationState.WAITING_NOTIFICATION_TIME
            }
        }
        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = messageToUser)
    }

    private fun validateNotificationTime(message: Message): LocalTime {
        var quoteTime = LocalTime.parse(message.text, DateTimeFormatter.ofPattern("H:mm")).truncatedTo(ChronoUnit.MINUTES)

        var userTimeZoneOffset = UserDao.findByChatId(message.chat.id)?.timeZoneOffset?.toLong()
        if (userTimeZoneOffset == null) {
            userTimeZoneOffset = 0;
        }

        val serverTimeZoneOffset = TimeZone.getDefault().rawOffset / (60 * 60 * 1000);

        if (userTimeZoneOffset > serverTimeZoneOffset) {
            quoteTime = quoteTime.minusHours(userTimeZoneOffset - serverTimeZoneOffset)
        } else if (userTimeZoneOffset < serverTimeZoneOffset) {
            quoteTime = quoteTime.plusHours(serverTimeZoneOffset - userTimeZoneOffset)
        }
        return quoteTime
    }

}