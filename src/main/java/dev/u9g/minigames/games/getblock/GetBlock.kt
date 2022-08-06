package dev.u9g.minigames.games.getblock

import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.u9g.minigames.games.Games
import dev.u9g.minigames.games.gathering.*
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.GameState
import dev.u9g.minigames.util.infodisplay.InfoDisplayer
import dev.u9g.minigames.util.infodisplay.TaskResult
import dev.u9g.minigames.util.mm
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent

private val UNGETTABLES = listOf(
        Material.COMMAND_BLOCK,
        Material.COMMAND_BLOCK_MINECART,
        Material.CHAIN_COMMAND_BLOCK,
        Material.REPEATING_COMMAND_BLOCK,
        Material.DEBUG_STICK,
        Material.AIR,
        Material.CAVE_AIR,
        Material.VOID_AIR,
        *Material.values().filter { it.name.endsWith("_SPAWN_EGG") || it.name.endsWith("SHULKER_BOX") }.toTypedArray()
)

//private val a = listOf(
////        Material.JUKEBOX,
////        Material.ENCHANTING_TABLE,
////        Material.ENDER_CHEST,
//        Material.NETHER_WART
//).random()

class GetBlock(private val player: Player) : AbstractWorldGame(player) {
    private val materialWanted = Material.values().filter { it.isItem }.filter { it !in UNGETTABLES }.random()
    private var infoWindow = InfoDisplayer.closableDisplay("<gray><bold><u>Find a <white><lang:${materialWanted.translationKey()}></white> as quickly as you can!".mm(), listOf(
            "<light_purple><b>*</b> After 10 seconds of being in water,".mm(),
            "<light_purple>you gain a level of dolphins grace.".mm(),
            "<red><b>*</b> Any block of wood broken".mm(),
            "<red>that touches other wood of the same type".mm(),
            "<red>will break them too with the held tool".mm(),
            "<aqua><b>*</b> Pickaxes gain a level of efficiency".mm(),
            "<aqua>for every 50 blocks broken".mm(),
            "<green><b>*</b> Sugarcane can be applied to a pickaxe/axe".mm(),
            "<green>to add one level of efficiency per piece".mm()), player)
    private val listeners: MutableSet<EventListener<*>> = mutableSetOf()
    private var gameState = GameState.STARTING
    private val startTime = System.currentTimeMillis()

    override suspend fun begin() = prepareGame()

    override suspend fun prepareGame() {
        // prepare world
        world.makeWorld()
        // prepare player
        playerData.resetPlayerData()
        val status = infoWindow.status
        if (status != TaskResult.FINISHED_TASK) return endGame()
        world.teleportToSpawnPoint(player)
        startGame()
        infoWindow.close()
    }

    override suspend fun startGame() {
        gameState = GameState.IN_PROGRESS
        player.sendMessage("Find a <aqua><lang:${materialWanted.translationKey()}></aqua> as quick as you can!".mm())
        // TODO: Add a took too long to find the item timeout
        player.sendMessage("<gray>You can use <white>/findnewbiome</white> for a second chance!".mm())
        listeners.add(EventListener.suspendable(InventoryClickEvent::class.java) {
            checkForMaterialWanted(it.whoClicked)
        }.filter { it.whoClicked == player && this.gameState == GameState.IN_PROGRESS })
        listeners.add(EventListener.suspendable(InventoryDragEvent::class.java) {
            checkForMaterialWanted(it.whoClicked)
        }.filter { it.whoClicked == player && this.gameState == GameState.IN_PROGRESS })
        listeners.add(EventListener.suspendable(PlayerAttemptPickupItemEvent::class.java) {
            checkForMaterialWanted(it.player)
        }.filter { it.player == player && this.gameState == GameState.IN_PROGRESS })
        listeners.add(EventListener.suspendable(PlayerDeathEvent::class.java) {
            internalEndGame(GameResult.DIED)
            it.isCancelled = true
        }.filter { it.player == player && this.gameState == GameState.IN_PROGRESS })
    }

    private suspend fun checkForMaterialWanted(entity: HumanEntity) {
        delay(1.ticks)
        if (entity.itemOnCursor.type == materialWanted || entity.inventory.contains(materialWanted)) {
            internalEndGame(GameResult.FOUND_ITEM)
        }
    }

    private suspend fun internalEndGame(gameResult: GameResult) {
        val timeTaken = (System.currentTimeMillis() - startTime) / 1000
        val mins = timeTaken / 60
        val sec = timeTaken % 60
        playerData.resetPlayerToSnapshot()
        val action = when (gameResult) {
            GameResult.FOUND_ITEM -> "got a <lang:${materialWanted.translationKey()}>"
            GameResult.DIED -> "died"
        }
        Bukkit.broadcast("<green><b>${player.name}</green> $action after $mins min $sec sec of searching for <aqua><lang:${materialWanted.translationKey()}></aqua>".mm())
        endGame()
    }

    override suspend fun endGame() {
        listeners.forEach { it.unregister() }
        world.delete()
        this.gameState = GameState.OVER
        Games.leftGame(player)
    }

    override fun gameState() = gameState
}