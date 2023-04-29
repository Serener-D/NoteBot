package dao

import entity.User
import entity.UserTable
import org.jetbrains.exposed.sql.transactions.transaction

object UserDao {

    fun findByChatId(chatId: Long): User? {
        var user: User? = null
        transaction {
            user = User.find { UserTable.chatId eq chatId }.firstOrNull()
            commit()
        }
        return user
    }

    fun create(chatId: Long, timeZoneOffset: String) {
        transaction {
            User.new {
                this.chatId = chatId
                this.timeZoneOffset = timeZoneOffset
            }
            commit()
        }
    }

    fun update(chatId: Long, timeZoneOffset: String) {
        transaction {
            val user = findByChatId(chatId)
            if (user != null) {
                user.timeZoneOffset = timeZoneOffset
            }
            commit()
        }
    }

}