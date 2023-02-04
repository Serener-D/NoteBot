package service.command

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import dao.QuoteDao
import dto.QuoteDto

object DisableCommand : Command {
    override fun execute(message: Message, bot: Bot) {
        val quotes = QuoteDao.findAllByChatId(message.chat.id)
        for (quote in quotes) {
            QuoteDao.update(QuoteDto(id = quote.id, notificationEnabled = false))
        }
        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "All notifications disabled")
    }
}