package dev.u9g.minigames.games

import dev.u9g.minigames.util.GameState

interface Game {
    fun gameState(): GameState
    fun begin()
    fun onPlayerLogout()
}