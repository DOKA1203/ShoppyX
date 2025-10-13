package kr.doka.lab.shoppy.paper

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

object ShoppyCommand {
    fun createCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        val root = Commands.literal("상점")

        val open =
            Commands.literal("열기")
                .then(
                    Commands.argument("name", StringArgumentType.string())
                        .executes(::createShopLogic),
                )

        val create =
            Commands.literal("제작")
                .requires {
                    it.sender.hasPermission("shoppy.admin")
                }
        val edit =
            Commands.literal("설정")
                .requires {
                    it.sender.hasPermission("shoppy.admin")
                }
        val price =
            Commands.literal("가격설정")
                .requires {
                    it.sender.hasPermission("shoppy.admin")
                }

        root.then(open)
        root.then(create)
        root.then(edit)
        root.then(price)

        return root
    }

    private fun createShopLogic(ctx: CommandContext<CommandSourceStack>): Int {
        val shopName = StringArgumentType.getString(ctx, "name") // Retrieve the speed argument
        val sender = ctx.getSource()!!.sender // Retrieve the command sender
        val executor: Entity? = ctx.getSource()!!.executor // Retrieve the command executor, which may or may not be the same as the sender

        // Check whether the executor is a player, as you can only set a player's flight speed
        if (executor !is Player) {
            // If a non-player tried to set their own flight speed
            sender.sendPlainMessage("플레이어만 상점을 열 수 있습니다.")
            return Command.SINGLE_SUCCESS
        }

        return Command.SINGLE_SUCCESS
    }
}
