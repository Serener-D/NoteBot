package service.command

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import dao.NoteDao
import org.jetbrains.exposed.sql.transactions.transaction

object DisableCommand : Command {

    private const val COMMAND_NAME = "/disable"

    override fun execute(message: Message, bot: Bot) {
        val notes = NoteDao.findAllByChatId(message.chat.id)
        transaction {
            for (note in notes) {
                note.notificationEnabled = false
            }
            commit()
        }
        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "All notifications disabled")
    }

    override fun getCommandName(): String {
        return COMMAND_NAME
    }
}