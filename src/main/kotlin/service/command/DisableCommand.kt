package service.command

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import dao.QuoteDao

object DisableCommand : Command {

    private const val COMMAND_NAME = "/disable"

    override fun execute(message: Message, bot: Bot) {
        val quotes = QuoteDao.findAllByChatId(message.chat.id)
        for (quote in quotes) {
            QuoteDao.update(id = quote.id.value, notificationEnabled = false)
        }
        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "All notifications disabled")
    }

    override fun getCommandName(): String {
        return COMMAND_NAME
    }
}