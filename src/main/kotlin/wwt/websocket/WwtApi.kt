package wwt.websocket

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import wwt.dto.OfferResponseServerData
import wwt.dto.OfferServerData
import wwt.dto.PlayerServerData
import java.util.UUID

interface WwtApi {
    suspend fun getUserByPlayerUUIDAndServerUUID(player: Player) : PlayerServerData?

    suspend fun registerPlayer(player: Player) : PlayerServerData?

    suspend fun createItem(itemStack: ItemStack) : Int?

    suspend fun createOffer(offerServerData: OfferServerData) : Boolean

    suspend fun getPaginatedOffers(page: Int) : List<OfferResponseServerData>

    suspend fun confirmOffer(offerId: Int, userId: Int) : Boolean
}