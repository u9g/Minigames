package dev.u9g.minigames.games.gathering

import com.destroystokyo.paper.MaterialSetTag
import com.destroystokyo.paper.MaterialTags
import com.github.u9g.u9gutils.ItemBuilder
import com.github.u9g.u9gutils.NBTUtil
import dev.u9g.minigames.Minigames
import dev.u9g.minigames.games.gathering.util.GATHERING_WORLD_PREFIX
import dev.u9g.minigames.util.*
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class GatheringWorldListeners {
    companion object {
        fun start() {
            EventListener(WorldInitEvent::class.java) {
                it.world.keepSpawnInMemory = false
            }.filter { it.world.name.startsWith(GATHERING_WORLD_PREFIX) }

            EventListener(PlayerAdvancementDoneEvent::class.java) {
                it.message(null)
            }.filter { it.player in Minigames.activeGames }

            val BLOCKS_BROKEN = NamespacedKey.fromString("minigames:blocks_broken")!!
            EventListener(BlockBreakEvent::class.java) {
                val item = it.player.inventory.itemInMainHand
                if (item.type.isAir || (item !in MaterialTags.PICKAXES && item !in MaterialTags.AXES)) return@EventListener
                val ib = ItemBuilder.from(item)
                val newNum = NBTUtil.getAsInt(item.itemMeta, BLOCKS_BROKEN).orElse(0) + 1
                ib.set(BLOCKS_BROKEN, newNum)
                val lvl = level(newNum)
                if (lvl != -1) {
                    ib.enchant(Enchantment.DIG_SPEED, lvl)
                }
            }.filter { it.player in Minigames.activeGames }

            val ENTITIES_HIT = NamespacedKey.fromString("minigames:entities_hit")!!
            EventListener(EntityDamageByEntityEvent::class.java) {
                val player = it.damager
                if (player in Minigames.activeGames) {
                    if (player is Player) {
                        val item = player.inventory.itemInMainHand
                        if (item.type.isAir || (item !in MaterialTags.SWORDS && item !in MaterialTags.AXES)) return@EventListener
                        val ib = ItemBuilder.from(item)
                        val num = NBTUtil.getAsInt(item.itemMeta, ENTITIES_HIT).orElse(0) + 1
                        ib.set(ENTITIES_HIT, num)
                        val lvl = level(num)
                        if (lvl != -1) {
                            ib.enchant(Enchantment.DAMAGE_ALL, lvl)
                        }
                    }
                }
            }

            // TODO: Only put players in this when they are in gathering game, remove them when they leave
            val secInWater: MutableMap<Player, Int> = mutableMapOf()
            val timeOutOfWater: MutableMap<Player, Int> = mutableMapOf()

            // TODO: Give dolphins grace & water breathing
            val DG = PotionEffectType.DOLPHINS_GRACE
            Task.syncRepeating(0, Ticks.TICKS_PER_SECOND.toLong()) {
                Bukkit.getOnlinePlayers().stream().filter { it in Minigames.activeGames }.forEach { player -> when {
                    player.isInWater -> {
                        secInWater[player] = (secInWater[player] ?: 0) + 1
                        timeOutOfWater[player] = 0
                        when (secInWater[player]) {
                            10 -> player.addPotionEffect(PotionEffect(DG, 10_000_000, 0))
                            20 -> player.addPotionEffect(PotionEffect(DG, 10_000_000, 1))
                            30 -> player.addPotionEffect(PotionEffect(DG, 10_000_000, 2))
                        }
                    }
                    player.hasPotionEffect(DG) -> {
                        timeOutOfWater[player] = (timeOutOfWater[player] ?: 0) + 1
                        if (timeOutOfWater[player] == 5) {
                            player.removePotionEffect(DG)
                            timeOutOfWater.remove(player)
                            secInWater.remove(player)
                        }
                    }
                }
                }
            }

            EventListener(PlayerAdvancementDoneEvent::class.java) {
                it.message(null)
            }.filter { it.player in Minigames.activeGames }

            EventListener(PlayerQuitEvent::class.java) {
                // TODO: Finish
            }.filter { it.player in Minigames.activeGames }

            // Treefeller
            EventListener(BlockBreakEvent::class.java) {
                if (MaterialSetTag.LOGS.isTagged(it.block.type))
                    checkBlockForTreefeller(it.block.type, it.player, it.block.location.clone())
            }.filter { it.player in Minigames.activeGames }

            val sugarcaneTag = MaterialSetTag(NamespacedKey.fromString("minigames:sugar_cane")).add(Material.SUGAR_CANE)

            // TODO: Handle transferring diamond pickaxe to netherite
            EventListener(PrepareItemCraftEvent::class.java) {
                if (it.inventory.result?.type in MaterialTags.PICKAXES) {
                    it.inventory.result = ItemBuilder.from(it.inventory.result).lore(listOf("<dark_gray><b>Right click</b></dark_gray> with <yellow>sugar</yellow> to add <aqua>1 <green><bold>unbreaking<green> level".mm())).build()
                }
            }
            var runningBiggerBreak = false
            EventListener(BlockBreakEvent::class.java) {
                if (!runningBiggerBreak && it.player.inventory.itemInMainHand.type in MaterialTags.PICKAXES) {
                    runningBiggerBreak = true
                    val loc = it.block.location
                    it.player.breakBlock(loc.clone().subtract(0.0,1.0,0.0).block)
                    it.player.breakBlock(loc.clone().subtract(0.0, 2.0, 0.0).block)
                    runningBiggerBreak = false
                }
            }
//            EventListener(InventoryDragEvent::class.java) {
//                Bukkit.broadcast("drag click occurred".mm())
//            }
            EventListener(InventoryClickEvent::class.java) {
//                Bukkit.broadcast("<aqua><b>Click Type</aqua>: <green>${it.click.name}</green> | <aqua><b>Click Type</aqua>: <red>${it.action.name}".mm())
                when (it.action) {
                    InventoryAction.SWAP_WITH_CURSOR -> {
                        val cursor = it.cursor
                        val clicked = it.currentItem
                        if (cursor == null || clicked == null) return@EventListener
                        if (cursor in MaterialTags.PICKAXES && clicked in sugarcaneTag) {
                            val numUsed = if (clicked.amount >= 3) 3 else clicked.amount
                            ItemBuilder.from(cursor).enchant(Enchantment.DURABILITY, cursor.getEnchantmentLevel(Enchantment.DURABILITY) + numUsed)
                            clicked.amount -= numUsed
                            it.isCancelled = true
                        } else if (cursor in sugarcaneTag && clicked in MaterialTags.PICKAXES) {
                            val numUsed = if (cursor.amount >= 3) 3 else cursor.amount
                            ItemBuilder.from(clicked).enchant(Enchantment.DURABILITY, clicked.getEnchantmentLevel(Enchantment.DURABILITY) + numUsed)
                            cursor.amount -= numUsed
                            it.isCancelled = true
                        }
//                        Bukkit.broadcast("cursor: ${cursor?.type?.name ?: "AIR"} clicked: ${clicked?.type?.name ?: "AIR"}".mm())
                    }
                    else -> {}
                }
            }.filter { it.whoClicked in Minigames.activeGames }
            // TODO: right-clicking Sugar gives speed?
        }

        private val DIRECTIONS = setOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN).map { it.direction }

        private fun checkBlockForTreefeller(blockType: Material, player: Player, location: Location) {
            for (direction in DIRECTIONS) {
                val newBlock = location.clone().add(direction).block
                if (newBlock.type == blockType) {
                    // This is such a hack, but if we dont run this in a block,
                    // we get into infinite recursion since this spawns another
                    // blockbreakevent, which we then get into, causing a stack
                    // overflow
                    runSync { player.breakBlock(newBlock) }
                }
            }
        }

        private fun level(n: Int): Int =
            if (n == 10) {
                1
            } else if (n % 50 == 0) {
                n / 50
            } else {
                -1
            }
    }
}
