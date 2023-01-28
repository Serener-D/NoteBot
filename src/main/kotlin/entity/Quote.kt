package entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Quote(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Quote>(QuoteTable)
    var chatId by QuoteTable.chatId
    var text     by QuoteTable.text
    var notificationTime by QuoteTable.notificationTime
    var notificationEnabled by QuoteTable.notificationEnabled
}