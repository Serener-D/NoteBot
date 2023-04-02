package dao

import dto.QuoteDto
import dto.UserDto
import entity.User
import entity.UserTable
import org.jetbrains.exposed.sql.transactions.transaction

object UserDao {

    fun findById(id: Long): User? {
        var user: User? = null
        transaction {
            user = User.findById(id)
            commit()
        }
        return user
    }

    fun findByChatId(chatId: Long): User? {
        var user: User? = null
        transaction {
            user = User.find { UserTable.chatId eq chatId }.firstOrNull()
            commit()
        }
        return user
    }

    fun update(userDto: UserDto) {
        transaction {
            val user = findByChatId(userDto.chatId)
            if (userDto.timeZoneOffset != null) {
                user?.timeZoneOffset = userDto.timeZoneOffset
            }
            commit()
        }
    }

}