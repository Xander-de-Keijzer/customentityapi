package nl.xanderwander.customentityapi.entities.handlers

import nl.xanderwander.customentityapi.entities.Entity
import org.bukkit.Location
import org.bukkit.entity.Player

class Group(var loc: Location): Registrable<Group>() {

    val entities = arrayListOf<Entity>()

    override fun addViewer(player: Player) {
        entities.forEach { entity -> entity.addViewer(player) }
        super.addViewer(player)
    }

    override fun remViewer(player: Player) {
        entities.forEach { entity -> entity.remViewer(player) }
        super.remViewer(player)
    }

    override fun pluginDisabled() {
        entities.forEach { entity -> entity.destroy() }
    }

}