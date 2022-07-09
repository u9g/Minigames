package dev.u9g.minigames.games

import dev.u9g.minigames.games.gathering.FindBlocks
import dev.u9g.minigames.games.gathering.GetBlock
import org.bukkit.entity.Player

enum class GameType(val gameName: String, private val gameFactory: (Player) -> Game) {
    GATHERING("Matching", ::MatchingGame),
    FIND_BLOCK("FindBlocks", ::FindBlocks),
    GET_BLOCK("GetBlock", ::GetBlock);

    fun start(player: Player) = gameFactory(player).also { it.begin() }
}
