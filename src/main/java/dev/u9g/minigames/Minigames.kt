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
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.AppliableItemManager
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.appliables.*
import dev.u9g.minigames.games.listeners.*
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.mm
import dev.u9g.minigames.util.throwablerenderer.sendToOps
import net.megavex.scoreboardlibrary.ScoreboardLibraryImplementation
import net.megavex.scoreboardlibrary.api.ScoreboardManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import redempt.redlib.commandmanager.ArgType
import redempt.redlib.commandmanager.CommandHook
import redempt.redlib.commandmanager.CommandParser

class Minigames : JavaPlugin(), Listener {
    override fun onEnable() {
        ScoreboardLibraryImplementation.init()
        scoreboardManager = ScoreboardManager.scoreboardManager(this)

        CommandParser(this.getResource("commands.rdcml")).setArgTypes(
            ArgType.of("game", GameType.values().associateBy { it.gameName }),
            ArgType.of("debugtype", DebugSwitchType.values().associateBy { it.name }),
            ArgType.of("queablegame", QueueableGameType.values().associateBy { it.gameName })
        ).parse().register("minigames", object {
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

            @CommandHook("startgame")
            fun startGame(player: Player, game: GameType) {
                if (player in activeGames) {
                    player.sendMessage("You're already in a game :(".mm())
                } else {
                    activeGames[player] = game.start(player)
                }
            }

            @CommandHook("queuegame")
            fun queueGame(player: Player, game: QueueableGameType) {
                if (player in activeGames) {
                    player.sendMessage("You're already in a game :(".mm())
                } else {
                    QueueableGameStarter(("<aqua><b>"+game.gameName).mm(), game).startQueue()
//                    activeGames[player] = game.start(player)
                }
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
                FindNewBiomeFunctionality(),
                FortuneDisablerListener(),
                LootingDisablerListener(),
                SingleUseCraftingTableListener(),
                GrapplingHookListener(),
                PlayerEquipHeadListener()
        ).forEach { Bukkit.getPluginManager().registerEvents(it, this) }

//        EventListener(PlayerJoinEvent::class.java) {
//            it.player.sendMessage("Find a <aqua><lang:${Material.CHIPPED_ANVIL.translationKey()}></aqua> as quick as you can!".mm())
//        }
    }

    override fun onDisable() {
        scoreboardManager.close()
        ScoreboardLibraryImplementation.close()
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
        lateinit var scoreboardManager: ScoreboardManager
    }
}

