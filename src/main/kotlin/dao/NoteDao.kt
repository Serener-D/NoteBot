package dao

import entity.Note
import entity.NoteTable
import entity.User
import entity.UserTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalTime
import java.time.temporal.ChronoUnit

object NoteDao {

    fun findById(id: Long): Note? {
        var note: Note? = null
        transaction {
            note = Note.findById(id)
            commit()
        }
        return note
    }

    fun findAllByChatId(chatId: Long): List<Note> {
        val notesList = ArrayList<Note>()
        transaction {
            NoteTable.innerJoin(UserTable)
                .select(UserTable.chatId eq chatId)
                .map { Note.wrapRow(it) }
                .forEach(notesList::add)
            commit()
        }
        return notesList
    }

    fun findAllByNotificationTime(notificationTime: String): List<Note> {
        val notesList = ArrayList<Note>()
        transaction {
            NoteTable
                .select(NoteTable.notificationTime eq notificationTime)
                .andWhere { NoteTable.notificationEnabled eq true }
                .map { Note.wrapRow(it) }
                .forEach(notesList::add)
            commit()
        }
        return notesList
    }

    fun create(chatId: Long, text: String) {
        transaction {
            var user = UserDao.findByChatId(chatId)
            if (user == null) {
                user = User.new { this.chatId = chatId }
            }
            Note.new {
                this.user = user
                this.text = text
            }
            commit()
        }
    }

    fun updateNotificationTimeForLastAddedNote(chatId: Long, notificationTime: String) {
        transaction {
            val note = Note.wrapRow(
                NoteTable.innerJoin(UserTable)
                    .select(UserTable.chatId eq chatId)
                    .last()
            )
            note.notificationEnabled = true
            note.notificationTime = LocalTime.parse(notificationTime).truncatedTo(ChronoUnit.MINUTES).toString()
            commit()
        }
    }

    fun delete(id: Long) {
        val note = findById(id)
        transaction {
            note?.delete()
            commit()
        }
    }

}