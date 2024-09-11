package org.cptgummiball.mcdealer2.commands;

import org.cptgummiball.mcdealer2.MCDealer2;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {

    private final MCDealer2 plugin;

    public ReloadCommand(MCDealer2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender has the required permission
        if (!(sender instanceof Player) || sender.hasPermission("mcdealer2.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "Reloading MCDealer2 plugin...");

            // Stop processes
            plugin.stopProcesses();

            // Reload config
            plugin.reloadConfig();

            // Start processes again
            plugin.startProcesses();

            sender.sendMessage(ChatColor.GREEN + "MCDealer2 has been reloaded successfully.");
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
        }
        return false;
    }
}
