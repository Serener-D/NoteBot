package dto

import entity.Quote
import java.time.LocalTime

data class QuoteDto(
    var id: Long? = null,
    val text: String? = null,
    val notificationTime: String? = null,
    val notificationEnabled: Boolean? = false,
    // FIXME I can use just chatID here
    val userDto: UserDto? = null
)

fun convertToDto(quote: Quote): QuoteDto {
    var notificationTime = ""
    if (quote.notificationTime != null) {
        notificationTime = LocalTime.parse(quote.notificationTime).toString()
    }
    return QuoteDto(
        quote.id.value,
        quote.text,
        notificationTime,
        quote.notificationEnabled,
        UserDto(quote.user.chatId, quote.user.timeZoneOffset)
    )

}
