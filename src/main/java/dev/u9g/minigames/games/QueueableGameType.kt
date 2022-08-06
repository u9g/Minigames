package dev.u9g.minigames.games

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.u9g.minigames.games.getblock.FindBlockMultiplayerOneWorld
import dev.u9g.minigames.util.getCallingPlugin
import org.bukkit.entity.Player

enum class QueueableGameType(val gameName: String, private val gameFactory: (List<Player>) -> Game) {
//    MultiplayerFindBlock("FindBlock", ::FindBlockMultiplayerOneWorld);
    MULTIPLAYER_FIND_BLOCK("FindBlock", ::FindBlockMultiplayerOneWorld);

    fun start(players: List<Player>) = gameFactory(players).also {
        // call `begin` which is a suspend fun
        getCallingPlugin().launch { it.begin() }
    }
}