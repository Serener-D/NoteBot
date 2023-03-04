import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.extensions.filters.Filter
import dao.QuoteDao
import dto.QuoteDto
import service.NotificationScheduler.checkNotificationTime
import service.callbackhandler.DeleteCallbackHandler
import service.callbackhandler.GetCallbackHandler
import service.callbackhandler.SetNotificationCallbackHandler
import service.command.DisableCommand
import service.command.GetQuotesCommand
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.concurrent.thread


enum class ConversationState { IDLE, WAITING_NOTIFICATION_TIME }

// fixme clear context after certain periods of time
val userStates = mutableMapOf<Long, ConversationState>()

// fixme make map for all handlers and commands
val bot = bot {
    token = "5406796718:AAEdyLsc53hjjhE-enRT1q7i4aAS3OaDoJo"
    dispatch {

        callbackQuery {
            val query = callbackQuery.data.split(" ")[0]

            if (GetCallbackHandler.getQueryName() == query) {
                GetCallbackHandler.handle(callbackQuery, bot)
            }
            if (DeleteCallbackHandler.getQueryName() == query) {
                DeleteCallbackHandler.handle(callbackQuery, bot)
            }
            if (SetNotificationCallbackHandler.getQueryName() == query) {
                SetNotificationCallbackHandler.handle(callbackQuery, bot)
            }
        }

        message(Filter.Command) {
            val command = message.text
            if ("/disable" == command) {
                DisableCommand.execute(message, bot)
            }
            if ("/getquotes" == command) {
                GetQuotesCommand.execute(message, bot)
            }
        }

        message(!Filter.Command) {
            var messageToUser: String
            if (userStates[message.chat.id] == null || userStates[message.chat.id] == ConversationState.IDLE) {
                saveQuote(message)
                messageToUser = "Quote saved. Enter notification time, ex: 9:00"
            } else {
                try {
                    saveNotificationTime(message)
                    messageToUser = "Notification set"
                    userStates[message.chat.id] = ConversationState.IDLE
                } catch (e: Exception) {
                    messageToUser = "Enter valid notification time, ex: 9:00";
                    e.printStackTrace()
                    userStates[message.chat.id] = ConversationState.WAITING_NOTIFICATION_TIME
                }
            }
            bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = messageToUser)
        }
    }
}

fun saveQuote(message: Message) {
    QuoteDao.create(QuoteDto(chatId = message.chat.id, text = message.text))
    userStates[message.chat.id] = ConversationState.WAITING_NOTIFICATION_TIME
}

fun saveNotificationTime(message: Message) {
    QuoteDao.updateNotificationTimeForLastAddedQuote(message.chat.id, validateNotificationTime(message).toString())
    userStates[message.chat.id] = ConversationState.IDLE

}

private fun validateNotificationTime(message: Message): LocalTime {
    var  quoteTime = LocalTime.parse(message.text, DateTimeFormatter.ofPattern("H:mm")).truncatedTo(ChronoUnit.MINUTES)
    println(quoteTime)

    val userLocalTime = LocalTime.ofInstant(Instant.ofEpochSecond(message.date), ZoneId.systemDefault())
    val serverLocalTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)

    if (userLocalTime.isAfter(serverLocalTime)) {
        quoteTime = quoteTime.plusHours((userLocalTime.hour - serverLocalTime.hour).toLong())
    } else if (userLocalTime.isBefore(serverLocalTime)) {
        quoteTime = quoteTime.minusHours((serverLocalTime.hour - userLocalTime.hour).toLong())
    }
    return quoteTime
}


fun main() {
    thread {
        checkNotificationTime(bot)
    }
    bot.startPolling()
}