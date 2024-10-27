package wwt.func

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import wwt.sendWwtMessage

class Help {

    fun sendHelpMessage(sender: CommandSender) {

        val lineBreak = Component.text("\n")

        val helpMessage = Component.text()
            .append(lineBreak)
            .append(getHelpMessageComponent())
            .append(lineBreak)
            .append(lineBreak)
            .append(getCommandComponent("shop"))
            .append(getCommandDescriptionComponent("Open shop menu."))
            .append(lineBreak)
            .append(getCommandComponent("sell"))
            .append(getCommandDescriptionComponent("Open sell menu."))
            .append(lineBreak)
            .append(getCommandComponent("help"))
            .append(getCommandDescriptionComponent("List help of WWT."))
            .append(lineBreak)
            .append(lineBreak)
            .append(Component.text("For more information, visit our documentation."))
            .build()

        sender.sendWwtMessage(helpMessage)
    }

    private fun getHelpMessageComponent(): Component {
        return Component.text("=== WWT Help ===")
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.BOLD, true)
    }

    private fun getCommandComponent(command: String): Component {
        return Component.text("• /wwt $command: ")
            .color(NamedTextColor.GREEN)
            .decoration(TextDecoration.BOLD, false)
    }

    private fun getCommandDescriptionComponent(description: String): Component {
        return Component.text(description)
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.BOLD, false)
    }

}