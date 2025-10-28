package kr.doka.lab.shoppy.paper

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kr.doka.lab.shoppy.paper.ShoppyCommand.createCommand
import kr.doka.lab.shoppy.paper.database.ShopItems
import kr.doka.lab.shoppy.paper.database.Shops
import kr.doka.lab.shoppy.paper.listeners.InventoryListener
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class ShoppyPlugin : JavaPlugin() {
    val pluginScope: CoroutineScope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.Default +
                CoroutineName("ShoppyX-PluginScope") +
                CoroutineExceptionHandler { _, e -> logger.severe("ShoppyX-PluginScope - Coroutine error: $e") },
        )

    companion object {
        lateinit var instance: ShoppyPlugin
            private set
        val pluginScope: CoroutineScope
            get() {
                return instance.pluginScope
            }

        lateinit var econ: Economy
    }

    override fun onEnable() {
        saveDefaultConfig()

        val dbFile = File(dataFolder, "shop.db")

        // 3. Exposed를 사용하여 SQLite 데이터베이스에 연결
        Database.connect(url = "jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC")

        logger.info("SQLite 데이터베이스에 성공적으로 연결되었습니다: ${dbFile.absolutePath}")

        // 4. 테이블 생성 (기존과 동일)
        transaction {
            SchemaUtils.createMissingTablesAndColumns(ShopItems, Shops)
        }

        this.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(createCommand().build())
        }
        if (!setupEconomy()) {
            logger.severe("Cannot use Vault economy. Disabling plugin")
            server.pluginManager.disablePlugin(this)
            return
        }
        instance = this

        Bukkit.getPluginManager().registerEvents(InventoryListener(), this)


    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false
        econ = rsp.getProvider()
        return true
    }
}
