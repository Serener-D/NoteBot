package entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Quote(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Quote>(QuoteTable)

    var user by User referencedOn QuoteTable.user
    var text by QuoteTable.text
    var notificationTime by QuoteTable.notificationTime
    var notificationEnabled by QuoteTable.notificationEnabled
}