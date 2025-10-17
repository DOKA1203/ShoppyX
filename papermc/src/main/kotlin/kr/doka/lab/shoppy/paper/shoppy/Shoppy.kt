package kr.doka.lab.shoppy.paper.shoppy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.doka.lab.shoppy.paper.ShoppyPlugin.Companion.pluginScope
import kr.doka.lab.shoppy.paper.database.ShopItems
import kr.doka.lab.shoppy.paper.database.Shops
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.io.encoding.Base64

class Shoppy(val name: String) {
    val list: ArrayList<ShoppyData> = arrayListOf()

    fun open(player: Player) {
    }

    init {
        pluginScope.launch {
            withContext(Dispatchers.IO) {
                transaction {
                    // 'default' 상점의 ID를 먼저 찾음
                    val shopId = Shops.selectAll().where { Shops.name eq name }.singleOrNull()?.get(Shops.id)

                    if (shopId == null) {
                        return@transaction
                    }

                    // 해당 상점 ID에 속한 모든 아이템을 조회
                    ShopItems.selectAll().where { ShopItems.shop eq shopId }
                        .map { row ->
                            val item = ItemStack.deserializeBytes(Base64.decode(row[ShopItems.itemStackBase64]))
                            val data =
                                ShoppyData(
                                    row[ShopItems.page],
                                    row[ShopItems.slot] % 9,
                                    row[ShopItems.slot] / 9,
                                    item,
                                    row[ShopItems.sellPrice],
                                    row[ShopItems.buyPrice],
                                )
                            list.add(data)
                        }
                }
            }
        }
    }

    fun save() {
        pluginScope.launch {
            val row = Shops.selectAll().where { Shops.name eq this@Shoppy.name }.singleOrNull()
            var shopId = row?.get(Shops.id)
            if (row != null) {
                shopId =
                    Shops.insertAndGetId {
                        it[name] = this@Shoppy.name
                        it[size] = 54
                    }
            }

            list.forEach { shoppyData ->
                ShopItems.insert {
                    it[shop] = shopId!!
                    it[slot] = shoppyData.id
                    it[itemStackBase64] = Base64.encode(shoppyData.item.serializeAsBytes())
                    it[sellPrice] = shoppyData.sellPrice // 팔기 불가
                    it[buyPrice] = shoppyData.buyPrice // 사기만 가능
                }
            }
        }
    }
}
