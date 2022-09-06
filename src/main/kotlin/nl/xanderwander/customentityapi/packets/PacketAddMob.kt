package nl.xanderwander.customentityapi.packets

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
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
        super.packet = ClientboundAddEntityPacket(
            id,
            UUID.randomUUID(),
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch,
            EntityType.ARMOR_STAND,
            0,
            Vec3(0.0, 0.0 ,0.0),
            0.0
        )
    }

}