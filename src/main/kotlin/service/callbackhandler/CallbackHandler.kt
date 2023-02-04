package service.callbackhandler

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery

interface CallbackHandler {

    fun handle(callbackQuery: CallbackQuery, bot: Bot)

    fun getQueryName(): String
}