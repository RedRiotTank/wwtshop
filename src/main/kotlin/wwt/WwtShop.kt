package wwt

import org.bukkit.plugin.java.JavaPlugin
import wwt.func.UI

class WwtShop : JavaPlugin() {

    private var ui = UI()
    private var commandExecutor = WwtCommandExecutor(ui)


    companion object {
        lateinit var instance: WwtShop
    }

    override fun onEnable() {
        instance = this
        this.getCommand("wwt")?.setExecutor(commandExecutor)
        this.getCommand("wwt")?.tabCompleter = commandExecutor
        server.pluginManager.registerEvents(ui, this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
