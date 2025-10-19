package kr.doka.lab.shoppy.paper

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kr.doka.lab.shoppy.paper.ShoppyCommand.createCommand
import org.bukkit.plugin.java.JavaPlugin

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
    }

    override fun onEnable() {
        saveDefaultConfig()

        this.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(createCommand().build())
        }
        instance = this
    }
}
