package dev.u9g.minigames

import com.destroystokyo.paper.event.server.ServerExceptionEvent
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.launch
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import dev.u9g.minigames.debug.DebugListener
import dev.u9g.minigames.debug.DebugSwitchType
import dev.u9g.minigames.games.Games
import dev.u9g.minigames.games.GameQueuesManager
import dev.u9g.minigames.games.GameType
import dev.u9g.minigames.games.QueueableGameType
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
import redempt.redlib.commandmanager.ArgType
import redempt.redlib.commandmanager.CommandHook
import redempt.redlib.commandmanager.CommandParser

class Minigames : SuspendingJavaPlugin(), Listener {
    private val queuesManager = GameQueuesManager()

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
                if (Games.isPlayerInGame(player)) {
                    player.sendMessage("You're already in a game :(".mm())
                } else {
                    Games[player] = game.start(player)
                }
            }

            @CommandHook("queuegame")
            fun queueGame(player: Player, game: QueueableGameType) {
                if (Games.isPlayerInGame(player)) {
                    player.sendMessage("You're already in a game :(".mm())
                } else {
                    this@Minigames.launch {
                        queuesManager.startQueueFor(("<aqua><b>"+game.gameName).mm(), game)
                    }
//                    activeGames[player] = game.start(player)
                }
            }

            @CommandHook("leavegame")
            fun leaveGame(player: Player) {
                player.health = .0
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
                PlayerEquipHeadListener(),
                queuesManager
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
        var appliableItemManager: AppliableItemManager? = null
        val debugSwitches: Multimap<DebugSwitchType, Player> = HashMultimap.create()
        lateinit var scoreboardManager: ScoreboardManager
    }
}
