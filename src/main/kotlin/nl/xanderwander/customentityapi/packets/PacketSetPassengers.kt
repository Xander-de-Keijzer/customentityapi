package nl.xanderwander.customentityapi.packets

import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import org.bukkit.entity.Player

class PacketSetPassengers(): Packet() {

    constructor(entityID: Int, passengers: ArrayList<Player> = arrayListOf()) : this() {
        val passengerIdMap = passengers.map { it.entityId }
        val passengerIds = IntArray(passengerIdMap.size)
        passengerIdMap.forEachIndexed{ index, i -> passengerIds[index] = i }
        create(entityID, passengerIds)
    }

    constructor(entityID: Int, passenger: Player?) : this() {
        val passengerIds = if (passenger == null) IntArray(0) else intArrayOf(passenger.entityId)
        create(entityID, passengerIds)
    }

    private fun create(entityID: Int, passengers: IntArray) {
        buf.writeVarInt(entityID)
        buf.writeVarIntArray(passengers)
        super.packet = ClientboundSetPassengersPacket(buf)
    }

}