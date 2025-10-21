package kr.doka.lab.shoppy.paper.shoppy

import kr.doka.lab.shoppy.paper.ShoppyPlugin.Companion.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

enum class ShoppyInventoryType {
    MAIN,
    EDIT,
    PRICE,
}

class ShoppyInventory(val shoppy: Shoppy, val player: Player, val type: ShoppyInventoryType) : InventoryHolder {
    private val inventory: Inventory = Bukkit.createInventory(this, 54, Component.text("Shop - ${shoppy.name}"))
    var page: Int = 1

    init {
        loadPage(page)
        player.openInventory(inventory)
    }
    fun reload() = loadPage(page)
    private fun loadPage(p: Int) {
        inventory.clear()

        val next = ItemStack.of(Material.ARROW)
        val previous = ItemStack.of(Material.ARROW)

        val glass = ItemStack.of(Material.WHITE_STAINED_GLASS_PANE)

        glass.itemMeta =
            glass.itemMeta?.apply {
                displayName(Component.empty())
            }
        next.itemMeta =
            next.itemMeta?.apply {
                displayName(Component.text("다음 페이지 (p.${p + 1})").decoration(TextDecoration.ITALIC, false))
            }
        previous.itemMeta =
            previous.itemMeta?.apply {
                displayName(Component.text("이전 페이지 (p.${p - 1})").decoration(TextDecoration.ITALIC, false))
            }

        for (i in 0 until 9) {
            inventory.setItem(45 + i, glass)
        }
        if (p != shoppy.maxPage || type == ShoppyInventoryType.EDIT) {
            inventory.setItem(53, next)
        }

        if (p != 1) {
            inventory.setItem(45, previous)
        }

        val newLore = instance.config.getStringList("lore")
        shoppy.list.filter { it.page == p }.forEach {
            if (type != ShoppyInventoryType.EDIT) {
                val item = it.item.clone()
                item.itemMeta =
                    item.itemMeta.apply {
                        val currentLore = lore() ?: mutableListOf()
                        val newLore =
                            newLore.map { l ->
                                l.replace("<SELL_PRICE>", it.sellPrice.toString())
                                    .replace("<BUY_PRICE>", it.buyPrice.toString())
                                    .replace("<MAX_STACK>", it.item.maxStackSize.toString())
                            }

                        newLore.forEach { l ->
                            currentLore.add(MiniMessage.miniMessage().deserialize(l).decoration(TextDecoration.ITALIC, false))
                        }
                        lore(currentLore)
                    }
                inventory.setItem(it.id, item)
            } else {
                inventory.setItem(it.id, it.item)
            }
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
