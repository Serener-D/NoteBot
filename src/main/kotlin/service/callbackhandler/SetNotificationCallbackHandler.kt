package service.callbackhandler

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import userStates

object SetNotificationCallbackHandler : CallbackHandler {

    private const val QUERY_NAME = "SET_NOTIFICATION"

    // fixme баг!!! работает только для последней добавленной цитаты
    override fun handle(callbackQuery: CallbackQuery, bot: Bot) {
        val id = callbackQuery.data.split(" ")[1]
        val chatId = callbackQuery.message?.chat?.id
        if (chatId != null) {
            userStates[chatId] = ConversationState.WAITING_NOTIFICATION_TIME
            bot.sendMessage(chatId = ChatId.fromId(chatId), text = "Enter notification time, ex: 16:00")
        }
    }

    override fun getQueryName(): String {
        return QUERY_NAME
    }

}