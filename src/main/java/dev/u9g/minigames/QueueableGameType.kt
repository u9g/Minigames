package dev.u9g.minigames

import dev.u9g.minigames.games.Game
import dev.u9g.minigames.games.gathering.FindBlockMultiplayerOneWorld
import dev.u9g.minigames.games.gathering.FindBlocks
import dev.u9g.minigames.games.gathering.GetBlock
import org.bukkit.entity.Player

enum class QueueableGameType(val gameName: String, private val gameFactory: (List<Player>) -> Game) {
//    MultiplayerFindBlock("FindBlock", ::FindBlockMultiplayerOneWorld);
    MULTIPLAYER_FIND_BLOCK("FindBlock", ::FindBlockMultiplayerOneWorld);

    fun start(players: List<Player>) = gameFactory(players).also { it.begin() }
}
