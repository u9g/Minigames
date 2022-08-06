package dev.u9g.minigames.games

import org.bukkit.entity.Player

object Games {
    private val player2game = mutableMapOf<Player, Game>()
    val activeGames = mutableListOf<Game>()

    fun isPlayerInGame(player: Any): Boolean {
        if (player !is Player) return false
        return player in player2game
    }

    fun leftGame(player: Player) {
        player2game.remove(player)
    }

    operator fun set(player: Player, game: Game) {
        player2game[player] = game
    }
}