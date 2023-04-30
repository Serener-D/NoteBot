package service.callbackhandler

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import dao.NoteDao

object GetCallbackHandler : CallbackHandler {

    private const val QUERY_NAME = "GET"

    override fun handle(callbackQuery: CallbackQuery, bot: Bot) {
        val id = callbackQuery.data.split(" ")[1]
        val chatId = callbackQuery.message?.chat?.id
        val note = NoteDao.findById(id.toLong())
        val buttonsList = ArrayList<InlineKeyboardButton>()
        buttonsList.add(
            InlineKeyboardButton.CallbackData(
                text = "Delete",
                callbackData = DeleteCallbackHandler.getQueryName() + " " + id
            )
        )
        buttonsList.add(
            InlineKeyboardButton.CallbackData(
                text = "Set notification time",
                callbackData = SetNotificationCallbackHandler.getQueryName() + " " + id
            )
        )
        if (chatId != null) {
            bot.sendMessage(
                ChatId.fromId(chatId),
                note?.text.orEmpty(),
                replyMarkup = InlineKeyboardMarkup.create(buttonsList)
            )
        }
    }

    override fun getQueryName(): String {
        return QUERY_NAME
    }

}