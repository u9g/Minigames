package dev.u9g.minigames.games

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.u9g.minigames.games.gathering.FindBlocks
import dev.u9g.minigames.games.getblock.GetBlock
import dev.u9g.minigames.util.getCallingPlugin
import org.bukkit.entity.Player

enum class GameType(val gameName: String, private val gameFactory: (Player) -> Game) {
    GATHERING("Matching", ::MatchingGame),
    FIND_BLOCK("FindBlocks", ::FindBlocks),
    GET_BLOCK("GetBlock", ::GetBlock);

    fun start(player: Player) = gameFactory(player).also {
        // call `begin` which is a suspend fun
        getCallingPlugin().launch { it.begin() }
    }
}
