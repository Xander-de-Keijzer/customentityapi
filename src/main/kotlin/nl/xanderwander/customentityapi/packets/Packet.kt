package nl.xanderwander.customentityapi.packets

import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import nl.xanderwander.customentityapi.Main
import org.bukkit.Bukkit
import org.bukkit.entity.Player

abstract class Packet {

    val buf = FriendlyByteBuf(FriendlyByteBuf(Unpooled.buffer()))
    lateinit var packet: Packet<*>

    /**
     * Send this packet to the specified player.
     * @param player the player to send this packet to.
     */
    fun send(player: Player) {
        Main.protocol.sendPacket(player, packet)
    }

    /**
     * Send this packet to the specified players.
     * @param players the players to send this packet to.
     */
    fun send(players: ArrayList<Player>) {
        players.forEach { send(it) }
    }

    /**
     * Send this packet to the specified players.
     * @param players the players to send this packet to.
     */
    fun send(players: Collection<Player>) {
        players.forEach { send(it) }
    }

    /**
     * Send this packet to the all online players.
     * @param players the players to send this packet to.
     */
    fun sendAll() {
        send(Bukkit.getOnlinePlayers())
    }

    /**
     * Get the internal created packet object
     * @return The internal packet object
     */
    fun get(): Packet<*> {
        return packet
    }

}