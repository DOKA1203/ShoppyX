package kr.doka.lab.shoppy.paper.shoppy

import org.bukkit.inventory.ItemStack

class ShoppyData(val page: Int, val x: Int, val y: Int, val item: ItemStack) {
    val id: Int = y * 9 + x
}
