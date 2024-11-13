package wwt

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class Config {
    companion object {
        private lateinit var config: FileConfiguration
        private lateinit var plugin: JavaPlugin

        fun initialize(configuration: FileConfiguration, pluginInstance: JavaPlugin) {
            config = configuration
            plugin = pluginInstance
        }

        fun getServerUuid(): UUID {
            val uuidString = config.getString("server.uuid")

            return if (!uuidString.isNullOrEmpty() && uuidString != "null") {
                UUID.fromString(uuidString)
            } else {
                val newUuid = UUID.randomUUID().toString()
                setServerUuid(newUuid)
                UUID.fromString(newUuid)
            }
        }

        fun setServerUuid(uuid: String): String {
            config["server.uuid"] = uuid
            saveConfig()
            return uuid
        }

        private fun saveConfig() {
            plugin.saveConfig()
        }
    }
}
