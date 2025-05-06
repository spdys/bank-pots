package banking.service.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DateTimeValidator : ConstraintValidator<DateTimeValid, String> {

    private var format: String = "dd-MM-yyyy"

    override fun initialize(constraintAnnotation: DateTimeValid) {
        format = constraintAnnotation.format
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null || value.isBlank()) {
            return true
        }

        val formatter = DateTimeFormatter.ofPattern(format)
        return try {
            formatter.parse(value)
            true
        } catch (e: DateTimeParseException) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(
                "Invalid date format. Expected format: $format, received: $value"
            ).addConstraintViolation()
            false
        }
    }
}