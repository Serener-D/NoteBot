import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.extensions.filters.Filter
import dao.QuoteDao
import dto.QuoteDto
import service.NotificationScheduler.checkNotificationTime
import kotlin.concurrent.thread

enum class ConversationState { IDLE, WAITING_NOTIFICATION_TIME }
enum class СallbackQuery { GET, DELETE, SET_NOTIFICATION }

// fixme надо периодическую очистку контекста сделать
private val userStates = mutableMapOf<Long, ConversationState>()

val bot = bot {
    token = "5406796718:AAEdyLsc53hjjhE-enRT1q7i4aAS3OaDoJo"
    dispatch {
        callbackQuery {
            // fixme отрефактори эти ифы chatId != null
            val argument = callbackQuery.data.split(" ")[0]
            val id = callbackQuery.data.split(" ")[1]
            val chatId = callbackQuery.message?.chat?.id

            if (СallbackQuery.GET.name == argument) {
                val quote = QuoteDao.findById(id.toLong())
                val buttonsList = ArrayList<InlineKeyboardButton>()
                buttonsList.add(
                    InlineKeyboardButton.CallbackData(
                        text = "Delete",
                        callbackData = СallbackQuery.DELETE.name + " " + id
                    )
                )
                buttonsList.add(
                    InlineKeyboardButton.CallbackData(
                        text = "Set notification time",
                        callbackData = СallbackQuery.SET_NOTIFICATION.name + " " + id
                    )
                )
                if (chatId != null) {
                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        quote?.text.orEmpty(),
                        replyMarkup = InlineKeyboardMarkup.create(buttonsList)
                    )
                }

            }
            if (СallbackQuery.DELETE.name == argument) {
                QuoteDao.delete(QuoteDto(id = id.toLong()))
                if (chatId != null) {
                    bot.sendMessage(chatId = ChatId.fromId(chatId), text = "Quote deleted")
                }
            }
            if (СallbackQuery.SET_NOTIFICATION.name == argument) {
                if (chatId != null) {
                    userStates[chatId] = ConversationState.WAITING_NOTIFICATION_TIME
                    bot.sendMessage(chatId = ChatId.fromId(chatId), text = "Enter notification time, ex: 16:00")
                }
            }
        }

        message(Filter.Command) {
            val command = message.text
            if ("/disable" == command) {
                val quotes = QuoteDao.findAllByChatId(message.chat.id)
                for (quote in quotes) {
                    QuoteDao.update(QuoteDto(id = quote.id, notificationEnabled = false))
                }
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "All notifications disabled")
            }
            if ("/getquotes" == command) {
                val quotes = QuoteDao.findAllByChatId(message.chat.id)
                val buttonsList = ArrayList<List<InlineKeyboardButton>>()
                for (quote in quotes) {
                    buttonsList.add(
                        listOf<InlineKeyboardButton>(
                            InlineKeyboardButton.CallbackData(
                                text = quote.text.orEmpty(),
                                callbackData = СallbackQuery.GET.name + " " + quote.id.toString()
                            )
                        )
                    )
                }
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Saved quotes",
                    replyMarkup = InlineKeyboardMarkup.create(buttonsList)
                )
            }
        }

        message(!Filter.Command) {
            val messageToUser: String
            if (userStates[message.chat.id] == null || userStates[message.chat.id] == ConversationState.IDLE) {
                QuoteDao.create(QuoteDto(chatId = message.chat.id, text = message.text))
                messageToUser = "Quote saved. Enter notification time, ex: 16:00"
                userStates[message.chat.id] = ConversationState.WAITING_NOTIFICATION_TIME
            } else {
                // fixme надо еще флажок на true
                QuoteDao.updateNotificationTimeForLastAddedQuote(message.chat.id, message.text.orEmpty())
                messageToUser = "Notification set"
                userStates[message.chat.id] = ConversationState.IDLE
            }
            bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = messageToUser)
        }
    }
}


fun main() {
    thread {
        checkNotificationTime(bot)
    }
    bot.startPolling()
}