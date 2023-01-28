package entity

import org.jetbrains.exposed.dao.id.LongIdTable

object QuoteTable: LongIdTable("quote") {
    val chatId = long( "chatId").index()
    val text = varchar("text", 2000)
    val notificationTime = varchar("notification_time", 8).nullable().index()
    val notificationEnabled = bool("notification_enabled").default(true)
}