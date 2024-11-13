package wwt.func

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
import wwt.sendWwtMessage
import wwt.websocket.ApiSocket
import wwt.websocket.WwtApi
import java.util.*

class UI : Listener {
    private val itemSlot = 11
    private val okSlot = 18
    private val cancelSlot = 26
    private val priceSlot = 15
    private val upSlot = 6
    private val downSlot = 24

    private val filler = ItemStack(Material.RED_STAINED_GLASS_PANE)

    private val sellInventories = mutableMapOf<UUID, Inventory>()

    private val wwtApi : WwtApi = ApiSocket()

    fun openSellInventory(player: Player) {
        val inventory = createSellInventory()
        sellInventories[player.uniqueId] = inventory
        player.openInventory(inventory)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val inventoryUUID = sellInventories.entries.find { it.value == event.inventory }?.key

        if (inventoryUUID != null) {
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
                    okSlot -> {
                        if (sellInventories[inventoryUUID]?.getItem(itemSlot) == null) {
                            event.whoClicked.sendWwtMessage("You can not confirm sell without an item in the slot")
                            event.isCancelled = true
                            return
                        }

                        event.whoClicked.sendWwtMessage("You sold the item for $currentPrice")
                        sellInventories[inventoryUUID]?.setItem(itemSlot, null)
                        event.whoClicked.closeInventory()
                        event.isCancelled = true
                    }
                    cancelSlot -> {
                        if (sellInventories[inventoryUUID]?.getItem(itemSlot) != null) {
                            event.whoClicked.sendWwtMessage("You can not cancel the sell with item in the slot")
                            event.isCancelled = true
                            return
                        }

                        event.whoClicked.sendWwtMessage("You canceled the sell")
                        event.whoClicked.closeInventory()
                        event.isCancelled = true
                    }
                    else -> event.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val inventoryUUID = sellInventories.entries.find { it.value == event.inventory }?.key
        if (inventoryUUID != null) {

            sellInventories[inventoryUUID]?.getItem(itemSlot)?.let {
                event.player.sendWwtMessage("You canceled the sell, returned the item to your inventory")
                event.player.inventory.addItem(it)
            }

            sellInventories.remove(inventoryUUID)
        }
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

    private fun createItemStack(material: Material, name: String, color: NamedTextColor? = null): ItemStack {
        return ItemStack(material).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text(name).color(color))
            }
        }
    }

    private fun updatePrice(event: InventoryClickEvent, newPrice: Int) {
        event.inventory.setItem(priceSlot, createItemStack(Material.EMERALD, "Current price: $newPrice"))
        event.isCancelled = true
    }
}