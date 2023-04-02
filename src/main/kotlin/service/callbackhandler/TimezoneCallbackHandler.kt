package service.callbackhandler

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import dao.UserDao
import dto.UserDto

object TimezoneCallbackHandler : CallbackHandler {

    private const val QUERY_NAME = "TIMEZONE"

    override fun handle(callbackQuery: CallbackQuery, bot: Bot) {
        val chatId = callbackQuery.message?.chat?.id
        if (chatId != null) {
            val timeZone = callbackQuery.data.split(" ")[1]
            UserDao.update(UserDto(chatId = chatId, timeZoneOffset = timeZone))
            bot.sendMessage(chatId = ChatId.fromId(chatId), text = "Timezone set")
        }
    }

    override fun getQueryName(): String {
        return QUERY_NAME
    }


}