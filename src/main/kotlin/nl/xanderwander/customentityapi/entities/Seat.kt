package nl.xanderwander.customentityapi.entities

import net.minecraft.core.Rotations
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import nl.xanderwander.customentityapi.Main
import nl.xanderwander.customentityapi.packets.PacketSetPassengers
import nl.xanderwander.customentityapi.utils.Utils
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class Seat(
    loc: Location,
    name: String = "",
    nameVisible: Boolean = false,
    small: Boolean = false,
    arms: Boolean = false,
    basePlate: Boolean = false,
    marker: Boolean = false,
    visible: Boolean = false,
    headRotation: Rotations = Rotations(0F, 0F, 0F),
): Entity(loc, name, nameVisible, small, arms, basePlate, marker, visible, headRotation) {

    private var passenger: Player? = null
    private var passengerYaw = 0f
    private var passengerPitch = 0f

    init {

        Main.protocol.onInPacket onInPacket@ { container ->
            val packet = container.packet
            val player = container.player ?: return@onInPacket
            if (packet is ServerboundInteractPacket) {
                if (packet.entityId == id) setPassenger(player)
            }
            if (packet is ServerboundPlayerInputPacket) {
                if (player == passenger) {
                    if (packet.isShiftKeyDown) remPassenger()
                }
            }
            if (packet is ServerboundMovePlayerPacket) {
                if (player == passenger) {
                    passengerYaw = packet.yRot
                    passengerPitch = packet.xRot
                }
            }
        }

        Main.protocol.onOutPacket onOutPacket@ { container ->
            if (container.packet is ClientboundPlayerPositionPacket) {
                if (container.player == passenger) container.isCancelled = true
            }
        }

    }

    fun setPassenger(player: Player) {
        if (passenger != null) return
        passenger = player
        PacketSetPassengers(id, passenger).sendAll()
    }

    fun remPassenger() {
        PacketSetPassengers(id).sendAll()
        val player = passenger ?: return
        passenger = null

        Utils.runSync {
            player.teleport(loc.passengerCorrected())
        }
    }

    override fun correction(newLoc: Location) {
        passenger?.let { passenger ->
            Utils.runSync {
                passenger.teleport(newLoc.passengerCorrected())
                PacketSetPassengers(id, passenger).sendAll()
            }
        }
        super.correction(newLoc)
    }

    override fun addViewer(player: Player) {
        super.addViewer(player)
        PacketSetPassengers(id, passenger).send(player)
    }

    override fun remViewer(player: Player) {
        PacketSetPassengers(id).send(player)
        super.remViewer(player)
    }

    override fun destroy() {
        PacketSetPassengers(id).sendAll()
        super.destroy()
    }

    private fun Location.passengerCorrected(): Location {
        return Location(this.world, this.x, this.y + 1.13114, this.z, passengerYaw, passengerPitch)
    }

}