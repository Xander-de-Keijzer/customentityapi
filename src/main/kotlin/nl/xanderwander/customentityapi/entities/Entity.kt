package nl.xanderwander.customentityapi.entities

import net.minecraft.core.Rotations
import nl.xanderwander.customentityapi.entities.handlers.Registrable
import nl.xanderwander.customentityapi.packets.*
import nl.xanderwander.customentityapi.utils.Utils.Companion.distSqr
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.random.Random

open class Entity(
    loc: Location,
    name: String = "",
    nameVisible: Boolean = false,
    small: Boolean = false,
    arms: Boolean = false,
    basePlate: Boolean = true,
    marker: Boolean = false,
    visible: Boolean = true,
    glowing: Boolean = false,
    headRotation: Vector = Vector(0F, 0F, 0F),
): Registrable<Entity>() {

    var loc = loc.clone()
    var name = name
        set(value) { field = value; updateSettings() }
    var nameVisible = nameVisible
        set(value) { field = value; updateSettings() }
    var small = small
        set(value) { field = value; updateSettings() }
    var arms = arms
        set(value) { field = value; updateSettings() }
    var basePlate = basePlate
        set(value) { field = value; updateSettings() }
    var marker = marker
        set(value) { field = value; updateSettings() }
    var visible = visible
        set(value) { field = value; updateSettings() }
    var glowing = glowing
        set(value) { field = value; updateSettings() }
    var headRotation = headRotation
        set(value) { field = value; updateSettings() }

    val id = Random.nextInt(0, Int.MAX_VALUE)
    var correctionCount = 0
    var lastCorrectLocation = loc

    fun moveTo(newLoc: Location) {
        if (newLoc == loc) return
        if (loc.distSqr(newLoc) > 49 /*7*/ || correctionCount > 60 || loc.distSqr(lastCorrectLocation) > 100 /*10*/) {
            correction(newLoc)
            correctionCount = 0
        } else {
            correctionCount++
            PacketMoveEntity(id, loc, newLoc).send(viewers)
        }
        loc = newLoc
    }

    protected open fun correction(newLoc: Location) {
        val block = newLoc.toBlockLocation()
        PacketTeleportEntity(id, block).send(viewers)
        PacketMoveEntity(id, block, newLoc).send(viewers)
        lastCorrectLocation = newLoc
    }

    override fun addViewer(player: Player) {
        super.addViewer(player)
        PacketAddMob(this).send(player)
        PacketMobData(this).send(player)
    }

    override fun remViewer(player: Player) {
        super.remViewer(player)
        PacketRemoveMob(this).send(player)
    }

    override fun destroy(unregister: Boolean): Entity {
        viewers.forEach { viewer -> PacketRemoveMob(this).send(viewer) }
        viewers.clear()
        if (unregister) unregister()
        return this
    }

    private fun updateSettings() {
        PacketMobData(this).send(viewers)
    }

}