package kr.doka.lab.shoppy.paper.listeners

import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.event.player.PlayerCustomClickEvent
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import kr.doka.lab.shoppy.paper.ShoppyPlugin.Companion.econ
import kr.doka.lab.shoppy.paper.ShoppyPlugin.Companion.instance
import kr.doka.lab.shoppy.paper.shoppy.ShoppyData
import kr.doka.lab.shoppy.paper.shoppy.ShoppyInventory
import kr.doka.lab.shoppy.paper.shoppy.ShoppyInventoryType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.UUID
import kotlin.math.round

class InventoryListener : Listener {
    fun savePage(
        shoppy: ShoppyInventory,
        inv: Inventory,
        p: Int,
    ) {
        for (i in 0..44) {
            val l = shoppy.shoppy.list.filter { it.id == i && it.page == p }
            if (l.isEmpty()) {
                val invItem =
                    inv.getItem(i)?.apply {
                        amount = 1
                    }
                if (invItem == null) continue

                shoppy.shoppy.list.add(ShoppyData(p, i % 9, i / 9, invItem, 0.0, 0.0))
            } else {
                val item = l[0].item
                val invItem =
                    inv.getItem(i)?.apply {
                        amount = 1
                    }
                if (invItem == item) continue
                shoppy.shoppy.list.remove(l[0])
                // 새로운 아이템이 들어옴.
                if (invItem == null) continue

                shoppy.shoppy.list.add(ShoppyData(p, i % 9, i / 9, invItem, 0.0, 0.0))
            }
        }
        shoppy.shoppy.save()
    }

    @EventHandler
    fun editClickHandle(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        val holder = inventory.getHolder(false) ?: return
        if (holder !is ShoppyInventory) return

        if (holder.type != ShoppyInventoryType.EDIT) return

        val page = holder.page
        event.isCancelled = true

        when (event.slot) {
            45 -> { // 이전 창.
                if (page == 1) return
                savePage(holder, inventory, page)
                holder.previousPage()
            }
            53 -> { // 다음 창.
                // if (page == holder.shoppy.maxPage) return
                savePage(holder, inventory, page)
                holder.nextPage()
            }
            !in 46..52 -> {
                event.isCancelled = false
            }
        }
    }

    @EventHandler
    fun editCloseHandle(event: InventoryCloseEvent) {
        val inventory = event.inventory
        val holder = inventory.getHolder(false) ?: return
        if (holder !is ShoppyInventory) return
        if (holder.type != ShoppyInventoryType.EDIT) return

        savePage(holder, inventory, holder.page)
    }

    @EventHandler
    fun mainClickHandle(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        val holder = inventory.getHolder(false) ?: return
        if (holder !is ShoppyInventory) return
        if (holder.type != ShoppyInventoryType.MAIN) return

        val page = holder.page

        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return
        if (event.slot == 45) { // 이전 창.
            if (page == 1) return
            holder.previousPage()
        } else if (event.slot == 53) { // 다음 창.
            if (page == holder.shoppy.maxPage) return
            holder.nextPage()
        }

        val l = holder.shoppy.list.filter { it.id == event.slot && it.page == page }
        if (l.isEmpty())return
        val item = l[0]

        if (event.isLeftClick) { // buy
            var buyAmount = 1
            if (event.isShiftClick) { // 1set buy
                buyAmount = item.item.maxStackSize
            }
            if (econ.getBalance(player) < buyAmount * item.buyPrice) {
                val msg = instance.config.getString("messages.buy.error.not-enough-money")!!
                player.sendMessage(MiniMessage.miniMessage().deserialize(msg))
                return
            }
            if (!isCanHold(player, item.item)) {
                val msg = instance.config.getString("messages.buy.error.inventory-full")!!
                player.sendMessage(MiniMessage.miniMessage().deserialize(msg))
                return
            }

            player.inventory.addItem(
                item.item.clone().apply {
                    amount = buyAmount
                },
            )
            val msg =
                instance.config.getString("messages.buy.success")!!
                    .replace("<PRICE>", round(buyAmount * item.buyPrice).toString())
            player.sendMessage(MiniMessage.miniMessage().deserialize(msg))
            econ.withdrawPlayer(player, buyAmount * item.buyPrice)
        } else if (event.isRightClick) { // sell
            val playerAmount =
                player.inventory.storageContents
                    .filterNotNull() // null이 아닌 슬롯만 필터링
                    .filter { it.isSimilar(item.item) } // 비슷한 아이템인지 확인
                    .sumOf { it.amount }
            if (playerAmount < 1) {
                val msg = instance.config.getString("messages.sell.error.no-item-to-sell")!!
                player.sendMessage(MiniMessage.miniMessage().deserialize(msg))
                return
            }
            val amountToSell =
                if (event.click.isShiftClick) {
                    playerAmount
                } else {
                    1
                }

            val itemToRemove = item.item.clone().apply { amount = amountToSell }
            player.inventory.removeItem(itemToRemove)

            econ.depositPlayer(player, item.sellPrice * amountToSell)
            val msg = instance.config.getString("messages.sell.success")!!
            msg.replace("<PRICE>", round(amountToSell * item.sellPrice).toString())
            player.sendMessage(MiniMessage.miniMessage().deserialize(msg))
        }
    }

    @EventHandler
    fun underClickHandle(event: InventoryClickEvent) {
        // val player: Player = event.whoClicked as Player
        val clickedInventory = event.clickedInventory
        val topInventory = event.view.topInventory
        val holder = topInventory.getHolder(false) ?: return
        if (clickedInventory == topInventory)return
        if (holder !is ShoppyInventory) return
        // 상점 열기 또는 설정에서 밑 인벤을 눌렀을 때.
        if (holder.type == ShoppyInventoryType.EDIT)return
        event.isCancelled = true
    }

    val hashMap = HashMap<UUID, ShoppyData>()

    @EventHandler
    fun priceClickHandle(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        val holder = inventory.getHolder(false) ?: return
        if (holder !is ShoppyInventory) return
        if (holder.type != ShoppyInventoryType.PRICE) return
        if (event.currentItem == null) return
        event.isCancelled = true
        // val player = event.whoClicked as Player
        val s = holder.shoppy.list.filter { it.id == event.slot && it.page == holder.page }
        if (s.isEmpty()) return

        hashMap[event.whoClicked.uniqueId] = s[0]

        val dialog =
            Dialog.create { builder ->
                builder.empty()
                    .base(
                        DialogBase.builder(Component.text("선택한 아이템의 가격을 설정하세요"))
                            .body(
                                listOf(
                                    DialogBody.item(s[0].item).build(),
                                ),
                            )
                            .inputs(
                                listOf(
                                    DialogInput.text("sell_price", Component.text("판매 가격"))
                                        .initial(s[0].sellPrice.toString())
                                        .build(),
                                    DialogInput.text("buy_price", Component.text("구매 가격"))
                                        .initial(s[0].buyPrice.toString())
                                        .build(),
                                ),
                            )
                            .build(),
                    )
                    .type(
                        DialogType.confirmation(
                            ActionButton.create(
                                Component.text("설정하기", TextColor.color(0xAEFFC1)),
                                Component.text("클릭해서 가격을 설정합니다"),
                                100,
                                DialogAction.customClick(Key.key("shoppy:user_input/confirm"), null),
                            ),
                            ActionButton.create(
                                Component.text("닫기", TextColor.color(0xFFA0B1)),
                                Component.text("가격을 설정을 취소합니다."),
                                100,
                                null,
                            ),
                        ),
                    )
            }
        event.whoClicked.showDialog(dialog)
    }

    @EventHandler
    fun handleLevelsDialog(event: PlayerCustomClickEvent) {
        if (event.identifier != Key.key("shoppy:user_input/confirm")) {
            return
        }

        val view = event.dialogResponseView ?: return

        val sellPrice = view.getText("sell_price")!!.toDouble()
        val buyPrice = view.getText("buy_price")!!.toDouble()
        val conn = event.commonConnection
        if (conn is PlayerGameConnection) {
            val player: Player = conn.player

            val holder = player.openInventory.topInventory.holder
            if (holder !is ShoppyInventory) return

            val s = hashMap[player.uniqueId] ?: return
            holder.shoppy.list.remove(s)

            s.sellPrice = sellPrice
            s.buyPrice = buyPrice

            holder.shoppy.list.add(s)
            holder.shoppy.save()
            hashMap.remove(player.uniqueId)
            holder.reload()
        }
    }

    fun isCanHold(
        player: Player,
        item: ItemStack,
    ): Boolean {
        val testInv = Bukkit.createInventory(null, 36)
        testInv.contents = player.inventory.storageContents.clone()
        return testInv.addItem(item).isEmpty()
    }
}
