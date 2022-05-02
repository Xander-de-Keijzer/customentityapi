package nl.xanderwander.customentityapi.packets

import net.minecraft.network.protocol.game.ClientboundAddMobPacket
import nl.xanderwander.customentityapi.entities.Entity
import org.bukkit.Location
import java.util.*

class PacketAddMob(
    id: Int,
    type: Int,
    location: Location
): Packet() {

    constructor(entity: Entity): this(entity.id, 1, entity.loc)

    init {
        val yaw = (location.yaw * 256.0) / 360.0
        val pitch = (location.pitch * 256.0) / 360.0
        buf.writeVarInt(id)
            .writeUUID(UUID.randomUUID())
            .writeVarInt(type)
            .writeDouble(location.x)
            .writeDouble(location.y)
            .writeDouble(location.z)
            .writeByte(yaw.toInt())
            .writeByte(pitch.toInt())
            .writeByte(0)
            .writeShort(0)
            .writeShort(0)
            .writeShort(0)
        super.packet = ClientboundAddMobPacket(buf)
    }

}