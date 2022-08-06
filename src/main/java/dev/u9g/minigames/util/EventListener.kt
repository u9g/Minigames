package dev.u9g.minigames.util

import com.github.shynixn.mccoroutine.bukkit.launch
import org.bukkit.Bukkit
import org.bukkit.event.*

typealias SuspendableEventListenerFunction<T> = suspend (event: T) -> Unit
typealias EventListenerFunction<T> = (event: T) -> Unit
typealias EventListenerWithTaskFunction<T> = (event: T, listener: EventListener<T>) -> Unit
typealias Filter<T> = (event: T) -> Boolean

class EventListener<T : Event> internal constructor(private val eventClass: Class<T>,
                                                    private val eventListener: Any,
                                                    priority: EventPriority,
                                                    private val isSuspendable: Boolean) : Listener {
    private val filters = mutableSetOf<Filter<T>>()

    companion object {
        fun <T : Event> withTask(eventClass: Class<T>,
                                 priority: EventPriority = EventPriority.NORMAL,
                                 listener: SuspendableEventListenerFunction<T>): EventListener<T> {
            return EventListener(eventClass, listener, priority, false)
        }

        fun <T : Event> suspendable(eventClass: Class<T>, priority: EventPriority = EventPriority.NORMAL, listener: SuspendableEventListenerFunction<T>): EventListener<T> {
            return EventListener(eventClass, listener, priority, true)
        }
    }

    init {
        val plugin = getCallingPlugin()
        Bukkit.getPluginManager().registerEvent(eventClass, this, priority, { _, event ->
        if (eventClass.isAssignableFrom(event::class.java)) {
            plugin.launch { handleEvent(@Suppress("UNCHECKED_CAST") (event as T)) }
        }
        }, plugin)
    }

    constructor(eventClass: Class<T>,
                priority: EventPriority = EventPriority.NORMAL,
                listener: EventListenerFunction<T>)
            : this(eventClass, listener, priority, false)

    suspend fun handleEvent(event: T) {
        for (filter in filters) {
            if (!filter(event)) return
        }
        if (isSuspendable) {
            (eventListener as SuspendableEventListenerFunction<T>).invoke(event)
        } else when (eventListener) {
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