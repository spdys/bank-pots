package banking.service.validation

import banking.PotsException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class KWCivilIDValidator {
    companion object {
        private val regex = Regex("^\\d{12}$")
        private val weights = listOf(2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)

        @Throws(PotsException::class)
        fun validate(civilId: String): String {
            if (!regex.matches(civilId)) {
                throw PotsException("Enter a valid Kuwaiti Civil ID number")
            }

            // Extract and build birthdate
            val centuryChar = civilId[0]
            val yy = civilId.substring(1, 3)
            val mm = civilId.substring(3, 5)
            val dd = civilId.substring(5, 7)

            val fullYear = when (centuryChar) {
                '2' -> "19$yy"
                '3' -> "20$yy"
                else -> throw PotsException("Invalid century digit in Civil ID")
            }

            try {
                LocalDate.parse("$fullYear-$mm-$dd", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: DateTimeParseException) {
                throw PotsException("Invalid birth date in Civil ID")
            }

            // Checksum validation
            var checksum = 0
            for (i in 0 until 11) {
                checksum += civilId[i].digitToInt() * weights[i]
            }

            val remainder = checksum % 11
            val checkDigit = 11 - remainder

            if (checkDigit != civilId[11].digitToInt()) {
                throw PotsException("Invalid Civil ID checksum") as Throwable
            }

            return civilId
        }
    }
}