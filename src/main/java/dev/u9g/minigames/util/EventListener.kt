package dev.u9g.minigames.util

import dev.u9g.minigames.getCallingPlugin
import org.bukkit.Bukkit
import org.bukkit.event.*

typealias EventListenerFunction<T> = (event: T) -> Unit
typealias EventListenerWithTaskFunction<T> = (event: T, listener: EventListener<T>) -> Unit

// unused is here, so I can specify to use the internal constructor with any
class EventListener<T : Event> internal constructor(private val eventClass: Class<T>,
                                                    val eventListener: Any,
                                                    val priority: EventPriority, unused: Int) : Listener {
    companion object {
        fun <T : Event> withTask(eventClass: Class<T>,
                                 priority: EventPriority = EventPriority.NORMAL,
                                 listener: EventListenerWithTaskFunction<T>): EventListener<T> {
            return EventListener(eventClass, listener, priority, 1)
        }
    }

    init {
        Bukkit.getPluginManager().registerEvent(eventClass, this, priority, { _, event -> handleEvent(event as T) }, getCallingPlugin())
    }

    constructor(eventClass: Class<T>,
                priority: EventPriority = EventPriority.NORMAL,
                listener: EventListenerFunction<T>)
            : this(eventClass, listener, priority, 1) {}

    @EventHandler
    fun handleEvent(event: T) {
        if (event.javaClass == eventClass) {
            when (eventListener) {
                is Function1<*, *> -> (eventListener as EventListenerFunction<T>).invoke(event)
                is Function2<*, *, *> -> (eventListener as EventListenerWithTaskFunction<T>).invoke(event, this)
            }
        }
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
    }
}