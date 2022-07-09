//package dev.u9g.minigames.games
//
//import dev.u9g.minigames.util.mm
//import dev.u9g.minigames.util.EventListener
//import dev.u9g.minigames.util.Task
//import dev.u9g.minigames.util.TickingCountdown
//import dev.u9g.minigames.util.infodisplay.showInfoUntilCallbackCalled
//import net.kyori.adventure.text.format.TextColor
//import net.kyori.adventure.text.minimessage.MiniMessage
//import net.kyori.adventure.text.minimessage.tag.Tag
//import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
//import org.apache.commons.lang.StringUtils
//import org.bukkit.Bukkit
//import org.bukkit.Location
//import org.bukkit.Material
//import org.bukkit.WorldCreator
//import org.bukkit.attribute.Attribute
//import org.bukkit.entity.Player
//import org.bukkit.event.block.BlockBreakEvent
//import org.bukkit.inventory.ItemStack
//import org.bukkit.potion.PotionEffect
//import java.nio.file.Files
//import java.nio.file.Path
//import java.util.*
//import java.util.concurrent.CompletableFuture
//import kotlin.random.Random
//
//
//class StartData(private val player: Player) {
//    val location = player.location
//    private val inventory = arrayOfNulls<ItemStack>(player.inventory.size)
//    private val xp = player.exp
//    private val health = player.health
//    private val food = player.foodLevel
//    private val saturation = player.saturation
//    private val potions = mutableSetOf<Map<String, Any>>()
//    init {
//        player.inventory.contents.zip(0..player.inventory.size).forEach { pair ->
//            pair.first?.let { inventory[pair.second] = it.clone() }
//        }
//        println("Potions: " + StringUtils.join(player.activePotionEffects.map { it.toString() }, ", "))
//        potions.addAll(player.activePotionEffects.map { it.serialize() }.toTypedArray())
//    }
//
//    fun clearPlayer() {
//        player.exp = 0.0f
//        player.inventory.clear()
//        player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
//        player.foodLevel = 20
//        player.saturation = 0.0f
//    }
//
//    fun resetPlayer() {
//        player.exp = xp
//        player.inventory.clear()
//        inventory.zip(0..inventory.size).forEach { pair ->
//            pair.first?.let { player.inventory.setItem(pair.second, it) }
//        }
//        player.health = health
//        player.foodLevel = food
//        player.saturation = saturation
//        potions.forEach { player.addPotionEffect(PotionEffect(it)) }
//    }
//}
//
//class GatheringGame(private val player: Player): Game {
//    private val playerStartData = StartData(player)
//    private val materialTracked = Material.DIRT
//    var worldName = "gathering-" + UUID.randomUUID()
//    var listener: EventListener<*>? = null
//    var blocksMined = 0
//    override fun begin() {
//        // TODO: Make Player -> Game Map
//        val hideInfo = showInfoUntilCallbackCalled(GameInfo.name(), GameInfo.info(), player)
//        prepareGame()
//        hideInfo()
//        playerStartData.clearPlayer()
//        startGame()
//        val loc = Bukkit.getWorld("gathering-world")!!.getHighestBlockAt(Random.nextInt(0, 50)*10, Random.nextInt(0, 50)*10).location.add(0.0,1.0,0.0)
//        player.teleportAsync(loc).join()
//        startGame()
//    }
//
//    private fun prepareGame(): CompletableFuture<*> {
//        return player.teleportAsync(Location(Bukkit.createWorld(WorldCreator.name(worldName))!!, 0.0, 0.0, 0.0))
//    }
//
//    private fun destroyGame(): CompletableFuture<*> {
//        Bukkit.unloadWorld(worldName, false)
//        return CompletableFuture.allOf(*Files.walk(Path.of(worldName)).map { path ->
//            val deleteFuture = CompletableFuture<Unit>()
//            Task.asyncDelayed(0) {
//                println("file found: $path")
//                deleteFuture.complete(null)
//            }
//            deleteFuture
//        }.toList().toTypedArray())
//    }
//
//    private fun startGame(): CompletableFuture<Unit> {
//        val cf = CompletableFuture<Unit>()
//        // listen for block breaks
//        listener = EventListener(BlockBreakEvent::class.java) {
//            blocksMined++
//        }.filter { it.player == player && it.block.type == materialTracked }
//        TickingCountdown(
//            endAfterSeconds = 60,
//            // tick the player's counter on their hotbar
//            onTick = {
//                     player.sendActionBar("<aqua>${-(it.timesRun-60)}</aqua> sec left".mm())
//            },
//            // end the game when it's over
//            onTimeout = {
//                player.teleportAsync(playerStartData.location).thenAccept {
//                    listener?.unregister()
//                    listener = null
//                    playerStartData.resetPlayer()
//                    player.sendMessage(MiniMessage.miniMessage().deserialize("You mined <green>${blocksMined}</green> <brown>dirt</brown> blocks",
//                            TagResolver.resolver("brown", Tag.styling(TextColor.color(0x8B4513)))))
//                    cf.complete(null)
//                }
//            }
//        )
//        return cf
//    }
//
//    companion object {
//        val GameInfo = object : GameInfo {
//            override fun name() = "Gathering"
//
//            override fun info() = listOf("Gather as much dirt as you can in 2 minutes".mm())
//
//            override fun start(player: Player): Game {
//                // TODO: Make a player -> Game map
//                val game = GatheringGame(player)
//                game.begin()
//                return game
//            }
//        }
//    }
//}
