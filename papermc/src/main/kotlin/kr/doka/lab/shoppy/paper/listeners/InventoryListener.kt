package kr.doka.lab.shoppy.paper.listeners

import kr.doka.lab.shoppy.paper.shoppy.ShoppyData
import kr.doka.lab.shoppy.paper.shoppy.ShoppyInventory
import kr.doka.lab.shoppy.paper.shoppy.ShoppyInventoryType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType

class InventoryListener : Listener {
    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        val holder = inventory.getHolder(false) ?: return
        if (holder !is ShoppyInventory) return
        val shoppyInventory: ShoppyInventory = holder

        val page = shoppyInventory.page

        if (shoppyInventory.type == ShoppyInventoryType.EDIT) {
            for (i in 0..44) {
                val l = shoppyInventory.shoppy.list.filter { it.id == i && it.page == page}
                if (l.isEmpty())continue
                val item = l[0].item
                if (event.clickedInventory!!.getItem(i) == item) continue

            }
        }
        event.isCancelled = true
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        val inventory = event.inventory
        val holder = inventory.getHolder(false) ?: return
        if (holder !is ShoppyInventory) return
        val shoppyInventory: ShoppyInventory = holder
        val page = shoppyInventory.page

        if (shoppyInventory.type != ShoppyInventoryType.EDIT) return
        // save logic
        for (i in 0..44) {
            val l = shoppyInventory.shoppy.list.filter { it.id == i && it.page == page}
            if (l.isEmpty())continue
            val item = l[0].item
            if (event.inventory.getItem(i) == item) continue
            shoppyInventory.shoppy.list.remove(l[0])
            // 새로운 아이템이 들어옴.
            if(event.inventory.getItem(i) == null) continue

            shoppyInventory.shoppy.list.add(ShoppyData(page, i%9,i/9,event.inventory.getItem(i)!!, 0.0, 0.0))
        }
    }
}
