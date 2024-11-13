package wwt

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import wwt.func.Help
import wwt.func.UI

class WwtCommandExecutor(
    private val ui: UI
) : CommandExecutor, TabCompleter {

    private val subcommands = listOf(
        "shop",
        "sell",
        "help"
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            Help().sendHelpMessage(sender)
            return true
        }

        when (args[0].lowercase()) {
            "shop" -> {
                sender.sendWwtMessage("Opening shop menu...")

            }
            "sell" -> {
                ui.openSellInventory(sender as Player)
            }
            "help" -> {
                Help().sendHelpMessage(sender)
            }
            else -> {
                Help().sendHelpMessage(sender)

            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (args.size == 1)
            return subcommands.filter { it.startsWith(args[0], ignoreCase = true) }
        return emptyList()
    }
}