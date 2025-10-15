package kr.doka.lab.shoppy.paper.listeners

import kr.doka.lab.shoppy.paper.shoppy.ShoppyInventory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryListener : Listener {
    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        val holder = inventory.getHolder(false) ?: return
        if (holder !is ShoppyInventory) return
        val shoppyInventory: ShoppyInventory = holder

        event.isCancelled = true
    }
}
