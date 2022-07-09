package dev.u9g.minigames.games.gathering

import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.GameState
import dev.u9g.minigames.util.Task
import dev.u9g.minigames.util.infodisplay.TaskResult
import dev.u9g.minigames.util.infodisplay.showInfoUntilCallbackCalled
import dev.u9g.minigames.util.mm
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

class GetBlock(private val player: Player) : AbstractWorldGame(player) {
    private var prepareDone: () -> TaskResult = showInfoUntilCallbackCalled("<gray><bold><u>Find a <white>jukebox</white> as quickly as you can!".mm(), listOf(
            "<light_purple><b>*</b> Every 10 seconds you are in water,".mm(),
            "<light_purple>you gain a level of dolphins grace. (up to 3)".mm(),
            "<red><b>*</b> Any wood block broken that is part of a tree,".mm(),
            "<red> will break all matching wood blocks of the same type.".mm(),
            "<aqua><b>*</b> Pickaxes gain a level of efficiency for every".mm(),
            "<aqua>50 blocks broken, (after the first 10, which give one level of efficiency, max of lvl 8)".mm(),
            "<green><b>*</b> Applying sugarcane to a pickaxe item adds one level of efficiency per piece of sugarcane".mm()), player)
    private val materialWanted = Material.JUKEBOX
    private val listeners: MutableSet<EventListener<*>> = mutableSetOf()
    private var gameState = GameState.STARTING
    private val startTime = System.currentTimeMillis()

    override fun begin() = prepareGame()

    override fun prepareGame() {
        world.makeWorld()
        preparePlayer()
    }

    override fun preparePlayer() {
        playerData.resetPlayerData()
        if (prepareDone() != TaskResult.FINISHED_TASK) return endGame()
        player.teleportAsync(world.spawnPoint()).thenAccept { startGame() }
    }

    override fun startGame() {
        gameState = GameState.IN_PROGRESS
        // TODO: Add a too long timeout
        val checkForMaterialWanted: (HumanEntity) -> Unit = {
            Task.syncDelayed(1) {
                if (player.itemOnCursor.type == materialWanted || player.inventory.contains(materialWanted)) {
                    internalEndGame(GameResult.FOUND_ITEM)
                }
            }
        }
        listeners.add(EventListener(InventoryClickEvent::class.java) {
            checkForMaterialWanted(it.whoClicked)
        }.filter { it.whoClicked == player })
        listeners.add(EventListener(InventoryDragEvent::class.java) {
            checkForMaterialWanted(it.whoClicked)
        }.filter { it.whoClicked == player })
        listeners.add(EventListener(PlayerDeathEvent::class.java) {
            internalEndGame(GameResult.DIED)
            it.isCancelled = true
        }.filter { it.player == player })
    }

    private fun internalEndGame(gameResult: GameResult) {
        val timeTaken = (System.currentTimeMillis() - startTime) / 1000
        val mins = timeTaken / 60
        val sec = timeTaken % 60
        playerData.resetPlayerToSnapshot().thenAccept {
            val action = when (gameResult) {
                GameResult.FOUND_ITEM -> "got a jukebox"
                GameResult.DIED -> "died"
            }
            player.sendMessage("You $action after $mins min $sec sec of searching for $materialWanted")
            endGame()
        }
    }

    override fun endGame() {
        listeners.forEach { it.unregister() }
        world.delete()
        this.gameState = GameState.OVER
    }

    override fun onPlayerLogout() {
        endGame()
    }

    override fun gameState() = gameState
}

private enum class GameResult {
    DIED,
    FOUND_ITEM
}