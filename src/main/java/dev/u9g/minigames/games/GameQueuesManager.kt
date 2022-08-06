package dev.u9g.minigames.games

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import dev.u9g.minigames.util.SplitAudience
import dev.u9g.minigames.util.mm
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.command.UnknownCommandEvent
import java.util.*
import kotlin.time.Duration.Companion.seconds

class GameQueuesManager : Listener {
    private val uuid2GameName: MutableMap<UUID, Component> = mutableMapOf()
    private val activeGameQueues: Multimap<UUID, Player> = HashMultimap.create()

    suspend fun startQueueFor(gameName: Component, gameType: QueueableGameType) {
        val gameUUID = UUID.randomUUID()
        println("running")
        println(Thread.currentThread().name)
        fun sendStartingIn(seconds: Int) {
            val (notInGame, inGame) = SplitAudience.splitAudience(Bukkit.getServer().filterAudience { !Games.isPlayerInGame(it) }) { it !in activeGameQueues[gameUUID] }
            notInGame.sendMessage(gameName
                    .append(" <white><!b>starting in <green>$seconds</green> seconds. <gray><click:run_command:/game-$gameUUID><hover:show_text:'Click to join'>(Click to join)".mm()))
            inGame.sendMessage(gameName
                    .append(" <white><!b>starting in <green>$seconds</green> seconds.".mm()))
        }
        uuid2GameName[gameUUID] = gameName
        sendStartingIn(30)
        delay(10.seconds)
        sendStartingIn(20)
        delay(5.seconds)
        for (i in 5 downTo 1) {
            sendStartingIn(i)
            delay(1.seconds)
        }
        uuid2GameName.remove(gameUUID)
        val players = activeGameQueues.removeAll(gameUUID).toList()
        val game = gameType.start(players)
        players.forEach { player ->
            Games[player] = game
        }
    }

    @EventHandler
    fun onGameJoinCommand(event: UnknownCommandEvent) {
        val gameUUID = event.commandLine.replace("game-", "")
        try {
            val uuid = UUID.fromString(gameUUID)
            val gameName = uuid2GameName[uuid] ?: return
            event.message("You have just joined the queue for ".mm().append(gameName).append("<white><!b> game!".mm()))
            activeGameQueues[uuid].add(event.sender as Player)
        } catch (_: Exception) {}
    }
}