package kr.doka.lab.shoppy.paper.shoppy

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class ShoppyInventory(val shoppy: Shoppy, val player: Player) : InventoryHolder {
    private val inventory: Inventory = Bukkit.createInventory(this, 9, Component.text("Shop - ${shoppy.name}"))
    private var page: Int = 1

    init {
        player.openInventory(inventory)
    }

    private fun loadPage(p: Int) {
        inventory.clear()

        val next = ItemStack.of(Material.ARROW)
        val previous = ItemStack.of(Material.ARROW)

        val glass = ItemStack.of(Material.GLASS_PANE)

        glass.itemMeta =
            glass.itemMeta?.apply {
                displayName(Component.empty())
            }
        next.itemMeta =
            next.itemMeta?.apply {
                displayName(Component.text("다음 페이지"))
            }
        previous.itemMeta =
            previous.itemMeta?.apply {
                displayName(Component.text("이전 페이지"))
            }

        for (i in 0 until 9) {
            inventory.setItem(45 + i, glass)
        }
        inventory.setItem(53, next)
        // TODO 방향키 제작
        if (p != 1) {
            inventory.setItem(45, previous)
        }

        shoppy.list.filter { it.page == p }.forEach {
            inventory.setItem(it.id, it.item)
        }
    }

    fun nextPage() {
        loadPage(++page)
    }

    fun previousPage() {
        loadPage(--page)
    }

    override fun getInventory(): Inventory = inventory
}
