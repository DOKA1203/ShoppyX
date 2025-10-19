package kr.doka.lab.shoppy.paper.listeners

import kr.doka.lab.shoppy.paper.shoppy.ShoppyData
import kr.doka.lab.shoppy.paper.shoppy.ShoppyInventory
import kr.doka.lab.shoppy.paper.shoppy.ShoppyInventoryType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class InventoryListener : Listener {
    fun savePage(
        shoppyInventory: ShoppyInventory,
        inventory: Inventory,
        currentPage: Int,
    ) {
        for (i in 0..44) {
            val l = shoppyInventory.shoppy.list.filter { it.id == i && it.page == currentPage }
            if (l.isEmpty())continue
            val item = l[0].item
            if (inventory.getItem(i) == item) continue
            shoppyInventory.shoppy.list.remove(l[0])
            // 새로운 아이템이 들어옴.
            if (inventory.getItem(i) == null) continue

            shoppyInventory.shoppy.list.add(ShoppyData(currentPage, i % 9, i / 9, inventory.getItem(i)!!, 0.0, 0.0))
        }
    }

    @EventHandler
    fun editClickHandle(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        val holder = inventory.getHolder(false) ?: return
        if (holder !is ShoppyInventory) return
        val shoppyInventory: ShoppyInventory = holder
        event.isCancelled = true
        val page = shoppyInventory.page

        if (shoppyInventory.type == ShoppyInventoryType.EDIT) {
            // TODO 페이지 이동일 시, 변경사항 저장.
            // 45 53 prev, next
            if (event.slot == 45) { // 이전 창.
                if (page == 1) return

                savePage(shoppyInventory, inventory, page)
                shoppyInventory.previousPage()
            } else if (event.slot == 53) { // 다음 창.
                if (page == shoppyInventory.shoppy.maxPage) return

                savePage(shoppyInventory, inventory, page)
                shoppyInventory.nextPage()
            } else if (event.slot in 46..<53) {
                return
            }
        }
    }

    @EventHandler
    fun editCloseHandle(event: InventoryCloseEvent) {
        val inventory = event.inventory
        val holder = inventory.getHolder(false) ?: return
        if (holder !is ShoppyInventory) return
        val shoppyInventory: ShoppyInventory = holder
        val page = shoppyInventory.page

        if (shoppyInventory.type != ShoppyInventoryType.EDIT) return
        // save logic
        savePage(shoppyInventory, inventory, page)
    }
}
