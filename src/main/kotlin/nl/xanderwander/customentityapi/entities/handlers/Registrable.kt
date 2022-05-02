package nl.xanderwander.customentityapi.entities.handlers

import nl.xanderwander.customentityapi.Main
import org.bukkit.entity.Player

@Suppress("UNCHECKED_CAST")
abstract class Registrable<T: Registrable<T>> {

    val viewers: ArrayList<Player> = arrayListOf()

    fun register(): T {
        Main.entityManager.registry.add(this)
        return this as T
    }

    fun unregister(): T {
        Main.entityManager.registry.remove(this)
        return this as T
    }

    open fun pluginDisabled() {}

    open fun addViewer(player: Player) {
        viewers.add(player)
    }

    open fun remViewer(player: Player) {
        viewers.remove(player)
    }

}