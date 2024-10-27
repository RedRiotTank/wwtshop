package wwt

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender

fun CommandSender.getPrefixComponent(): Component {
    return Component.text("[${WwtShop.instance.name}]: ".uppercase())
        .color(NamedTextColor.BLUE)
        .decorate(TextDecoration.BOLD)
}

fun CommandSender.sendWwtMessage(message: String) {
    val messageComponent = Component.text(message)
        .color(NamedTextColor.WHITE)
        .decoration(TextDecoration.BOLD, false)

    this.sendMessage(this.getPrefixComponent().append(messageComponent))
}

fun CommandSender.sendWwtMessage(message: Component) {
    this.sendMessage(this.getPrefixComponent().append(message))
}