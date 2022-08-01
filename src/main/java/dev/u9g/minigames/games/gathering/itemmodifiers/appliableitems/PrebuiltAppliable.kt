package dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems

import com.destroystokyo.paper.MaterialSetTag
import com.github.u9g.u9gutils.ItemBuilder
import com.github.u9g.u9gutils.NBTUtil
import dev.u9g.minigames.games.gathering.level
import dev.u9g.minigames.util.convertToRoman
import dev.u9g.minigames.util.mm
import dev.u9g.minigames.util.toSetTag
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

private fun applyItemLine(materialToAddMore: Material, color: String) =
        " <gray>Apply <$color><lang:${materialToAddMore.translationKey()}></$color> to this tool".mm()

private fun getLevel(item: ItemStack, levelKey: NamespacedKey) = item.itemMeta?.let { NBTUtil.getAsInt(it, levelKey).orElse(0) } ?: 0

typealias Applier = (mainItem: ItemStack, appliedItem: ItemStack, amountOfItemsToUse: AmountOfItemsToUse, player: Player) -> Unit
typealias LoreMaker = (item: ItemStack) -> List<Component>

object PrebuiltAppliable {
    open class EnchantmentAppliable(override val mainItem: MaterialSetTag, materialToAddMore: Material, maxLevel: Int, enchantment: Enchantment, materialColor: String) : AppliableItem {
        override val appliedItem: MaterialSetTag = materialToAddMore.toSetTag()
        val applier = OnApply.enchantment(maxLevel, enchantment)
        val loreMaker = OnLore.Limited.enchantment(maxLevel, enchantment, materialToAddMore, materialColor)
        override fun onApply(mainItem: ItemStack, appliedItem: ItemStack, amountOfItemsToUse: AmountOfItemsToUse, player: Player) {
            applier(mainItem, appliedItem, amountOfItemsToUse, player)
        }

        override fun loreToAdd(item: ItemStack) = loreMaker(item)

        override fun enchantLoreToAdd(item: ItemStack): List<Component> = emptyList()
    }

    abstract class PDCEnchantmentAppliable(override val mainItem: MaterialSetTag, headerName: String, whatsGiven: String,
                                           quantityGiven: String, materialToAddMore: Material, maxLevel: Int, val levelKey: NamespacedKey,
                                           materialColor: String) : AppliableItem {
        override val appliedItem: MaterialSetTag = materialToAddMore.toSetTag()
        val applier = OnApply.pdc(maxLevel, levelKey) { item, newLevel, appliedNumber ->
            onAppliedWhenUsingPDC(item, newLevel, appliedNumber)
        }
        val loreMaker = OnLore.Limited.pdc(maxLevel, headerName, whatsGiven, quantityGiven, levelKey, materialToAddMore, materialColor)
        val enchantmentLoreMaker = OnEnchantmentLore.pdcEnchantment(headerName, levelKey)

        override fun onApply(mainItem: ItemStack, appliedItem: ItemStack, amountOfItemsToUse: AmountOfItemsToUse, player: Player) {
            applier(mainItem, appliedItem, amountOfItemsToUse, player)
        }
        open fun onAppliedWhenUsingPDC(item: ItemStack, newLevel: Int, appliedNumber: Int) {}
        override fun loreToAdd(item: ItemStack) = loreMaker(item)
        override fun enchantLoreToAdd(item: ItemStack) = enchantmentLoreMaker(item)

        fun level(item: ItemStack, levelKey: NamespacedKey): Int = getLevel(item, levelKey)
    }

    object OnApply {
        internal fun enchantment(maxLevel: Int, enchantment: Enchantment): Applier {
            return { mainItem: ItemStack, appliedItem: ItemStack, amountOfItemsToUse: AmountOfItemsToUse, _: Player ->
                val currentLevel = mainItem.level(enchantment)
                if (currentLevel < 3) {
                    var addable = appliedItem.amount.coerceAtMost(maxLevel - currentLevel)
                    if (addable > 1 && amountOfItemsToUse == AmountOfItemsToUse.ONE) {
                        addable = 1
                    }
                    if (addable > 0) {
                        mainItem.level(enchantment, currentLevel + addable)
                        appliedItem.amount -= addable
                    }
                }
            }
        }

        fun pdc(maxLevel: Int, levelKey: NamespacedKey, doAfter: (item: ItemStack, newLevel: Int, appliedNumber: Int) -> Unit): Applier {
            return { mainItem: ItemStack, appliedItem: ItemStack, amountOfItemsToUse: AmountOfItemsToUse, _: Player ->
                val currLvl = NBTUtil.getAsInt(mainItem.itemMeta, levelKey).orElse(0)
                if (currLvl < 5) {
                    var addable = appliedItem.amount.coerceAtMost(maxLevel-currLvl)
                    if (addable > 1 && amountOfItemsToUse == AmountOfItemsToUse.ONE) {
                        addable = 1
                    }

                    if (addable > 0) {
                        val newLvl = currLvl + addable
                        ItemBuilder.from(mainItem).set(levelKey, newLvl)
                        doAfter(mainItem, newLvl, addable)
                        appliedItem.amount -= addable
                    }
                }
            }
        }
    }

    object OnLore {
        object Limited {
            internal fun enchantment(maxLevel: Int, enchantment: Enchantment, materialToAddMore: Material, materialColor: String): LoreMaker {
                val translatedEnchantment = "<lang:${enchantment.translationKey()}>"
                return transformItem@{
                    val enchantmentLevel = it.level(enchantment)
                    val enchantmentLevelsLeft = (maxLevel - enchantmentLevel).coerceAtLeast(0)
                    val enchantmentLevelColor = if (enchantmentLevel == maxLevel) "red" else "green"
                    return@transformItem listOf(
                            "<white><b>$translatedEnchantment </b><gray>(<white><b><$enchantmentLevelColor>${enchantmentLevelsLeft}</b> Left</white>)".mm(),
                            applyItemLine(materialToAddMore, materialColor),
                            " → Gives <green>+1</green> $translatedEnchantment level".mm()
                    )
                }
            }

            fun pdc(maxLevel: Int, headerName: String, whatsGiven: String, quantityGiven: String, levelKey: NamespacedKey, materialToAddMore: Material, materialColor: String): LoreMaker {
                return transformItem@{
                    val enchantmentLevel = NBTUtil.getAsInt(it.itemMeta, levelKey).orElse(0)
                    val enchantmentLevelsLeft = (maxLevel - enchantmentLevel).coerceAtLeast(0)
                    val enchantmentLevelColor = if (enchantmentLevel == maxLevel) "red" else "green"
                    return@transformItem listOf(
                            "<white><b>$headerName </b><gray>(<white><b><$enchantmentLevelColor>${enchantmentLevelsLeft}</b> Left</white>)".mm(),
                            applyItemLine(materialToAddMore, materialColor),
                            " → Gives <green>+$quantityGiven</green> $whatsGiven".mm()
                    )
                }
            }
        }

        object Unlimited {
            fun custom(headerName: String, whatsGiven: String, quantityGiven: String, materialToAddMore: Material, materialColor: String): (ItemStack) -> List<Component> {
                return transformItem@{
                    return@transformItem listOf(
                            "<white><b>$headerName".mm(),
                            applyItemLine(materialToAddMore, materialColor),
                            " → Gives <green>+$quantityGiven</green> $whatsGiven".mm()
                    )
                }
            }
        }
    }

    object OnEnchantmentLore {
        fun pdcEnchantment(enchantmentName: String, levelKey: NamespacedKey): LoreMaker {
            return cb@{
                val lvl = getLevel(it, levelKey)
                if (lvl == 0) return@cb emptyList()
                return@cb listOf("<gray>$enchantmentName ${convertToRoman(lvl)}".mm())
            }
        }
    }
}