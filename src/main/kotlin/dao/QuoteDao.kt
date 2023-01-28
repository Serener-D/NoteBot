package dao

import dto.QuoteDto
import dto.convertToDto
import entity.Quote
import entity.QuoteTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalTime
import java.time.temporal.ChronoUnit

object QuoteDao {
    init {
        Database.connect(url = "jdbc:sqlite:mentor.db", driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(QuoteTable)
            commit()
        }
    }

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
            val quotes = Quote.find {
                QuoteTable.chatId eq chatId
            }
            for (quote in quotes) {
                quotesList.add(convertToDto(quote))
            }
            commit()
        }
        return quotesList
    }

    fun findAllWhereNotificationTime(notificationTime: String): List<QuoteDto> {
        val quotesList = ArrayList<QuoteDto>()
        transaction {
            val quotes = Quote.find {
                QuoteTable.notificationTime eq notificationTime
            }
            for (quote in quotes) {
                quotesList.add(convertToDto(quote))
            }
            commit()
        }
        return quotesList
    }

    fun create(quoteDto: QuoteDto) {
        transaction {
            if (quoteDto.chatId != null && quoteDto.text != null) {
                Quote.new {
                    chatId = quoteDto.chatId
                    text = quoteDto.text.toString()
                }
            }
            commit()
        }
    }

    fun update(quoteDto: QuoteDto) {
        transaction {
            val quote = quoteDto.id?.let { findById(it) }
            if (quoteDto.chatId != null) {
                quote?.chatId = quoteDto.chatId
            }
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

    fun updateNotificationTimeForLastAddedQuote(chatId: Long, notificationTime: String) {
        val quotesDto = findAllByChatId(chatId)
        val lastQuoteDto = quotesDto.last()
        val quote = lastQuoteDto.id?.let { findById(it) }
        if (quote != null) {
            transaction {
                try {
                    quote.notificationTime =
                        LocalTime.parse(notificationTime).truncatedTo(ChronoUnit.MINUTES).toString()
                } catch (_: Exception) {
                }
                commit()
            }
        }
    }

    fun delete(quoteDto: QuoteDto) {
        val quote = quoteDto.id?.let { findById(it) }
        quote?.delete()
    }

}