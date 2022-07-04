package dev.u9g.minigames.games

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

interface GameInfo {
    fun name(): String
    fun info(): List<Component>
    fun start(player: Player): Game
    fun init() {}
}