import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.extensions.filters.Filter
import dao.QuoteDao
import dao.UserDao
import dto.QuoteDto
import dto.UserDto
import entity.QuoteTable
import entity.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import service.NotificationScheduler.checkNotificationTime
import service.callbackhandler.DeleteCallbackHandler
import service.callbackhandler.GetCallbackHandler
import service.callbackhandler.SetNotificationCallbackHandler
import service.callbackhandler.TimezoneCallbackHandler
import service.command.DisableCommand
import service.command.GetQuotesCommand
import service.command.TimezoneCommand
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.concurrent.thread


enum class ConversationState { WAITING_NOTIFICATION_TIME }

val userStates = mutableMapOf<Long, ConversationState>()

val callbackMap = mapOf(
    Pair(GetCallbackHandler.getQueryName(), GetCallbackHandler),
    Pair(DeleteCallbackHandler.getQueryName(), DeleteCallbackHandler),
    Pair(SetNotificationCallbackHandler.getQueryName(), SetNotificationCallbackHandler),
    Pair(TimezoneCallbackHandler.getQueryName(), TimezoneCallbackHandler)
)
val commandMap = mapOf(
    Pair(DisableCommand.getCommandName(), DisableCommand),
    Pair(GetQuotesCommand.getCommandName(), GetQuotesCommand),
    Pair(TimezoneCommand.getCommandName(), TimezoneCommand)
)

val bot = bot {
    token = "5406796718:AAEdyLsc53hjjhE-enRT1q7i4aAS3OaDoJo"
    dispatch {
        callbackQuery {
            val query = callbackQuery.data.split(" ")[0]
            callbackMap[query]?.handle(callbackQuery, bot)
        }
        message(Filter.Command) {
            val command = message.text
            commandMap[command]?.execute(message, bot)
        }
        message(!Filter.Command) {
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
    }
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

fun main() {
    Database.connect(url = "jdbc:sqlite:mentor.db", driver = "org.sqlite.JDBC")
    transaction {
        SchemaUtils.createMissingTablesAndColumns(QuoteTable, UserTable)
        commit()
    }
    thread {
        checkNotificationTime(bot)
    }
    bot.startPolling()
}