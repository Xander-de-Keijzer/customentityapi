package nl.xanderwander.customentityapi.packets

import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket
import org.bukkit.Location

class PacketMoveEntity(entityID: Int, origin: Location, target: Location): Packet() {

    init {
        val x = (target.x * 32 - origin.x * 32) * 128
        val y = (target.y * 32 - origin.y * 32) * 128
        val z = (target.z * 32 - origin.z * 32) * 128
        val yaw = (target.yaw * 256.0) / 360.0
        val pitch = (target.pitch * 256.0) / 360.0
        buf.writeVarInt(entityID)
            .writeShort(x.toInt())
            .writeShort(y.toInt())
            .writeShort(z.toInt())
            .writeByte(yaw.toInt())
            .writeByte(pitch.toInt())
            .writeBoolean(false)
        super.packet = ClientboundMoveEntityPacket.PosRot.read(buf)
    }

}