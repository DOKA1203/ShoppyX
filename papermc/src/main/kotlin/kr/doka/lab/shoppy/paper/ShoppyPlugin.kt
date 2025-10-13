package kr.doka.lab.shoppy.paper

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kr.doka.lab.shoppy.paper.ShoppyCommand.createCommand
import org.bukkit.plugin.java.JavaPlugin

class ShoppyPlugin : JavaPlugin() {
    override fun onEnable() {
        this.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(createCommand().build())
        }
    }
}
