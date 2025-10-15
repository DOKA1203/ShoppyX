package kr.doka.lab.shoppy.paper.shoppy

import org.bukkit.entity.Player

class Shoppy(val name: String) {
    val list: ArrayList<ShoppyData> = arrayListOf()

    fun open(player: Player) {
    }

    init {
        TODO("Load Data From DB")
    }
}
