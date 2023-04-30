package service

import ConversationState
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import dao.NoteDao
import dao.UserDao
import userStates
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

object MessageHandler {

    fun handleMessage(message: Message, bot: Bot) {
        var messageToUser: String
        if (userStates[message.chat.id] == null) {
            NoteDao.create(text = message.text.toString(), chatId = message.chat.id)
            userStates[message.chat.id] = ConversationState.WAITING_NOTIFICATION_TIME
            messageToUser = "Note saved. Enter notification time, ex: 9:00"
        } else {
            try {
                NoteDao.updateNotificationTimeForLastAddedNote(
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
        var noteTime = LocalTime
            .parse(message.text, DateTimeFormatter.ofPattern("H:mm"))
            .truncatedTo(ChronoUnit.MINUTES)

        var userTimeZoneOffset = UserDao.findByChatId(message.chat.id)?.timeZoneOffset?.toLong()
        if (userTimeZoneOffset == null) {
            userTimeZoneOffset = 0;
        }

        val serverTimeZoneOffset = TimeZone.getDefault().rawOffset / (60 * 60 * 1000);

        if (userTimeZoneOffset > serverTimeZoneOffset) {
            noteTime = noteTime.minusHours(userTimeZoneOffset - serverTimeZoneOffset)
        } else if (userTimeZoneOffset < serverTimeZoneOffset) {
            noteTime = noteTime.plusHours(serverTimeZoneOffset - userTimeZoneOffset)
        }
        return noteTime
    }

}