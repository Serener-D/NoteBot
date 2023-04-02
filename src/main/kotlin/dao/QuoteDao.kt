package dao

import dto.QuoteDto
import dto.UserDto
import entity.Quote
import entity.QuoteTable
import entity.User
import entity.UserTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalTime
import java.time.temporal.ChronoUnit

object QuoteDao {

    fun findById(id: Long): Quote? {
        var quote: Quote? = null
        transaction {
            quote = Quote.findById(id)
            commit()
        }
        return quote
    }

    fun findAllByChatId(chatId: Long): List<QuoteDto> {
        val quotesList = ArrayList<QuoteDto>()
        transaction {
            QuoteTable.innerJoin(UserTable)
                .select(UserTable.chatId eq chatId)
                .map {
                    QuoteDto(
                        it[QuoteTable.id].value,
                        it[QuoteTable.text],
                        it[QuoteTable.notificationTime],
                        it[QuoteTable.notificationEnabled],
                        UserDto(it[UserTable.chatId], it[UserTable.timeZoneOffset])
                    )
                }
                .forEach { quotesList.add(it) }
            commit()
        }
        return quotesList
    }

    fun update(quoteDto: QuoteDto) {
        val quote = quoteDto.id?.let { findById(it) }
        transaction {
            if (quoteDto.text != null) {
                quote?.text = quoteDto.text
            }
            if (quoteDto.notificationTime != null) {
                quote?.notificationTime = quoteDto.notificationTime
            }
            if (quoteDto.notificationEnabled != null) {
                quote?.notificationEnabled = quoteDto.notificationEnabled
            }
            commit()
        }
    }

    // FIXME doesn't look good. client should know, that he has to provide chatId
    fun create(quoteDto: QuoteDto) {
        if (quoteDto.userDto?.chatId != null) {
            transaction {
                var savedUser = UserDao.findByChatId(quoteDto.userDto.chatId)
                if (savedUser == null) {
                    savedUser = User.new { chatId = quoteDto.userDto.chatId }
                }
                Quote.new {
                    user = savedUser
                    text = quoteDto.text.toString()
                }
                commit()
            }
        }
    }

    fun updateNotificationTimeForLastAddedQuote(chatId: Long, notificationTime: String) {
        val quotesDto = findAllByChatId(chatId)
        val lastQuoteDto = quotesDto.last()
        val quote = lastQuoteDto.id?.let { findById(it) }
        if (quote != null) {
            val quoteDto = QuoteDto(
                id = quote.id.value,
                notificationTime = LocalTime.parse(notificationTime).truncatedTo(ChronoUnit.MINUTES).toString(),
                notificationEnabled = true
            )
            update(quoteDto)
        }
    }

    fun delete(quoteDto: QuoteDto) {
        val quote = quoteDto.id?.let { findById(it) }
        transaction {
            quote?.delete()
            commit()
        }
    }

}