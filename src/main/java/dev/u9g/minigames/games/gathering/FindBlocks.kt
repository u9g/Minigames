package dev.u9g.minigames.games.gathering

import com.destroystokyo.paper.MaterialSetTag
import dev.u9g.minigames.Minigames
import dev.u9g.minigames.util.*
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.infodisplay.TaskResult
import dev.u9g.minigames.util.infodisplay.showInfoUntilCallbackCalled
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent

class FindBlocks(private val player: Player) : AbstractWorldGame(player) {
    private val materialTracked: Set<Material> = MaterialSetTag.DIRT.values//setOf(Material.SEAGRASS)
    private var prepareDone: () -> TaskResult = showInfoUntilCallbackCalled("findBlocks".mm(), listOf("find as many dirt blocks as you can in 3 minutes".mm()), player)
    private var materialTrackingListener: EventListener<BlockBreakEvent>? = null
    private var tickingCountdown: TickingCountdown? = null
    private var gameState = GameState.STARTING

    override fun begin() = prepareGame()

    override fun prepareGame() {
        world.makeWorld()
        preparePlayer()
    }

    override fun preparePlayer() {
        playerData.resetPlayerData()
        world.teleportToSpawnPoint(player).thenAccept {
            if (prepareDone() != TaskResult.FINISHED_TASK) endGame()
            else startGame()
        }
    }

    override fun startGame() {
        gameState = GameState.IN_PROGRESS
        var blocksMined = 0
        materialTrackingListener = EventListener(BlockBreakEvent::class.java) {
            blocksMined++
        }.filter { it.player === player && materialTracked.contains(it.block.type) }
        tickingCountdown = TickingCountdown(
            endAfterSeconds = 60,
            // tick the player's counter on their hotbar
            onTick = {
                player.sendActionBar("<aqua>${-(it.timesRun-60)}</aqua> sec left".mm())
            },
            // end the game when it's over
            onTimeout = {
                player.sendMessage(MiniMessage.miniMessage().deserialize("You mined <green>${blocksMined}</green> <brown>dirt</brown> blocks",
                        TagResolver.resolver("brown", Tag.styling(TextColor.color(0x8B4513)))))
                endGame()
            }
        )
    }

    override fun endGame() {
        materialTrackingListener?.unregister()
        tickingCountdown?.cancel()
        playerData.resetPlayerToSnapshot().thenAccept {
            world.delete()
            gameState = GameState.OVER
            Minigames.activeGames.remove(player)
        }
    }

    override fun onPlayerLogout() {
        endGame()
    }

    override fun gameState() = gameState
}
