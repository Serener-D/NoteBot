import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.extensions.filters.Filter
import entity.QuoteTable
import entity.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import service.MessageHandler
import service.NotificationScheduler.checkNotificationTime
import service.callbackhandler.DeleteCallbackHandler
import service.callbackhandler.GetCallbackHandler
import service.callbackhandler.SetNotificationCallbackHandler
import service.callbackhandler.TimezoneCallbackHandler
import service.command.DisableCommand
import service.command.GetQuotesCommand
import service.command.TimezoneCommand
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
            MessageHandler.handleMessage(message, bot)
        }
    }
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