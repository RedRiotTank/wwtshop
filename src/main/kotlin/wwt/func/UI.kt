package wwt.func

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import wwt.WwtShop
import wwt.dto.OfferServerData
import wwt.sendWwtMessage
import wwt.websocket.ApiSocket
import wwt.websocket.WwtApi
import java.util.*

class UI(
    private val wwtAuth: WwtAuth
) : Listener {
    private val wwtApi : WwtApi = ApiSocket()

    // ==== Inventory slots ====
    private val itemSlot = 11
    private val okSlot = 18
    private val cancelSlot = 26
    private val priceSlot = 15
    private val upSlot = 6
    private val downSlot = 24
    private val filler = ItemStack(Material.RED_STAINED_GLASS_PANE)

    // ==== Inventory mappers ====
    private val sellInventories = mutableMapOf<UUID, Inventory>()
    private val buyInventories = mutableMapOf<UUID, Inventory>()
    private val playerActiveBuyItems = mutableMapOf<UUID, Int>()


    // ==== Sell module ====
    fun openSellInventory(player: Player) {
        val inventory = createSellInventory()
        sellInventories[player.uniqueId] = inventory
        player.openInventory(inventory)
    }

    private fun createSellInventory(): Inventory {
        val inventory = Bukkit.createInventory(null, InventoryType.CHEST, Component.text("Sell Interface"))

        inventory.forEachIndexed { index, _ -> inventory.setItem(index, filler) }

        inventory.setItem(itemSlot, ItemStack(Material.AIR))

        inventory.setItem(upSlot, createItemStack(Material.ARROW, "add +1 to price"))
        inventory.setItem(priceSlot, createItemStack(Material.EMERALD, "Current price: 1"))
        inventory.setItem(downSlot, createItemStack(Material.ARROW, "reduce -1 to price"))
        inventory.setItem(okSlot, createItemStack(Material.TOTEM_OF_UNDYING, "Confirm Sell", NamedTextColor.GREEN))
        inventory.setItem(cancelSlot, createItemStack(Material.BARRIER, "Cancel sell", NamedTextColor.RED))

        return inventory
    }

    private fun handleSellInventoryClick(event: InventoryClickEvent){
        val sellInventoryUUID = sellInventories.entries.find { it.value == event.inventory }?.key
        val playerId = wwtAuth.getPlayerId(event.whoClicked.uniqueId)
        if (sellInventoryUUID != null) {
            val clickedSlot = event.slot

            if (event.clickedInventory != event.whoClicked.inventory) {
                val currentPrice = event.inventory.getItem(priceSlot)?.itemMeta?.displayName()?.let {
                    val regex = Regex("\\d+")
                    val matchResult = regex.find(PlainTextComponentSerializer.plainText().serialize(it))
                    matchResult?.value?.toInt() ?: 1
                } ?: 1

                when (clickedSlot) {
                    itemSlot -> event.isCancelled = false
                    upSlot -> updatePrice(event, currentPrice + 1)
                    downSlot -> updatePrice(event, maxOf(currentPrice - 1, 1))
                    okSlot -> handleOkSlot(event, sellInventoryUUID, currentPrice, playerId)
                    cancelSlot -> cancelSell(event, sellInventoryUUID)
                    else -> event.isCancelled = true
                }
            }
        }

    }

    private fun handleOkSlot(event: InventoryClickEvent, inventoryUUID: UUID, currentPrice: Int, playerId: Int?) {
        val item = sellInventories[inventoryUUID]?.getItem(itemSlot)
        if (item != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val itemId = wwtApi.createItem(item)
                if (itemId != null) {
                    val resultOffer = wwtApi.createOffer(
                        OfferServerData(
                            id = 0,
                            price = currentPrice,
                            count = item.amount,
                            user = playerId ?: 0,
                            item = itemId
                        )
                    )

                    WwtShop.instance.server.scheduler.runTask(WwtShop.instance, Runnable {
                        if (resultOffer) {
                            event.whoClicked.sendWwtMessage("Item added to the market")
                        } else {
                            event.whoClicked.sendWwtMessage("Unexpected error: failed to add item to the market")
                        }
                    })
                } else {
                    WwtShop.instance.server.scheduler.runTask(WwtShop.instance, Runnable {
                        event.whoClicked.sendWwtMessage("Unexpected error: failed to add item to the market")
                    })
                }
            }
        } else {
            event.whoClicked.sendWwtMessage("You cannot confirm sell without an item in the slot")
            event.isCancelled = true
        }

        sellInventories[inventoryUUID]?.setItem(itemSlot, null)
        event.whoClicked.closeInventory()
        event.isCancelled = true
    }

    private fun handleSellCloseInventory(event: InventoryCloseEvent) {
        val sellInventoryUUID = sellInventories.entries.find { it.value == event.inventory }?.key

        if (sellInventoryUUID != null) {

            sellInventories[sellInventoryUUID]?.getItem(itemSlot)?.let {
                event.player.sendWwtMessage("You canceled the sell, returned the item to your inventory")
                event.player.inventory.addItem(it)
            }

            sellInventories.remove(sellInventoryUUID)
        }

    }

    private fun updatePrice(event: InventoryClickEvent, newPrice: Int) {
        event.inventory.setItem(priceSlot, createItemStack(Material.EMERALD, "Current price: $newPrice"))
        event.isCancelled = true
    }

    private fun cancelSell(event: InventoryClickEvent, inventoryUUID: UUID) {
        if (sellInventories[inventoryUUID]?.getItem(itemSlot) != null) {
            event.whoClicked.sendWwtMessage("You can not cancel the sell with item in the slot")
            event.isCancelled = true
            return
        }

        event.whoClicked.sendWwtMessage("You canceled the sell")
        event.whoClicked.closeInventory()
        event.isCancelled = true
    }

    // ==== Shop module ====
    fun openShopInventory(player: Player, page: Int) {
        player.sendWwtMessage("Opening shop inventory in page $page")
        val inventory = createShopInventory(player, page)
        buyInventories[player.uniqueId] = inventory
        player.openInventory(inventory)
    }

    private fun createShopInventory(player: Player, page: Int): Inventory {
        val inventory = Bukkit.createInventory(null, InventoryType.CHEST, Component.text("Shop Interface"))

        Bukkit.getScheduler().runTask(WwtShop.instance, Runnable {

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val offerList = wwtApi.getPaginatedOffers(page)

                    var currentCount = 1

                    Bukkit.getScheduler().runTask(WwtShop.instance, Runnable {
                        offerList.forEach { offer ->
                            val item = offer.item
                            val price = offer.price
                            val count = offer.count

                            inventory.setItem(currentCount,
                                Material.matchMaterial(item.itemId)?.let { createItemStack(it, it.name + " - " + offer.id) })
                            inventory.setItem(currentCount + 9, createItemStack(Material.EMERALD, "Price: $price"))
                            inventory.setItem(currentCount + 18, createItemStack(Material.CHEST, "Count: $count"))


                            currentCount += 1
                        }
                    })
                } catch (e: Exception) {
                    Bukkit.getScheduler().runTask(WwtShop.instance, Runnable {
                        player.sendWwtMessage("Error fetching offers: ${e.message}")
                    })
                }
            }

            inventory.setItem(okSlot, createItemStack(Material.TOTEM_OF_UNDYING, "Confirm Sell", NamedTextColor.GREEN))
            inventory.setItem(cancelSlot, createItemStack(Material.BARRIER, "Cancel sell", NamedTextColor.RED))
        })

        return inventory
    }

    private fun handleBuyInventoryClick(event: InventoryClickEvent) {
        val buyInventoryUUID = buyInventories.entries.find { it.value == event.inventory }?.key
        if (event.clickedInventory == event.whoClicked.inventory) return

        if (buyInventoryUUID != null) {
            var clickedSlot = event.slot

            if (event.clickedInventory?.getItem(clickedSlot) == null) {
                event.isCancelled = true
                return
            }


            if (
                clickedSlot != 0 &&
                clickedSlot != 9 &&
                clickedSlot != 18 &&
                clickedSlot != 8 &&
                clickedSlot != 17 &&
                clickedSlot != 26
            ) {

                if (clickedSlot in 9..18) clickedSlot -= 9
                else if (clickedSlot in 19..26) clickedSlot -= 18

                val itemPrice = event.inventory.getItem(clickedSlot + 9)?.itemMeta?.displayName()?.let {
                    val regex = Regex("\\d+")
                    val matchResult = regex.find(PlainTextComponentSerializer.plainText().serialize(it))
                    matchResult?.value?.toInt() ?: 1
                } ?: 1

                event.clickedInventory?.setItem(
                    clickedSlot + 9,
                    createItemStack(Material.REDSTONE_TORCH, "Price: $itemPrice")
                )

                val previousSlot = playerActiveBuyItems[buyInventoryUUID]
                if (previousSlot != null && previousSlot != clickedSlot) {
                    val previousItemPrice = event.clickedInventory?.getItem(previousSlot + 9)?.itemMeta?.displayName()?.let {
                        val regex = Regex("\\d+")
                        val matchResult = regex.find(PlainTextComponentSerializer.plainText().serialize(it))
                        matchResult?.value?.toInt() ?: 1
                    } ?: 1

                    event.clickedInventory?.setItem(
                        previousSlot + 9,
                        createItemStack(Material.EMERALD, "Price: $previousItemPrice")
                    )
                }

                playerActiveBuyItems[buyInventoryUUID] = clickedSlot

                event.isCancelled = true
            } else if(clickedSlot == okSlot){


                playerActiveBuyItems[buyInventoryUUID]?.let {
                    val offerId = event.clickedInventory?.getItem(it)?.itemMeta?.displayName()?.let {
                        val regex = Regex("\\d+")
                        val matchResult = regex.find(PlainTextComponentSerializer.plainText().serialize(it))
                        matchResult?.value?.toInt() ?: 1
                    } ?: 1

                    CoroutineScope(Dispatchers.IO).launch {
                        event.whoClicked.sendWwtMessage("Confirming offer $offerId and user ${wwtAuth.getPlayerId(event.whoClicked.uniqueId)}")
                        val result = wwtApi.confirmOffer(offerId, wwtAuth.getPlayerId(event.whoClicked.uniqueId)!!)

                        WwtShop.instance.server.scheduler.runTask(WwtShop.instance, Runnable {
                            if (result) {
                                event.whoClicked.sendWwtMessage("Offer confirmed")

                                val itemCount = event.clickedInventory?.getItem(it + 18)?.itemMeta?.displayName()?.let {
                                    val regex = Regex("\\d+")
                                    val matchResult = regex.find(PlainTextComponentSerializer.plainText().serialize(it))
                                    matchResult?.value?.toInt() ?: 1
                                } ?: 1
                                val itemMaterial = event.clickedInventory?.getItem(it)?.type

                                event.whoClicked.inventory.addItem(ItemStack(itemMaterial!!, itemCount))


                            } else {
                                event.whoClicked.sendWwtMessage("Not enough money or Unexpected error: failed to confirm offer")
                            }
                        })
                    }
                }

                event.whoClicked.closeInventory()
                buyInventories.remove(buyInventoryUUID)
                playerActiveBuyItems.remove(buyInventoryUUID)

            } else if (clickedSlot == cancelSlot) {
                event.whoClicked.closeInventory()
                buyInventories.remove(buyInventoryUUID)
                playerActiveBuyItems.remove(buyInventoryUUID)
            }
            else {
                event.isCancelled = true
            }
        }
    }

    private fun handleBuyCloseInventory(event: InventoryCloseEvent) {
        //TODO: Implement
    }


    // ==== Utility functions ====
    private fun createItemStack(material: Material, name: String, color: NamedTextColor? = null): ItemStack {
        return ItemStack(material).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text(name).color(color))
            }
        }
    }

   // ==== Event handlers ====
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        handleSellInventoryClick(event)
        handleBuyInventoryClick(event)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        handleSellCloseInventory(event)
        handleBuyCloseInventory(event)
    }
}