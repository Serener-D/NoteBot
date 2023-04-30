import com.github.kotlintelegrambot.Bot
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
import java.util.*
import kotlin.concurrent.thread

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

fun main(args: Array<String>) {
    initDatabase()
    val bot = initBot(args[0])
    thread {
        checkNotificationTime(bot)
    }
    bot.startPolling()
}

private fun initDatabase() {
    Database.connect(url = "jdbc:sqlite:mentor.db", driver = "org.sqlite.JDBC")
    transaction {
        SchemaUtils.createMissingTablesAndColumns(QuoteTable, UserTable)
        commit()
    }
}

private fun initBot(token: String?): Bot {
    return bot {
        this.token = Optional.ofNullable(token).orElseThrow { RuntimeException("You should pass a botToken!") }
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
}

enum class ConversationState { WAITING_NOTIFICATION_TIME }