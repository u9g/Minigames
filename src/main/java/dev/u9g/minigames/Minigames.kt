package dev.u9g.minigames

import com.destroystokyo.paper.event.server.ServerExceptionEvent
import dev.u9g.minigames.games.Game
import dev.u9g.minigames.games.GameType
import dev.u9g.minigames.games.gathering.GatheringWorldListeners
import dev.u9g.minigames.games.gathering.util.GATHERING_WORLD_PREFIX
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.throwablerenderer.sendToOps
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.plugin.java.JavaPlugin
import redempt.redlib.commandmanager.ArgType
import redempt.redlib.commandmanager.CommandHook
import redempt.redlib.commandmanager.CommandParser

class Minigames : JavaPlugin(), Listener {
    override fun onEnable() {

        val games = GameType.values().fold(mutableMapOf<String, GameType>()) { map, theGame ->
            map[theGame.gameName] = theGame
            map
        }

        CommandParser(this.getResource("commands.rdcml")).setArgTypes(
            ArgType.of("game", games)
        ).parse().register("minigames", object {
            @CommandHook("startgame")
            fun startGame(player: Player, game: GameType) {
                activeGames[player] = game.start(player)
            }
        })

        EventListener(ServerExceptionEvent::class.java) {
            it.exception.sendToOps()
        }

        GatheringWorldListeners.start()
    }

    override fun onDisable() {

    }

    companion object {
        val activeGames = mutableMapOf<Player, Game>()
    }
}