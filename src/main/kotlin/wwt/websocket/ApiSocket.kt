package wwt.websocket

import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson
import org.bukkit.entity.Player
import wwt.Config
import wwt.dto.PlayerServerData

class ApiSocket : WwtApi {

    private var apiUrl = "http://192.168.1.142:8080/wwtapi/"
    private val gson = Gson()

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    override suspend fun registerPlayer(player: Player) : Boolean {
        try {
            val requestBody = PlayerServerData(player.uniqueId.toString(), Config.getServerUuid().toString())

            val response: HttpResponse = client.post("${apiUrl}users/createUser") {
                contentType(ContentType.Application.Json)
                setBody(gson.toJson(requestBody))
            }

            return response.status == HttpStatusCode.OK

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}