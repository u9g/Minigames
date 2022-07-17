package dev.u9g.minigames

import com.destroystokyo.paper.MaterialSetTag
import com.destroystokyo.paper.event.server.ServerExceptionEvent
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import dev.u9g.minigames.debug.DebugListener
import dev.u9g.minigames.debug.DebugSwitchType
import dev.u9g.minigames.games.Game
import dev.u9g.minigames.games.GameType
import dev.u9g.minigames.games.gathering.GatheringWorldListeners
import dev.u9g.minigames.games.gathering.gamemodifiers.*
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.AppliableItemManager
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.appliables.*
import dev.u9g.minigames.games.listeners.*
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.mm
import dev.u9g.minigames.util.throwablerenderer.sendToOps
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin
import redempt.redlib.commandmanager.ArgType
import redempt.redlib.commandmanager.CommandHook
import redempt.redlib.commandmanager.CommandParser
import java.io.IOException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class Minigames : JavaPlugin(), Listener {
    override fun onEnable() {

        val games = GameType.values().fold(mutableMapOf<String, GameType>()) { map, theGame ->
            map[theGame.gameName] = theGame
            map
        }

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
        DebugListener.start()
        appliableItemManager = AppliableItemManager(listOf(
                SugarcaneAppliable(),
                FlintAppliable(),
                CopperAppliable(),
                FeatherAppliable(),
                SoulsandAppliable(),
                RedstoneAppliable(),
        ))

        listOf(
                ManyBlockBreakListener(),
                TreefellerListener(),
                FastCookListener(),
                HidePlayerAdvancementsListener(),
                appliableItemManager!!,
                PlayerDisconnectListener(),
                EfficiencyGivingListener(),
                SharpnessGivingListener(),
                DontSaveWorldListener(),
                MakePickaxeLoreListener(),
                SwimListener(),
                FortressLocaterListener(),
                CommandListener(),
                FortuneDisablerListener(),
                LootingDisablerListener(),
                LootGenerateLoreRemakerListener(),
                SingleUseCraftingTableListener()
        ).forEach { Bukkit.getPluginManager().registerEvents(it, this) }

//        EventListener(PlayerJoinEvent::class.java) {
//            it.player.sendMessage("Find a <aqua><lang:${Material.CHIPPED_ANVIL.translationKey()}></aqua> as quick as you can!".mm())
//        }
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
        var appliableItemManager: AppliableItemManager? = null
        val debugSwitches: Multimap<DebugSwitchType, Player> = HashMultimap.create()
    }
}