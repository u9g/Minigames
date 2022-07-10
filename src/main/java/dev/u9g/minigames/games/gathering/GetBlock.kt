package dev.u9g.minigames.games.gathering

import dev.u9g.minigames.Minigames
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.GameState
import dev.u9g.minigames.util.Task
import dev.u9g.minigames.util.infodisplay.TaskResult
import dev.u9g.minigames.util.infodisplay.showInfoUntilCallbackCalled
import dev.u9g.minigames.util.mm
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

private const val STAR = "<b>*</b>"
private const val P = "<light_purple>"
private const val R = "<red>"
private const val A = "<aqua>"
private const val G = "<green>"

private val a = listOf(
        Material.JUKEBOX,
        Material.ENCHANTING_TABLE,
        Material.ENDER_CHEST,
        Material.CHIPPED_ANVIL
).random()

class GetBlock(private val player: Player) : AbstractWorldGame(player) {
    private val materialWanted = a
    private var prepareDone: () -> TaskResult = showInfoUntilCallbackCalled("<gray><bold><u>Find a <white><lang:${materialWanted.translationKey()}></white> as quickly as you can!".mm(), listOf(
            "$P$STAR Every 10 seconds you are in water,".mm(),
            "${P}you gain a level of dolphins grace. (up to 3)".mm(),
            "$R$STAR Any block of wood broken".mm(),
            "${R}that touches other wood of the same type".mm(),
            "${R}will break them too with the held tool".mm(),
            "$A$STAR Pickaxes gain a level of efficiency".mm(),
            "${A}for every 50 blocks broken".mm(),
            "$G$STAR Sugarcane can be applied to a pickaxe/axe".mm(),
            "${G}to add one level of efficiency per piece".mm()), player)
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
        world.teleportToSpawnPoint(player).thenAccept { startGame() }
    }

    override fun startGame() {
        gameState = GameState.IN_PROGRESS
        player.sendMessage("Find a <aqua><lang:${materialWanted.translationKey()}></aqua> as quick as you can!".mm())
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
                GameResult.FOUND_ITEM -> "got a <lang:${materialWanted.translationKey()}>"
                GameResult.DIED -> "died"
            }
            Bukkit.broadcast("<green><b>${player.name}</green> $action after $mins min $sec sec of searching for <aqua><lang:${materialWanted.translationKey()}></aqua>".mm())
            endGame()
        }
    }

    override fun endGame() {
        listeners.forEach { it.unregister() }
        world.delete()
        this.gameState = GameState.OVER
        Minigames.activeGames.remove(player)
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