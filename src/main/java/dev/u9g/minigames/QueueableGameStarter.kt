package dev.u9g.minigames

import com.google.common.cache.Cache
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import dev.u9g.minigames.util.Task
import dev.u9g.minigames.util.mm
import net.kyori.adventure.text.Component
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

class QueueableGameStarter(private val gameName: Component, game: QueueableGameType) {
    val uuid = UUID.randomUUID()
    fun startingIn(seconds: Int) = gameName
            .append(" starting in <green>$seconds</green> seconds".mm())
            .append(" <gray><click:run_command:/say hello><hover:show_text:'Click to join'>(Click to join)".mm())

    fun startQueue() {
        Bukkit.broadcast(startingIn(30))

    }
}

private val joinableGame: Multimap<UUID, Player> = HashMultimap.create()