package dto

data class UserDto(
    val chatId: Long,
    val timeZoneOffset: String? = null,
)