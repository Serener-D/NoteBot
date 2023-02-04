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
import kotlin.concurrent.thread

enum class ConversationState { IDLE, WAITING_NOTIFICATION_TIME }

// fixme надо периодическую очистку контекста сделать
val userStates = mutableMapOf<Long, ConversationState>()

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
                // fixme баг!!! работает только для последней добавленной цитаты
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
            val messageToUser: String
            if (userStates[message.chat.id] == null || userStates[message.chat.id] == ConversationState.IDLE) {
                saveQuote(message)
                messageToUser = "Quote saved. Enter notification time, ex: 16:00"
            } else {
                saveNotificationTime(message)
                messageToUser = "Notification set"
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
    // fixme баг!!! работает только для последней добавленной цитаты
    QuoteDao.updateNotificationTimeForLastAddedQuote(message.chat.id, message.text.orEmpty())
    userStates[message.chat.id] = ConversationState.IDLE
}


fun main() {
    thread {
        checkNotificationTime(bot)
    }
    bot.startPolling()
}