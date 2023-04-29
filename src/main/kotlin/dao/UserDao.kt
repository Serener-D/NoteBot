package dao

import dto.UserDto
import entity.User
import entity.UserTable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object UserDao {

    fun findByChatId(chatId: Long): User? {
        var user: User? = null
        transaction {
            user = User.find { UserTable.chatId eq chatId }.firstOrNull()
            commit()
        }
        return user
    }

    fun create(userDto: UserDto) {
        transaction {
            User.new {
                chatId = userDto.chatId;
                timeZoneOffset = Optional.ofNullable(userDto.timeZoneOffset).orElse("0")
            }
            commit()
        }
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