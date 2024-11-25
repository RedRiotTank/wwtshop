package wwt.dto

data class PlayerServerData(
    val id: Int,
    val playerUuid: String,
    val serverUuid: String,
    val money: Int
){
    constructor(playerUuid: String, serverUuid: String) : this(0, playerUuid, serverUuid, 0)
}
