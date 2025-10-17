package kr.doka.lab.shoppy.paper.shoppy

import org.bukkit.inventory.ItemStack

class ShoppyData(val page: Int, val x: Int, val y: Int, val item: ItemStack, var sellPrice: Double, var buyPrice: Double) {
    val id: Int = y * 9 + x
}
