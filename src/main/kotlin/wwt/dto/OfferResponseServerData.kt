package wwt.dto

data class OfferResponseServerData(
    val id: Int,
    val price: Int,
    val count: Int,
    val user: PlayerServerData,
    val item: ItemServerData
)
