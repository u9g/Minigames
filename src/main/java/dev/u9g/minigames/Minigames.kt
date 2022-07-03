package dev.u9g.minigames

import com.destroystokyo.paper.event.server.ServerExceptionEvent
import dev.u9g.minigames.games.GatheringGame
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.throwablerenderer.render
import dev.u9g.minigames.util.throwablerenderer.sendToOps
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import redempt.redlib.commandmanager.ArgType
import redempt.redlib.commandmanager.CommandHook
import redempt.redlib.commandmanager.CommandParser

class Minigames : JavaPlugin() {
    override fun onEnable() {
        val games: Map<String, Game> = mapOf(
            "matching" to { MatchingGame.start(it) },
            "gathering" to { GatheringGame.start(it) }
        )
        CommandParser(this.getResource("commands.rdcml")).setArgTypes(
            ArgType.of("game", games)
        ).parse().register("minigames", object {
            @CommandHook("startgame")
            fun startGame(player: Player, game: Game) = game(player)
        })

        EventListener(ServerExceptionEvent::class.java) {
            it.exception.sendToOps()
        }
    }

    override fun onDisable() {

    }
}

typealias Game = (Player) -> Unit