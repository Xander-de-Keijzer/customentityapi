package nl.xanderwander.customentityapi.packets

import com.mojang.datafixers.util.Pair
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.world.entity.EquipmentSlot
import nl.xanderwander.customentityapi.entities.Model
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import protocol.Reflection

class PacketSetEquipment(
    entityID: Int,
    mainHand: ItemStack = ItemStack(Material.AIR),
    offHand: ItemStack = ItemStack(Material.AIR),
    boots: ItemStack = ItemStack(Material.AIR),
    leggings: ItemStack = ItemStack(Material.AIR),
    chestPlate: ItemStack = ItemStack(Material.AIR),
    helmet: ItemStack = ItemStack(Material.AIR),
): Packet() {

    constructor(model: Model): this(model.id, model.mainHand, model.offHand, model.boots, model.leggings, model.chestPlate, model.helmet)

    init {
        val asNMSCopy = Reflection.getMethod("{obc}.inventory.CraftItemStack", "asNMSCopy", ItemStack::class.java)
        val slots = listOf(
            Pair(EquipmentSlot.MAINHAND, asNMSCopy.invoke(null, mainHand) as net.minecraft.world.item.ItemStack),
            Pair(EquipmentSlot.OFFHAND, asNMSCopy.invoke(null, offHand) as net.minecraft.world.item.ItemStack),
            Pair(EquipmentSlot.FEET, asNMSCopy.invoke(null, boots) as net.minecraft.world.item.ItemStack),
            Pair(EquipmentSlot.LEGS, asNMSCopy.invoke(null, leggings) as net.minecraft.world.item.ItemStack),
            Pair(EquipmentSlot.CHEST, asNMSCopy.invoke(null, chestPlate) as net.minecraft.world.item.ItemStack),
            Pair(EquipmentSlot.HEAD, asNMSCopy.invoke(null, helmet) as net.minecraft.world.item.ItemStack),
        )
        super.packet = ClientboundSetEquipmentPacket(entityID, slots)
    }

}