package service.callbackhandler

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import dao.NoteDao

object DeleteCallbackHandler : CallbackHandler {

    private const val QUERY_NAME = "DELETE"

    override fun handle(callbackQuery: CallbackQuery, bot: Bot) {
        val id = callbackQuery.data.split(" ")[1]
        val chatId = callbackQuery.message?.chat?.id
        NoteDao.delete(id.toLong())
        if (chatId != null) {
            bot.sendMessage(chatId = ChatId.fromId(chatId), text = "Note deleted")
        }
    }

    override fun getQueryName(): String {
        return QUERY_NAME
    }

}