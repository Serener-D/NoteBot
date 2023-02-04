package service.command

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import dao.QuoteDao
import service.callbackhandler.GetCallbackHandler

object GetQuotesCommand : Command {

    override fun execute(message: Message, bot: Bot) {
        val quotes = QuoteDao.findAllByChatId(message.chat.id)
        val buttonsList = ArrayList<List<InlineKeyboardButton>>()
        for (quote in quotes) {
            buttonsList.add(
                listOf<InlineKeyboardButton>(
                    InlineKeyboardButton.CallbackData(
                        text = quote.text.orEmpty(),
                        callbackData = GetCallbackHandler.getQueryName() + " " + quote.id.toString()
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

}