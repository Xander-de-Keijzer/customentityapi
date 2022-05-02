package nl.xanderwander.customentityapi.packets

import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import nl.xanderwander.customentityapi.entities.Entity

class PacketRemoveMob(
    id: Int,
): Packet() {

    constructor(entity: Entity): this(entity.id)

    init {
        super.packet = ClientboundRemoveEntitiesPacket(id)
    }

}