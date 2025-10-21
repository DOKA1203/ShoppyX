package kr.doka.lab.shoppy.paper

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kr.doka.lab.shoppy.paper.database.Shops
import kr.doka.lab.shoppy.paper.shoppy.Shoppy
import kr.doka.lab.shoppy.paper.shoppy.ShoppyInventoryType
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ShoppyCommand {
    private val prefix = "[ SHOPPY ]"

    val shopList: List<String>
        get() = shops.keys.toList()
    val shops: MutableMap<String, Shoppy> = mutableMapOf()

    init {
        transaction {
            val row = Shops.selectAll()
            row.forEach { shop ->
                val name = shop[Shops.name]
                shops[name] = Shoppy(name)
            }
        }
    }

    fun createCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        val root =
            Commands.literal("상점")
                .executes(::withNoArgsLogic)

        val open =
            Commands.literal("열기")
                .executes(::openShopLogicWithNoName)
                .then(
                    Commands.argument("name", StringArgumentType.string())
                        .executes(::openShopLogic),
                )

        val create =
            Commands.literal("제작")
                .requires {
                    it.sender.hasPermission("shoppy.admin")
                }
                .executes(::openShopLogicWithNoName)
                .then(
                    Commands.argument("name", StringArgumentType.string())
                        .executes(::createShopLogic),
                )
        val delete =
            Commands.literal("삭제")
                .requires {
                    it.sender.hasPermission("shoppy.admin")
                }
        val edit =
            Commands.literal("설정")
                .requires {
                    it.sender.hasPermission("shoppy.admin")
                }
                .executes(::editShopLogicWithNoName)
                .then(
                    Commands.argument("name", StringArgumentType.string())
                        .executes(::editShopLogic),
                )
        val price =
            Commands.literal("가격설정")
                .requires {
                    it.sender.hasPermission("shoppy.admin")
                }
                .executes(::editShopLogicWithNoName)
                .then(
                    Commands.argument("name", StringArgumentType.string())
                        .executes(::editPriceShopLogic),
                )

        root.then(open)
        root.then(delete)
        root.then(create)
        root.then(edit)
        root.then(price)

        return root
    }

    private fun withNoArgsLogic(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.getSource()!!.sender

        sender.sendMessage("")
        sender.sendMessage(" Shop System Made By DOKA1203 (JEESEUNGHYEON)")
        sender.sendMessage("")
        sender.sendMessage(" /상점 열기 <이름> : 상점을 엽니다.")

        if (sender.hasPermission("shoppy.admin")) {
            sender.sendMessage("$prefix /상점 제작 <이름> : 상점을 제작합니다.")
            sender.sendMessage("$prefix /상점 삭제 <이름> : 상점을 삭제합니다.")
            sender.sendMessage("$prefix /상점 목록 : 상점 목록을 확인합니다.")
            sender.sendMessage("$prefix /상점 설정 <이름> : 상점을 설정합니다.")
            sender.sendMessage("$prefix /상점 가격설정 <이름> <가격> : 상점 판매가격을 설정합니다.")

            sender.sendMessage("$prefix /상점 리로드 : config.yml 을 리로드 합니다.")
            sender.sendMessage("$prefix 가격설정시 가격에 '0' 를 적을시 가격을 삭제합니다.")
        }
        return Command.SINGLE_SUCCESS
    }

    private fun createShopLogic(ctx: CommandContext<CommandSourceStack>): Int {
        val shopName = StringArgumentType.getString(ctx, "name")
        val sender = ctx.getSource()!!.sender
        val executor: Entity? = ctx.getSource()!!.executor
        if (executor !is Player) {
            sender.sendPlainMessage("$prefix 플레이어만 상점을 사용할 수 있습니다.")
            return Command.SINGLE_SUCCESS
        }

        val s = Shoppy(shopName)

        shops[shopName] = s
        s.save()

        return Command.SINGLE_SUCCESS
    }

    private fun openShopLogic(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.getSource()!!.sender
        val shopName = StringArgumentType.getString(ctx, "name")
        if (!shops.containsKey(shopName)) {
            sender.sendMessage("상점이 존재하지 않습니다.")
            return Command.SINGLE_SUCCESS
        }
        val shop = shops[shopName]!!
        try {
            shop.open(sender as Player, ShoppyInventoryType.MAIN)
        } catch (e: Exception) {
            sender.sendMessage("플레이어만 명령어를 실행 할 수 있습니다.")
        }

        return Command.SINGLE_SUCCESS
    }

    private fun openShopLogicWithNoName(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.getSource()!!.sender
        sender.sendMessage(" /상점 열기 <이름> : 상점을 엽니다.")
        return Command.SINGLE_SUCCESS
    }

    private fun editShopLogic(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.getSource()!!.sender
        val shopName = StringArgumentType.getString(ctx, "name")
        if (!shops.containsKey(shopName)) {
            sender.sendMessage("상점이 존재하지 않습니다.")
            return Command.SINGLE_SUCCESS
        }
        val shop = shops[shopName]!!
        try {
            shop.open(sender as Player, ShoppyInventoryType.EDIT)
        } catch (e: Exception) {
            sender.sendMessage("플레이어만 명령어를 실행 할 수 있습니다.")
        }

        return Command.SINGLE_SUCCESS
    }

    private fun editShopLogicWithNoName(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.getSource()!!.sender
        sender.sendMessage("$prefix /상점 설정 <이름> : 상점을 설정합니다.")
        return Command.SINGLE_SUCCESS
    }

    private fun editPriceShopLogic(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.getSource()!!.sender
        val shopName = StringArgumentType.getString(ctx, "name")
        if (!shops.containsKey(shopName)) {
            sender.sendMessage("상점이 존재하지 않습니다.")
            return Command.SINGLE_SUCCESS
        }
        val shop = shops[shopName]!!
        try {
            shop.open(sender as Player, ShoppyInventoryType.PRICE)
        } catch (e: Exception) {
            sender.sendMessage("플레이어만 명령어를 실행 할 수 있습니다.")
        }

        return Command.SINGLE_SUCCESS
    }
}
