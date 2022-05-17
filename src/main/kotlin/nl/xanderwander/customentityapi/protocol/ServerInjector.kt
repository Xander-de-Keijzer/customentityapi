package nl.xanderwander.customentityapi.protocol

import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket
import net.minecraft.network.protocol.status.ServerStatus
import nl.xanderwander.customentityapi.Main
import org.bukkit.Bukkit

class ServerInjector {

    var name: String = "Paper"

    init {
        Main.protocol.onOutPacket { container ->
            val packet = container.packet
            if (packet is ClientboundStatusResponsePacket) {
                val status = packet.status
                status.version = ServerStatus.Version(name + " " + Bukkit.getMinecraftVersion(), status.version?.protocol ?: 0)
                container.packet = ClientboundStatusResponsePacket(status)
            }
        }
    }

}