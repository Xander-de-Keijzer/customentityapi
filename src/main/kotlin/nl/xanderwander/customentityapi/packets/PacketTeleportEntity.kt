package nl.xanderwander.customentityapi.packets

import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import org.bukkit.Location

class PacketTeleportEntity(entityID: Int, target: Location): Packet() {

    init {
        val yaw = (target.yaw * 256.0) / 360.0
        val pitch = (target.pitch * 256.0) / 360.0
        buf.writeVarInt(entityID)
            .writeDouble(target.x)
            .writeDouble(target.y)
            .writeDouble(target.z)
            .writeByte(yaw.toInt())
            .writeByte(pitch.toInt())
            .writeBoolean(false)
        super.packet = ClientboundTeleportEntityPacket(buf)
    }

}