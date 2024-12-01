package wwt.websocket

import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import wwt.Config
import wwt.dto.*
import java.util.*

class ApiSocket : WwtApi {

    private var apiUrl = "http://192.168.1.142:8080/wwtapi/"
    private val gson = Gson()

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    override suspend fun getUserByPlayerUUIDAndServerUUID(player: Player): PlayerServerData? {
        try {
            val response: HttpResponse = client.get("${apiUrl}users/getUserByPlayerUUIDAndServerUUID?playerUUID=${player.uniqueId}&serverUUID=${Config.getServerUuid()}")

            return if (response.status == HttpStatusCode.NotFound) null
            else gson.fromJson(response.bodyAsText(), PlayerServerData::class.java)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    override suspend fun registerPlayer(player: Player) : PlayerServerData? {
        try {
            val requestBody = PlayerServerData(0, player.uniqueId.toString(), Config.getServerUuid().toString(), 0)

            val response: HttpResponse = client.post("${apiUrl}users/createUser") {
                contentType(ContentType.Application.Json)
                setBody(gson.toJson(requestBody))
            }

            return if (response.status == HttpStatusCode.OK)
                gson.fromJson(response.bodyAsText(), PlayerServerData::class.java)
            else null

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    override suspend fun createItem(itemStack: ItemStack): Int? {
        try {
            val requestBody = ItemServerData(null,itemStack.type.toString())

            val response: HttpResponse = client.post("${apiUrl}items/createItem") {
                contentType(ContentType.Application.Json)
                setBody(gson.toJson(requestBody))
            }

            return if (response.status == HttpStatusCode.OK)
                gson.fromJson(response.bodyAsText(), ItemServerData::class.java).id?: 0
            else null

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    override suspend fun createOffer(offerServerData: OfferServerData): Boolean {
        try {
            val response: HttpResponse = client.post("${apiUrl}offers/createOffer") {
                contentType(ContentType.Application.Json)
                setBody(gson.toJson(offerServerData))
            }

            return response.status == HttpStatusCode.OK

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override suspend fun getPaginatedOffers(page: Int): List<OfferResponseServerData> {
        try {
            val response: HttpResponse = client.get("${apiUrl}offers/getPaginatedOffers?page=$page")

            return if (response.status == HttpStatusCode.OK)
                gson.fromJson(response.bodyAsText(), Array<OfferResponseServerData>::class.java).toList()
            else emptyList()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return emptyList()
    }

    override suspend fun confirmOffer(offerId: Int, userId: Int): Boolean {
        try {
            val response: HttpResponse = client.post("${apiUrl}offers/confirmOffer") {
                contentType(ContentType.Application.Json)
                setBody(gson.toJson(ConfirmOfferServerData(offerId, userId)))
            }


            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                return gson.fromJson(responseBody, Boolean::class.java)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }
}