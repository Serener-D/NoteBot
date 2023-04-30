package dao

import entity.Quote
import entity.QuoteTable
import entity.User
import entity.UserTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
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

    fun findAllByChatId(chatId: Long): List<Quote> {
        val quotesList = ArrayList<Quote>()
        transaction {
            QuoteTable.innerJoin(UserTable)
                .select(UserTable.chatId eq chatId)
                .map { Quote.wrapRow(it) }
                .forEach(quotesList::add)
            commit()
        }
        return quotesList
    }

    fun findAllByNotificationTime(notificationTime: String): List<Quote> {
        val quotesList = ArrayList<Quote>()
        transaction {
            QuoteTable
                .select(QuoteTable.notificationTime eq notificationTime)
                .andWhere { QuoteTable.notificationEnabled eq true }
                .map { Quote.wrapRow(it) }
                .forEach(quotesList::add)
            commit()
        }
        return quotesList
    }

    fun create(chatId: Long, text: String) {
        transaction {
            var user = UserDao.findByChatId(chatId)
            if (user == null) {
                user = User.new { this.chatId = chatId }
            }
            Quote.new {
                this.user = user
                this.text = text
            }
            commit()
        }
    }

    fun updateNotificationTimeForLastAddedQuote(chatId: Long, notificationTime: String) {
        transaction {
            val quote = Quote.wrapRow(
                QuoteTable.innerJoin(UserTable)
                    .select(UserTable.chatId eq chatId)
                    .last()
            )
            quote.notificationEnabled = true
            quote.notificationTime = LocalTime.parse(notificationTime).truncatedTo(ChronoUnit.MINUTES).toString()
            commit()
        }
    }

    fun delete(id: Long) {
        val quote = findById(id)
        transaction {
            quote?.delete()
            commit()
        }
    }

}