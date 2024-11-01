package wwt.websocket

import org.bukkit.entity.Player

interface WwtApi {
    suspend fun registerPlayer(player: Player) : Boolean
}