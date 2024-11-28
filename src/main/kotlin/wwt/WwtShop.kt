package wwt

import org.bukkit.plugin.java.JavaPlugin
import wwt.func.UI
import wwt.func.WwtAuth
import wwt.websocket.ApiSocket
import wwt.websocket.WwtApi

class WwtShop : JavaPlugin() {

    private val wwtApi: WwtApi = ApiSocket()
    private val wwtAuth = WwtAuth(wwtApi)
    private val ui = UI(wwtAuth)
    private val commandExecutor = WwtCommandExecutor(ui)

    companion object {
        lateinit var instance: WwtShop
    }

    override fun onEnable() {
        instance = this

        saveDefaultConfig()
        Config.initialize(config, this)
        wwtAuth.generateServerUuidIfNull()

        this.getCommand("wwt")?.setExecutor(commandExecutor)
        this.getCommand("wwt")?.tabCompleter = commandExecutor

        server.pluginManager.registerEvents(wwtAuth, this)
        server.pluginManager.registerEvents(ui, this)
    }

    override fun onDisable() {
        TODO("No need to implement this method")
    }
}
