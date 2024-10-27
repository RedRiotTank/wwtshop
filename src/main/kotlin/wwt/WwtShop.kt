package wwt

import org.bukkit.plugin.java.JavaPlugin

class WwtShop : JavaPlugin() {

    companion object {
        lateinit var instance: WwtShop
    }

    override fun onEnable() {
        instance = this
        this.getCommand("wwt")?.setExecutor(WwtCommandExecutor())
        this.getCommand("wwt")?.tabCompleter = WwtCommandExecutor()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
