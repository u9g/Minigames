package dev.u9g.minigames

import com.destroystokyo.paper.profile.ProfileProperty
import com.github.u9g.u9gutils.NBTUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

fun makeItem(material: Material,
             count: Int = 1,
             customModelData: Int? = null,
             name: Component? = null,
             lore: List<Component>? = null,
             flags: List<ItemFlag>? = null,
             unbreakable: Boolean = false,
             headTexture: String? = null,
             enchantments: Map<Enchantment, Int>? = null,
             pdc: Map<NamespacedKey, Any>? = null
): ItemStack {
    val item = ItemStack(material)
    val itemMeta = item.itemMeta
    // custom model data
    customModelData?.let { itemMeta.setCustomModelData(it) }
    // name
    name?.let { itemMeta.displayName(it.colorIfAbsent(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)) }
    // lore
    lore?.let { itemMeta.lore(it.map { c -> c.colorIfAbsent(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false) }) }
    // flags
    flags?.let { itemMeta.addItemFlags(*it.toTypedArray()) }
    // unbreakable
    itemMeta.isUnbreakable = unbreakable
    // headTexture
    headTexture?.let {
        if (material == Material.PLAYER_HEAD && itemMeta is SkullMeta) {
            val profile = Bukkit.createProfile(UUID.randomUUID())
            profile.setProperty(ProfileProperty("textures", headTexture));
            itemMeta.playerProfile = profile;
        } else {
            throw AssertionError("Not a player head");
        }
    }
    // enchantments
    enchantments?.let { enchs -> enchs.forEach {
            val (ench, lvl) = it
            itemMeta.addEnchant(ench, lvl, true)
    } }
    // pdc
    pdc?.let { keys -> keys.forEach {
            val (k, v) = it
            when (v) {
                is Int -> NBTUtil.set(itemMeta, k, v)
                is Double -> NBTUtil.set(itemMeta, k, v)
                is Float -> NBTUtil.set(itemMeta, k, v)
                is String -> NBTUtil.set(itemMeta, k, v)
                is Long -> NBTUtil.set(itemMeta, k, v)
                is Boolean -> NBTUtil.set(itemMeta, k, v)
                is Byte -> NBTUtil.set(itemMeta, k, v == 1.toByte())
                else -> throw IllegalArgumentException("Unexpected type for key: $k")
            }
    } }
    item.itemMeta = itemMeta
    return item.asQuantity(count)
}