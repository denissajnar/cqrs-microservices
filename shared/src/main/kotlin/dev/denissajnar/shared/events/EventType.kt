package dev.denissajnar.shared.events

/**
 * Enum representing all available domain event types in the CQRS system
 * Provides mapping between event classes and their string representations
 */
enum class EventType(val eventClass: Class<out DomainEvent>, val typeName: String) {
    ORDER_EVENT(OrderEvent::class.java, "OrderEvent");

    companion object {
        /**
         * Gets the EventType for a given domain event instance
         * @param event the domain event instance
         * @return the corresponding EventType
         */
        fun fromEvent(event: DomainEvent): EventType =
            when (event) {
                is OrderEvent -> ORDER_EVENT
                else -> throw IllegalArgumentException("Unknown event type: ${event::class.simpleName}")
            }

        /**
         * Gets the EventType from a string type name
         * @param typeName the string representation of the event type
         * @return the corresponding EventType
         */
        fun fromTypeName(typeName: String): EventType =
            entries.firstOrNull { it.typeName == typeName }
                ?: throw IllegalArgumentException("Unknown event type: $typeName")
    }
}
