package dev.u9g.minigames.util

import org.bukkit.Bukkit
import org.bukkit.event.*

typealias EventListenerFunction<T> = (event: T) -> Unit
typealias EventListenerWithTaskFunction<T> = (event: T, listener: EventListener<T>) -> Unit
typealias Filter<T> = (event: T) -> Boolean

// unused is here, so I can specify to use the internal constructor with any
class EventListener<T : Event> internal constructor(private val eventClass: Class<T>,
                                                    private val eventListener: Any,
                                                    priority: EventPriority, @Suppress("UNUSED_PARAMETER") unused: Int) : Listener {
    private val filters = mutableSetOf<Filter<T>>()

    companion object {
        fun <T : Event> withTask(eventClass: Class<T>,
                                 priority: EventPriority = EventPriority.NORMAL,
                                 listener: EventListenerWithTaskFunction<T>): EventListener<T> {
            return EventListener(eventClass, listener, priority, 1)
        }
    }

    init {
        Bukkit.getPluginManager().registerEvent(eventClass, this, priority, { _, event ->
        if (eventClass.isAssignableFrom(event::class.java)) {
            handleEvent(@Suppress("UNCHECKED_CAST") (event as T))
        }
        }, getCallingPlugin())
    }

    constructor(eventClass: Class<T>,
                priority: EventPriority = EventPriority.NORMAL,
                listener: EventListenerFunction<T>)
            : this(eventClass, listener, priority, 1) {}

    @EventHandler
    fun handleEvent(event: T) {
        for (filter in filters) {
            if (!filter(event)) return
        }
        when (eventListener) {
            is Function1<*, *> -> @Suppress("UNCHECKED_CAST") (eventListener as EventListenerFunction<T>).invoke(event)
            is Function2<*, *, *> -> @Suppress("UNCHECKED_CAST") (eventListener as EventListenerWithTaskFunction<T>).invoke(event, this)
        }
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
    }

    fun filter(filter: Filter<T>): EventListener<T> {
        filters.add(filter)
        return this
    }
}