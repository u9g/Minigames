package dev.u9g.minigames.games.gathering

import com.destroystokyo.paper.MaterialSetTag
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.u9g.minigames.games.Games
import dev.u9g.minigames.util.*
import dev.u9g.minigames.util.infodisplay.InfoDisplayer
import dev.u9g.minigames.util.infodisplay.TaskResult
import kotlinx.coroutines.future.asDeferred
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent

val TIME_GIVEN_MINS = 3

class FindBlocks(private val player: Player) : AbstractWorldGame(player) {
    private val materialTracked: Set<Material> = MaterialSetTag.DIRT.values//setOf(Material.SEAGRASS)
    private var infoWindow = InfoDisplayer.closableDisplay("findBlocks".mm(), listOf("find as many dirt blocks as you can in $TIME_GIVEN_MINS minutes".mm()), player)
    private var materialTrackingListener: EventListener<BlockBreakEvent>? = null
    private var tickingCountdown: TickingCountdown? = null
    private var gameState = GameState.STARTING

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
        var blocksMined = 0
        materialTrackingListener = EventListener(BlockBreakEvent::class.java) {
            blocksMined++
        }.filter { it.player === player && materialTracked.contains(it.block.type) }
        tickingCountdown = TickingCountdown(
            endAfterSeconds = TIME_GIVEN_MINS*60,
            // tick the player's counter on their hotbar
            onTick = {
                player.sendActionBar("<aqua>${-(it.timesRun-(TIME_GIVEN_MINS*60))}</aqua> sec left".mm())
            },
            // end the game when it's over
            onTimeout = {
                player.sendMessage(MiniMessage.miniMessage().deserialize("You mined <green>${blocksMined}</green> <brown>dirt</brown> blocks",
                        TagResolver.resolver("brown", Tag.styling(TextColor.color(0x8B4513)))))
                getCallingPlugin().launch { endGame() }
            }
        )
    }

    override suspend fun endGame() {
        materialTrackingListener?.unregister()
        tickingCountdown?.cancel()
        playerData.resetPlayerToSnapshot()
        world.delete()
        gameState = GameState.OVER
        Games.leftGame(player)
    }

    override fun gameState() = gameState
}
