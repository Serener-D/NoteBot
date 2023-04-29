package entity

import org.jetbrains.exposed.dao.id.LongIdTable

object UserTable : LongIdTable("user") {
    val chatId = long("chat_id").uniqueIndex()
    val timeZoneOffset = varchar("time_zone_offset", 3).default("0")
}