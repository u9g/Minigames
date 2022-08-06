package dev.u9g.minigames.games.getblock

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.u9g.minigames.Minigames
import dev.u9g.minigames.games.Game
import dev.u9g.minigames.games.gathering.util.GatheringWorld
import dev.u9g.minigames.games.getblock.GetBlockUtil.hasWantedMaterial
import dev.u9g.minigames.util.*
import dev.u9g.minigames.util.infodisplay.ClosableInfoDisplay
import dev.u9g.minigames.util.infodisplay.InfoDisplayer
import dev.u9g.minigames.util.infodisplay.TaskResult
import kotlinx.coroutines.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import java.util.concurrent.CompletableFuture

class FindBlockMultiplayerOneWorld(_players: List<Player>) : Game {
    private val players = _players.toMutableList()
    private var gameState = GameState.STARTING
    override fun gameState() = gameState
    private val playersData = players.associateWith { PlayerState(it, players.size) }
    private val world = GatheringWorld()
    private val materialWanted = Material.STONE_PICKAXE
    private val listeners = mutableListOf<EventListener<*>>()
    private var sidebar: Sidebar? = null

    override suspend fun begin() = beginIntro()

    private suspend fun beginIntro() {
        for (player in players) {
            playersData[player]?.makeDisplay() ?: throw Error("player !in playersData")
        }
        world.makeWorld()
        players.removeIf { playersData[it]!!.exitedDisplay() }
        players.shuffled().map { it -> withContext(Dispatchers.IO) {
            async { world.teleportToSpawnPoint(it) }
        } }.awaitAll()

        players.forEach { playersData[it]!!.closeDisplay() }
        gameStarted()
    }

    private fun sidebarLines(): List<Component> {
        val list = listOf(
                Component.empty(),
                "<aqua>Searching for:".mm(),
                Component.translatable(materialWanted).color(NamedTextColor.RED).decorate(TextDecoration.BOLD),
                Component.empty(),
                "<green>Players Left:${if (players.size > 5) players.size else ""}".mm()
        ).toMutableList()
        if (players.size <= 5) {
            for (player in players) {
                list.add("* <green>${player.name}".mm())
            }
        }
        return list
    }

    private fun makeSidebar(): Sidebar {
        val sidebar = Minigames.scoreboardManager.sidebar(
                Sidebar.MAX_LINES,  // 15
                null // Locale which should be used for translating Components, or null if it should depend on each player's client locale
        )

        sidebar.title("<aqua>Find the block!".mm())
        sidebarLines().mapIndexed { i, msg -> sidebar.line(i, msg) }
        sidebar.addPlayers(players)
        sidebar.visible(true)

        return sidebar
    }

    private suspend fun startSidebarRefresher() {
        val sidebar = this.sidebar ?: return
        withContext(Dispatchers.IO) {
            while (sidebar.visible()) {
                withContext(getCallingPlugin().minecraftDispatcher) {
                    sidebarLines().mapIndexed { i, msg -> sidebar.line(i, msg) }
                }
                delay(5.ticks)
            }
        }
    }

    private suspend fun gameStarted() {
        gameState = GameState.IN_PROGRESS
        sidebar = makeSidebar()
        startSidebarRefresher()
//        repeat(Int.MAX_VALUE) {
//
//        }
        Audience.audience(players).sendMessage("<gray>You can use <white>/findnewbiome</white> for a second chance!".mm())
        listeners.addAll(listOf(
                EventListener.suspendable(InventoryClickEvent::class.java) {
                    if (hasWantedMaterial(materialWanted, it.whoClicked))
                        onGameAction(GameAction.FOUND_ITEM, it.whoClicked as Player)
                }.filter { it.whoClicked in players && this.gameState == GameState.IN_PROGRESS },
                EventListener.suspendable(InventoryDragEvent::class.java) {
                    if (hasWantedMaterial(materialWanted, it.whoClicked))
                        onGameAction(GameAction.FOUND_ITEM, it.whoClicked as Player)
                }.filter { it.whoClicked in players && this.gameState == GameState.IN_PROGRESS },
                EventListener.suspendable(PlayerAttemptPickupItemEvent::class.java) {
                    if (hasWantedMaterial(materialWanted, it.player))
                        onGameAction(GameAction.FOUND_ITEM, it.player)
                }.filter { it.player in players && this.gameState == GameState.IN_PROGRESS },
                EventListener.suspendable(PlayerDeathEvent::class.java) {
                    onGameAction(GameAction.PLAYER_DEATH, it.player)
                    it.isCancelled = true
                }.filter { it.player in players && this.gameState == GameState.IN_PROGRESS }
        ))
    }

    private fun onGameAction(reason: GameAction, player: Player) {
        val playerState: PlayerState = playersData[player]!!
        when (reason) {
            GameAction.PLAYER_DEATH -> {

            }
            GameAction.PLAYER_LEAVE -> {

            }
            GameAction.FOUND_ITEM -> {
            }
        }
    }

    private fun endGame(reason: EndGameReason, player: Player?) {
        when (reason) {
            EndGameReason.EVERYONE_DIED -> {
                players.forEach {
                    it.sendMessage("Everyone <red><bold>DIED</red>!")
                }
            }
            EndGameReason.PLAYER_FOUND_ITEM -> {
                players.forEach {
                    it.sendMessage("<aqua>${player!!.name} <green><b>won</aqua>!")
                }
            }
        }
        // restore players to their previous state here!
        players.forEach { it.health = 0.0 }
        // restore players to their previous state
        world.delete()
    }
}

class PlayerState(private val player: Player, private val numberOfPlayers: Int) {
    private var intro: ClosableInfoDisplay? = null

    fun makeDisplay() {
        intro = InfoDisplayer.closableDisplay("Find Block".mm(), listOf("You will be put in a world with ${numberOfPlayers-1} other players".mm()), player)
    }

    fun closeDisplay(): TaskResult {
        val status = intro!!.status
        intro!!.close()
        return status
    }

    fun exitedDisplay() = intro!!.status == TaskResult.LEFT_TASK
}

private enum class EndGameReason {
    PLAYER_FOUND_ITEM,
    EVERYONE_DIED
}

private enum class GameAction {
    PLAYER_DEATH,
    PLAYER_LEAVE,
    FOUND_ITEM
}