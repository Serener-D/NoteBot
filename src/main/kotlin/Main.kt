import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.extensions.filters.Filter
import dao.QuoteDao
import dto.QuoteDto

enum class BotState { IDLE, WAITING_NOTIFICATION_TIME }

// fixme надо периодическую очистку контекста сделать
private val userStates = mutableMapOf<Long, BotState>()

// fixme отрефактори - вынеси в отдельные меоды все
val bot = bot {
    token = "5406796718:AAEdyLsc53hjjhE-enRT1q7i4aAS3OaDoJo"
    dispatch {

        command("getquotes") {
            val quotes = QuoteDao.findAllByChatId(message.chat.id)
            val buttonsList = ArrayList<List<InlineKeyboardButton>>()
            for (quote in quotes) {
                buttonsList.add(
                    listOf<InlineKeyboardButton>(
                        InlineKeyboardButton.CallbackData(
                            text = quote.text.orEmpty(), callbackData = quote.id.toString()
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

        command("disable") {
            val quotes = QuoteDao.findAllByChatId(message.chat.id)
            for (quote in quotes) {
                QuoteDao.update(QuoteDto(id = quote.id, notificationEnabled = false))
            }
            bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "All notifications disabled")
        }

        message(!Filter.Command) {
            if (userStates[message.chat.id] == null || userStates[message.chat.id] == BotState.IDLE) {
                QuoteDao.create(QuoteDto(chatId = message.chat.id, text = message.text))
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Quote saved. Enter notification time, ex: 16:00"
                )
                userStates[message.chat.id] = BotState.WAITING_NOTIFICATION_TIME
            } else {
                QuoteDao.updateNotificationTimeForLastAddedQuote(message.chat.id, message.text.orEmpty())
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Notification set"
                )
                userStates[message.chat.id] = BotState.IDLE
            }
        }
    }
}.startPolling()


fun main() {
    TelegramBot
}