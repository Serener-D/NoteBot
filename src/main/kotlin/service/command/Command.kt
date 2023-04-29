package service.command

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message

interface Command {

    fun execute(message: Message, bot: Bot)

    fun getCommandName(): String
}