package entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Note(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Note>(NoteTable)

    var user by User referencedOn NoteTable.user
    var text by NoteTable.text
    var notificationTime by NoteTable.notificationTime
    var notificationEnabled by NoteTable.notificationEnabled
}