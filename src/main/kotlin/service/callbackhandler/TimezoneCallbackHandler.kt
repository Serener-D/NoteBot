package service.callbackhandler

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import dao.UserDao
import org.jetbrains.exposed.sql.transactions.transaction

object TimezoneCallbackHandler : CallbackHandler {

    private const val QUERY_NAME = "TIMEZONE"

    override fun handle(callbackQuery: CallbackQuery, bot: Bot) {
        val userChatId = callbackQuery.message?.chat?.id
        if (userChatId != null) {
            val timeZone = callbackQuery.data.split(" ")[1]

            val user = UserDao.findByChatId(userChatId)
            if (user == null) {
                UserDao.create(userChatId, timeZone)
            } else {
                transaction {
                    user.timeZoneOffset = timeZone
                    commit()
                }
            }

            bot.sendMessage(chatId = ChatId.fromId(userChatId), text = "Timezone set")
        }
    }

    override fun getQueryName(): String {
        return QUERY_NAME
    }

}