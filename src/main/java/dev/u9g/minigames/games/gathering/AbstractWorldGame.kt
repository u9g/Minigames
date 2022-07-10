package dev.u9g.minigames.games.gathering

import dev.u9g.minigames.games.Game
import dev.u9g.minigames.games.gathering.util.GatheringWorld
import dev.u9g.minigames.games.gathering.util.PlayerData
import dev.u9g.minigames.util.GameState
import org.bukkit.entity.Player

abstract class AbstractWorldGame(player: Player) : Game {
    val world = GatheringWorld()
    protected val playerData = PlayerData(player)

    protected abstract fun prepareGame()
    protected abstract fun preparePlayer()
    protected abstract fun startGame()
    protected abstract fun endGame()
}