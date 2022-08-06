package dev.u9g.minigames.games.gathering

import dev.u9g.minigames.games.Game
import dev.u9g.minigames.games.Games
import dev.u9g.minigames.games.gathering.util.GatheringWorld
import dev.u9g.minigames.games.gathering.util.PlayerData
import org.bukkit.entity.Player

abstract class AbstractWorldGame(player: Player) : Game {
    val world = GatheringWorld()
    protected val playerData = PlayerData(player)

    protected abstract suspend fun prepareGame()
    protected abstract suspend fun startGame()
    protected abstract suspend fun endGame()
}