package pots.service.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [DateTimeValidator::class])

annotation class DateTimeValid(
    val message: String = "Invalid date format",
    val format: String = "dd-MM-yyyy",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)