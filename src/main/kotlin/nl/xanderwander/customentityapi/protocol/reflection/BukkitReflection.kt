package nl.xanderwander.customentityapi.protocol.reflection

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_18_R2.CraftServer
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.Player

class BukkitReflection {
    companion object {
        fun handle(player: Player): ServerPlayer {
            return (player as CraftPlayer).handle
        }
        fun server(): MinecraftServer {
            return ((Bukkit.getServer() as CraftServer).server as MinecraftServer)
        }
    }
}