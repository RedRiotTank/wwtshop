package wwt.func

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import wwt.Config
import wwt.WwtShop
import wwt.sendWwtMessage
import wwt.websocket.WwtApi
import java.util.UUID

class WwtAuth(
    private val wwtApi: WwtApi
) : Listener {

    private val playerIdMap = HashMap<UUID, Int>()

    fun generateServerUuidIfNull() {
        Config.setServerUuid(UUID.randomUUID().toString())
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerLogin(event: PlayerJoinEvent) {
        CoroutineScope(Dispatchers.IO).launch {

            val playerServerData = wwtApi.getUserByPlayerUUIDAndServerUUID(event.player)

            if (playerServerData == null) {
                val result = wwtApi.registerPlayer(event.player)


                WwtShop.instance.server.scheduler.runTask(WwtShop.instance, Runnable {
                    if (result != null) {
                        playerIdMap[event.player.uniqueId] = result.id
                        event.player.sendWwtMessage("Registered in World Wide Trade")
                    } else event.player.sendWwtMessage("Unexpected error: failed to join the World Wide Trade")
                })

            } else {
                playerIdMap[event.player.uniqueId] = playerServerData.id
                event.player.sendWwtMessage("Logged in World Wide Trade with id ${playerServerData.id}")
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        playerIdMap.remove(event.player.uniqueId)
    }

    fun getPlayerId(playerUuid: UUID): Int? {
        return playerIdMap[playerUuid]
    }
}