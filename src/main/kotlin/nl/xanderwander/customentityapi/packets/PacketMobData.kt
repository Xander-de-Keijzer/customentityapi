package nl.xanderwander.customentityapi.packets

import net.minecraft.core.Rotations
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.entity.decoration.ArmorStand
import nl.xanderwander.customentityapi.entities.Entity
import protocol.Reflection
import java.util.*

class PacketMobData(
    id: Int,
    name: String = "",
    private val nameVisible: Boolean = false,
    small: Boolean = false,
    arms: Boolean = false,
    basePlate: Boolean = false,
    marker: Boolean = false,
    visible: Boolean = true,
    glowing: Boolean = false,
    headRotation: Rotations = Rotations(0F, 0F, 0F)
): Packet() {

    constructor(entity: Entity) : this(entity.id, entity.name, entity.nameVisible, entity.small, entity.arms, entity.basePlate, entity.marker, entity.visible, entity.glowing, entity.headRotation)

    private val byteArmorStand = (if (small) 1 else 0) + (if (arms) 4 else 0) + (if (!basePlate) 8 else 0) + (if (marker) 16 else 0)
    private val byteEntity = (if (!visible) 32 else 0) + (if (glowing) 64 else 0)

    init {
        buf.writeVarInt(id)
        val dataClientFlags = ArmorStand.DATA_CLIENT_FLAGS

        writeFlags(dataClientFlags.serializer, 0, byteEntity.toByte())
        writeFlags(dataClientFlags, byteArmorStand.toByte())
        writeRotation(headRotation)
        writeName(name)

        buf.writeByte(255) // End of data with 255
        super.packet = ClientboundSetEntityDataPacket(buf)
    }

    private fun writeRotation(rotation: Rotations) {
        val accessor = ArmorStand.DATA_HEAD_POSE
        val serializer = accessor.serializer
        buf.writeByte(accessor.id)
        buf.writeVarInt(EntityDataSerializers.getSerializedId(serializer))
        serializer.write(buf, rotation)
    }

    private fun writeFlags(accessor: EntityDataAccessor<Byte>, data: Byte) {
        writeFlags(accessor.serializer, accessor.id, data)
    }

    private fun writeFlags(serializer: EntityDataSerializer<Byte>, id: Int, data: Byte) {
        buf.writeByte(id)
        buf.writeVarInt(EntityDataSerializers.getSerializedId(serializer))
        serializer.write(buf, data)
    }

    private fun writeName(name: String) {
        val fromString = Reflection.getMethod("{obc}.util.CraftChatMessage", "fromString", String::class.java)
        val optComponentArray = fromString.invoke(null, name) as Array<*>
        val optComponent = optComponentArray[0] as Component
        val optChatData = Optional.of(optComponent)
        val stringSerializer = EntityDataSerializers.OPTIONAL_COMPONENT
        buf.writeByte(2)
        buf.writeVarInt(EntityDataSerializers.getSerializedId(stringSerializer))
        stringSerializer.write(buf, optChatData)

        val booleanSerializer = EntityDataSerializers.BOOLEAN
        buf.writeByte(3)
        buf.writeVarInt(EntityDataSerializers.getSerializedId(booleanSerializer))
        booleanSerializer.write(buf, nameVisible)

    }

}