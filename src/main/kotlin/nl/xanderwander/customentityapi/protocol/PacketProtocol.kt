package nl.xanderwander.customentityapi.protocol

import io.netty.channel.Channel
import nl.xanderwander.customentityapi.protocol.objects.PacketContainer
import nl.xanderwander.customentityapi.protocol.reflection.TinyProtocol
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class PacketProtocol(plugin: Plugin): TinyProtocol(plugin) {

    private val outListeners = arrayListOf<(packet: PacketContainer) -> Unit>()
    private val inListeners = arrayListOf<(packet: PacketContainer) -> Unit>()

    override fun onPacketOutAsync(receiver: Player?, channel: Channel, packet: Any): Any? {
        return onPacketAsync(receiver, packet, outListeners)
    }

    override fun onPacketInAsync(sender: Player?, channel: Channel, packet: Any): Any? {
        return onPacketAsync(sender, packet, inListeners)
    }

    private fun onPacketAsync(
        sender: Player?,
        packet: Any,
        listeners: ArrayList<(any: PacketContainer) -> Unit>
    ): Any? {
        val container = PacketContainer(sender, packet)
        listeners.forEach { it.invoke(container) }
        return if (container.isCancelled) null else container.packet
    }

    /**
     * Register an outgoing packet listener.
     * @param f - the lambda to be called.
     */
    fun onOutPacket(f: (packet: PacketContainer) -> Unit) {
        outListeners.add(f)
    }

    /**
     * Register an incoming packet listener.
     * @param f - the lambda to be called.
     */
    fun onInPacket(f: (packet: PacketContainer) -> Unit) {
        inListeners.add(f)
    }

}