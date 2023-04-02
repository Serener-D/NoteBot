package entity

import org.jetbrains.exposed.dao.id.LongIdTable

object UserTable : LongIdTable("user") {
    // TODO I can use chatId as a primary key
    val chatId = long("chat_id").uniqueIndex()
    val timeZoneOffset = varchar("time_zone_offset", 3).default("0")
}