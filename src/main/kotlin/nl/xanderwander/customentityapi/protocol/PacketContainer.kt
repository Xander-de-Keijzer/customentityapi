package nl.xanderwander.customentityapi.protocol

import org.bukkit.entity.Player

data class PacketContainer (
    var player: Player?,
    var packet: Any,
    var isCancelled: Boolean = false
)