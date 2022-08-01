package dev.u9g.minigames.games.gathering

import dev.u9g.minigames.Minigames
import dev.u9g.minigames.games.Game
import dev.u9g.minigames.games.gathering.util.GatheringWorld
import dev.u9g.minigames.util.*
import dev.u9g.minigames.util.infodisplay.ClosableInfoDisplay
import dev.u9g.minigames.util.infodisplay.InfoDisplayer
import dev.u9g.minigames.util.infodisplay.TaskResult
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
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

    override fun begin() = beginIntro()

    private fun beginIntro() {
        for (player in players) {
            playersData[player]?.makeDisplay() ?: throw Error("player !in playersData")
        }
        world.makeWorld()
        players.removeIf { playersData[it]!!.exitedDisplay() }
        CompletableFuture.allOf(*players.shuffled().map { world.teleportToSpawnPoint(it) }.toTypedArray()).whenComplete { _, throwable ->
            if (throwable != null) return@whenComplete run { runSync { throw throwable } }
            players.forEach { playersData[it]!!.closeDisplay() }
            gameStarted()
        }
    }

    private fun gameStarted() {
        gameState = GameState.IN_PROGRESS
        val sidebar = Minigames.scoreboardManager.sidebar(
                Sidebar.MAX_LINES,  // 15
                null // Locale which should be used for translating Components, or null if it should depend on each player's client locale
        )

        sidebar.title("<aqua>Find the block!".mm())
        sidebar.line(0, Component.empty())
        sidebar.line(1, "<aqua>Searching for:".mm())
        sidebar.line(2, Component.translatable(materialWanted).color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
        sidebar.line(3, Component.empty())
        sidebar.line(4, "<green>Players Left:".mm())
        var ix = 6
        if (15 - players.size - 6 >= 0) {
            for (player in players)
                sidebar.line(ix++, player.displayName())
        } else {
            sidebar.line(5, "<green>${players.size}".mm())
        }
//
//        sidebar.addPlayer(player) // Add the player to the sidebar
//
//        sidebar.visible(true) // Make the sidebar visible

        listeners.addAll(listOf(
            EventListener(InventoryClickEvent::class.java) {
                checkForMaterialWanted(it.whoClicked)
            }.filter { it.whoClicked in players && this.gameState == GameState.IN_PROGRESS },
            EventListener(InventoryDragEvent::class.java) {
                checkForMaterialWanted(it.whoClicked)
            }.filter { it.whoClicked in players && this.gameState == GameState.IN_PROGRESS },
            EventListener(PlayerAttemptPickupItemEvent::class.java) {
                checkForMaterialWanted(it.player)
            }.filter { it.player in players && this.gameState == GameState.IN_PROGRESS },
//            EventListener(PlayerDeathEvent::class.java) {
//                internalEndGame(GameResult.DIED)
//                it.isCancelled = true
//            }.filter { it.player in players && this.gameState == GameState.IN_PROGRESS }
        ))
    }

    private fun endGame(reason: GameEndReason, player: Player?) {
        when (reason) {
            GameEndReason.EVERYONE_DIED -> {
                players.forEach {
                    it.sendMessage("Everyone <red><bold>DIED</red>!")
                }
            }
            GameEndReason.PLAYER_FOUND_ITEM -> {
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

    private fun checkForMaterialWanted (player: HumanEntity) {
        Task.syncDelayed(1) {
            if (player.itemOnCursor.type == materialWanted || player.inventory.contains(materialWanted)) {
                endGame(GameEndReason.PLAYER_FOUND_ITEM, player as Player)
            }
        }
    }
}

class PlayerState(private val player: Player, private val numberOfPlayers: Int) {
    private var intro: ClosableInfoDisplay? = null

    fun makeDisplay() {
        intro = InfoDisplayer.closableDisplay("Find Block".mm(), listOf("You will be put in a world with $numberOfPlayers".mm()), player)
    }

    fun closeDisplay(): TaskResult {
        val status = intro!!.status
        intro!!.close()
        return status
    }

    fun exitedDisplay() = intro!!.status == TaskResult.LEFT_TASK
}

private enum class GameEndReason {
    PLAYER_FOUND_ITEM,
    EVERYONE_DIED
}