package service.callbackhandler

import ConversationState
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import dao.NoteDao
import userStates

object SetNotificationCallbackHandler : CallbackHandler {

    private const val QUERY_NAME = "SET_NOTIFICATION"

    override fun handle(callbackQuery: CallbackQuery, bot: Bot) {
        val id = callbackQuery.data.split(" ")[1]
        val chatId = callbackQuery.message?.chat?.id
        val note = NoteDao.findById(id.toLong())
        if (chatId != null && note != null) {
            NoteDao.delete(id.toLong())
            NoteDao.create(text = note.text, chatId = chatId)
            userStates[chatId] = ConversationState.WAITING_NOTIFICATION_TIME
            bot.sendMessage(chatId = ChatId.fromId(chatId), text = "Enter notification time, ex: 16:00")
        }
    }

    override fun getQueryName(): String {
        return QUERY_NAME
    }

}