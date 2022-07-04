package dev.u9g.minigames

import com.destroystokyo.paper.event.server.ServerExceptionEvent
import dev.u9g.minigames.games.GameInfo
import dev.u9g.minigames.games.GatheringGame
import dev.u9g.minigames.games.MatchingGame
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.throwablerenderer.sendToOps
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import redempt.redlib.commandmanager.ArgType
import redempt.redlib.commandmanager.CommandHook
import redempt.redlib.commandmanager.CommandParser

class Minigames : JavaPlugin() {
    override fun onEnable() {
        Bukkit.getWorld("gathering-world") ?: Bukkit.createWorld(WorldCreator.name("gathering-world"))

        val games = listOf(
            MatchingGame.GameInfo,
            GatheringGame.GameInfo
        ).fold(mutableMapOf<String, GameInfo>()) { map, game ->
            map[game.name()] = game
            map
        }

        games.values.forEach { it.init() }

        CommandParser(this.getResource("commands.rdcml")).setArgTypes(
            ArgType.of("game", games)
        ).parse().register("minigames", object {
            @CommandHook("startgame")
            fun startGame(player: Player, game: GameInfo) = game.start(player)
        })

        EventListener(ServerExceptionEvent::class.java) {
            it.exception.sendToOps()
        }
    }

    override fun onDisable() {

    }
}

//typealias Game = (Player) -> Unit