package wwt.func

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import wwt.Config
import wwt.WwtShop
import wwt.sendWwtMessage
import wwt.websocket.WwtApi
import java.util.UUID

class WwtAuth(
    private val wwtApi: WwtApi
) : Listener {

    fun generateServerUuidIfNull() {
        Config.setServerUuid(UUID.randomUUID().toString())
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerLogin(event: PlayerJoinEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = wwtApi.registerPlayer(event.player)

            WwtShop.instance.server.scheduler.runTask(WwtShop.instance, Runnable {
                if (result)
                    event.player.sendWwtMessage("Joined the World Wide Trade")
                 else
                    event.player.sendWwtMessage("Unexpected error: failed to join the World Wide Trade")
            })
        }




    }
}