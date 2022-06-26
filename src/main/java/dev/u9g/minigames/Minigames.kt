package dev.u9g.minigames

import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import redempt.redlib.commandmanager.CommandHook
import redempt.redlib.commandmanager.CommandParser

class Minigames : JavaPlugin() {
    override fun onEnable() {
        CommandParser(this.getResource("commands.rdcml")).parse().register("minigames", object {
            @CommandHook("startgame")
            fun startGame(player: Player) {
                MatchingGame.start(player)
            }
        })
    }

    override fun onDisable() {

    }
}