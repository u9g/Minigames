//package dev.u9g.minigames.games.gathering
//
//import dev.u9g.minigames.Minigames
//import dev.u9g.minigames.games.gathering.util.GatheringWorld
//import dev.u9g.minigames.games.gathering.util.PlayerData
//import dev.u9g.minigames.util.EventListener
//import dev.u9g.minigames.util.GameState
//import dev.u9g.minigames.util.Task
//import dev.u9g.minigames.util.infodisplay.ShowInfoUntilCallbackCalled
//import dev.u9g.minigames.util.mm
//import net.kyori.adventure.text.Component
//import org.bukkit.Bukkit
//import org.bukkit.Material
//import org.bukkit.entity.HumanEntity
//import org.bukkit.entity.Player
//import org.bukkit.event.entity.PlayerDeathEvent
//import org.bukkit.event.inventory.InventoryClickEvent
//import org.bukkit.event.inventory.InventoryDragEvent
//
//private const val STAR = "<b>*</b>"
//private const val P = "<light_purple>"
//private const val R = "<red>"
//private const val A = "<aqua>"
//private const val G = "<green>"
//
////private val a = listOf(
////        Material.JUKEBOX,
////        Material.ENCHANTING_TABLE,
////        Material.ENDER_CHEST,
////        Material.CHIPPED_ANVIL
////).random()
//
//val msg = listOf(
//"$P$STAR Every 10 seconds you are in water,".mm(),
//"${P}you gain a level of dolphins grace".mm(),
//"$R$STAR Any block of wood broken".mm(),
//"${R}that touches other wood of the same type".mm(),
//"${R}will break them too with the held tool".mm(),
//"$A$STAR Pickaxes gain a level of efficiency".mm(),
//"${A}for every 50 blocks broken".mm(),
//"$G$STAR Sugarcane can be applied to a pickaxe/axe".mm(),
//"${G}to add one level of efficiency per piece".mm())
//
//data class GameData(val player: Player,
//                    val title: Component) {
//    val infoWindow: ShowInfoUntilCallbackCalled = ShowInfoUntilCallbackCalled(title, msg, player)
//    val world = GatheringWorld()
//    val playerData = PlayerData(player)
//}
//
//class GetAgainstPlayerGame(private val player1: Player, private val player2: Player) /*: AbstractWorldGame(player)*/ {
//    private val materialWanted = Material.values().filter { it.isItem }.random()
//    private val listeners: MutableSet<EventListener<*>> = mutableSetOf()
//    private var gameState = GameState.STARTING
//    private val startTime = System.currentTimeMillis()
//
//    val title = "<gray><bold><u>Find a <white><lang:${materialWanted.translationKey()}></white> as quickly as you can!".mm()
//    private val gameData = listOf(
//            GameData(player1, title),
//            GameData(player2, title)
//    )
//
//    fun begin() = gameData.forEach { prepareGame(it) }
//
//    private fun prepareGame(gameData: GameData) {
//        gameData.world.makeWorld()
//        preparePlayer(gameData)
//        listOf(player1, player2).forEach { preparePlayer(it) }
//    }
//
//    private fun preparePlayer(gameData: GameData) {
//        gameData.playerData.resetPlayerData()
//
////        val status = infoWindow.status
////        if (status != TaskResult.FINISHED_TASK) return endGame()
//
//        gameData.world.teleportToSpawnPoint(gameData.player).thenAccept {
//            startGame(gameData)
//            gameData.infoWindow.close()
//        }
//    }
//
//    private fun startGame(gameData: GameData) {
//        gameState = GameState.IN_PROGRESS
//        player1.sendMessage("Find a <aqua><lang:${materialWanted.translationKey()}></aqua> as quick as you can!".mm())
//        // TODO: Add a too long timeout
//        val checkForMaterialWanted: (HumanEntity) -> Unit = {
//            Task.syncDelayed(1) {
//                if (player1.itemOnCursor.type == materialWanted || player1.inventory.contains(materialWanted)) {
//                    internalEndGame(GameResult.FOUND_ITEM)
//                }
//            }
//        }
//        player1.sendMessage("<gray>You can use <white>/findnewbiome</white> for a second chance!".mm())
//        listeners.add(EventListener(InventoryClickEvent::class.java) {
//            checkForMaterialWanted(it.whoClicked)
//        }.filter { it.whoClicked == player1 })
//        listeners.add(EventListener(InventoryDragEvent::class.java) {
//            checkForMaterialWanted(it.whoClicked)
//        }.filter { it.whoClicked == player1 })
//        // pickup item too
//        listeners.add(EventListener(PlayerDeathEvent::class.java) {
//            internalEndGame(GameResult.DIED)
//            it.isCancelled = true
//        }.filter { it.player == player1 })
//    }
//
//    private fun internalEndGame(gameResult: GameResult) {
//        val timeTaken = (System.currentTimeMillis() - startTime) / 1000
//        val mins = timeTaken / 60
//        val sec = timeTaken % 60
//        playerData.resetPlayerToSnapshot().thenAccept {
//            val action = when (gameResult) {
//                GameResult.FOUND_ITEM -> "got a <lang:${materialWanted.translationKey()}>"
//                GameResult.DIED -> "died"
//            }
//            Bukkit.broadcast("<green><b>${player1.name}</green> $action after $mins min $sec sec of searching for <aqua><lang:${materialWanted.translationKey()}></aqua>".mm())
//            endGame()
//        }
//    }
//
//    fun endGame() {
//        listeners.forEach { it.unregister() }
//        world.delete()
//        this.gameState = GameState.OVER
//        Minigames.activeGames.remove(player1)
//    }
//
//    fun onPlayerLogout() {
//        endGame()
//    }
//
//    fun gameState() = gameState
//}
