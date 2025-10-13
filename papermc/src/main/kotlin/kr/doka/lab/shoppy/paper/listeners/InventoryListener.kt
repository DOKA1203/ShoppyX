package kr.doka.lab.shoppy.paper.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

class InventoryListener : Listener {
    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
    }
}
