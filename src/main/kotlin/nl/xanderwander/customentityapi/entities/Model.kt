package nl.xanderwander.customentityapi.entities

import net.minecraft.core.Rotations
import nl.xanderwander.customentityapi.packets.PacketSetEquipment
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Model(
    loc: Location,
    name: String = "",
    nameVisible: Boolean = false,
    small: Boolean = false,
    arms: Boolean = false,
    basePlate: Boolean = false,
    marker: Boolean = true,
    visible: Boolean = false,
    glowing: Boolean = false,
    headRotation: Rotations = Rotations(0F, 0F, 0F),
    mainHand: ItemStack = ItemStack(Material.AIR),
    offHand: ItemStack = ItemStack(Material.AIR),
    helmet: ItemStack = ItemStack(Material.AIR),
    chestPlate: ItemStack = ItemStack(Material.AIR),
    leggings: ItemStack = ItemStack(Material.AIR),
    boots: ItemStack = ItemStack(Material.AIR),
): Entity(loc, name, nameVisible, small, arms, basePlate, marker, visible, glowing, headRotation) {

    var mainHand: ItemStack = mainHand
        set(value) { field = value; updateEquipment() }
    var offHand: ItemStack = offHand
        set(value) { field = value; updateEquipment() }
    var helmet: ItemStack = helmet
        set(value) { field = value; updateEquipment() }
    var chestPlate: ItemStack = chestPlate
        set(value) { field = value; updateEquipment() }
    var leggings: ItemStack = leggings
        set(value) { field = value; updateEquipment() }
    var boots: ItemStack = boots
        set(value) { field = value; updateEquipment() }

    override fun addViewer(player: Player) {
        super.addViewer(player)
        PacketSetEquipment(this).send(player)
    }

    private fun updateEquipment() {
        PacketSetEquipment(this).send(viewers)
    }

}