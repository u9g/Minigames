package dev.u9g.minigames

import com.destroystokyo.paper.event.server.ServerExceptionEvent
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import dev.u9g.minigames.debug.DebugListener
import dev.u9g.minigames.debug.DebugSwitchType
import dev.u9g.minigames.games.Game
import dev.u9g.minigames.games.GameType
import dev.u9g.minigames.games.gathering.GatheringWorldListeners
import dev.u9g.minigames.games.gathering.gamemodifiers.*
import dev.u9g.minigames.games.gathering.itemmodifiers.AppliableItemManager
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.appliables.SnowballAppliable
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.appliables.SugarcaneAppliable
import dev.u9g.minigames.games.listeners.DontSaveWorldListener
import dev.u9g.minigames.games.listeners.HidePlayerAdvancementsListener
import dev.u9g.minigames.games.listeners.MakePickaxeLoreListener
import dev.u9g.minigames.games.listeners.PlayerDisconnectListener
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.mm
import dev.u9g.minigames.util.throwablerenderer.sendToOps
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import redempt.redlib.commandmanager.ArgType
import redempt.redlib.commandmanager.CommandHook
import redempt.redlib.commandmanager.CommandParser

class Minigames : JavaPlugin(), Listener {
    override fun onEnable() {

        val games = GameType.values().fold(mutableMapOf<String, GameType>()) { map, theGame ->
            map[theGame.gameName] = theGame
            map
        }

        val debugSwitches: Multimap<DebugSwitchType, Player> = HashMultimap.create()

        CommandParser(this.getResource("commands.rdcml")).setArgTypes(
            ArgType.of("game", games),
            ArgType.of("debugtype", DebugSwitchType.values().associateWith { it.name }.toMutableMap().entries.associate { (k,v) -> v to k })
        ).parse().register("minigames", object {
            @CommandHook("startgame")
            fun startGame(player: Player, game: GameType) {
                if (player in activeGames) {
                    player.sendMessage("You're already in a game :(".mm())
                } else {
                    activeGames[player] = game.start(player)
                }
            }

            @CommandHook("debug")
            fun debug(player: Player, debugSwitchType: DebugSwitchType) {
                val list = debugSwitches[debugSwitchType]
                if (player in list) {
                    list.remove(player)
                } else {
                    list.add(player)
                }
                player.sendMessage("<gray>[</gray><red><bold>DEBUG</red><gray>]</gray> You are ${if (player in list)"<green>now</green>" else "<red>no longer</red>"} receiving notifications for <aqua>$debugSwitchType".mm())
            }
        })

        EventListener(ServerExceptionEvent::class.java) {
            it.exception.sendToOps()
        }

        GatheringWorldListeners.start()
        DebugListener.start(debugSwitches)
        listOf(
                ManyBlockBreakListener(),
                TreefellerListener(),
                FastCookListener(),
                HidePlayerAdvancementsListener(),
                AppliableItemManager(listOf(SugarcaneAppliable(), SnowballAppliable())),
                PlayerDisconnectListener(),
                EfficiencyGivingListener(),
                SharpnessGivingListener(),
                DontSaveWorldListener(),
                MakePickaxeLoreListener()
        ).forEach { Bukkit.getPluginManager().registerEvents(it, this) }
    }

    override fun onDisable() {
//        for (game in activeGames.values) {
//            when (game) {
//                is AbstractWorldGame -> {
//                    game.world.delete()
//                }
//            }
//        }
    }

    companion object {
        val activeGames = mutableMapOf<Player, Game>()
    }
}