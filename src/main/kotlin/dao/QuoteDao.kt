package dao

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
                .map { Quote.wrapRow(it) }
                .forEach(quotesList::add)
            commit()
        }
        return quotesList
    }

    fun update(id: Long, text: String? = null, notificationTime: String? = null, notificationEnabled: Boolean? = null) {
        val quote = findById(id)
        if (quote != null) {
            transaction {
                if (text != null) {
                    quote.text = text
                }
                if (notificationTime != null) {
                    quote.notificationTime = notificationTime
                }
                if (notificationEnabled != null) {
                    quote.notificationEnabled = notificationEnabled
                }
                commit()
            }
        }
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
        val quotesDto = findAllByChatId(chatId)
        val lastQuoteDto = quotesDto.last()
        val quote = findById(lastQuoteDto.id.value)
        if (quote != null) {
            update(
                id = quote.id.value,
                notificationTime = LocalTime.parse(notificationTime).truncatedTo(ChronoUnit.MINUTES).toString(),
                notificationEnabled = true,
            )
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