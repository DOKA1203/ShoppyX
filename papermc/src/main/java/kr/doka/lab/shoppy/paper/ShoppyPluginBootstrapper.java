package kr.doka.lab.shoppy.paper;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.bukkit.plugin.java.JavaPlugin;

public class ShoppyPluginBootstrapper implements PluginBootstrap {


    @Override
    public void bootstrap(BootstrapContext ctx) {

    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        return new ShoppyPlugin();
    }
}
