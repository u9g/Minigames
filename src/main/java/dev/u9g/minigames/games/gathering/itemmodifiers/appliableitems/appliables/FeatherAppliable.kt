package dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.appliables

import com.destroystokyo.paper.MaterialTags
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.PrebuiltAppliable
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.UUID

class FeatherAppliable : PrebuiltAppliable.PDCEnchantmentAppliable(
        mainItem = MaterialTags.LEGGINGS,
        headerName = "Speed",
        whatsGiven = "speed",
        quantityGiven = "5%",
        materialToAddMore = Material.FEATHER,
        maxLevel = 5,
        levelKey = NamespacedKey.fromString("minigames:speed_ench")!!,
        materialColor = "white") {
    override fun onAppliedWhenUsingPDC(item: ItemStack, newLevel: Int, appliedNumber: Int) {
        item.editMeta {
            it.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED,
                    AttributeModifier(
                            UUID.randomUUID(),
                            "SPEED_ENCH" + UUID.randomUUID(),
                            appliedNumber * 0.05,
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            EquipmentSlot.LEGS
                    )
            )
        }
    }
}