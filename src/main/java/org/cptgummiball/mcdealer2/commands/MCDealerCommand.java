package org.cptgummiball.mcdealer2.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cptgummiball.mcdealer2.MCDealer2;
import org.cptgummiball.mcdealer2.utils.Translator;
import org.cptgummiball.mcdealer2.web.WebServer;

import java.util.ArrayList;
import java.util.List;

public class MCDealerCommand implements CommandExecutor {

    private final Translator translator;

    // Temporary lists to hold player names
    private static final List<String> hideshopPlayerList = new ArrayList<>();
    private static final List<String> showshopPlayerList = new ArrayList<>();

    public MCDealerCommand(MCDealer2 plugin) {
        this.translator = plugin.translator;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false; // Show usage if no arguments are provided
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (sender.hasPermission("mcdealer.admin")) {
                    MCDealer2 pluginInstance = MCDealer2.getPlugin(MCDealer2.class);

                    // Reload configuration
                    pluginInstance.reloadConfig();
                    sender.sendMessage(translator.translate("reloadcommand.config"));

                    // Reload translations
                    pluginInstance.translator = new Translator(pluginInstance, pluginInstance.getConfig().getString("language", "en-US"));
                    sender.sendMessage(translator.translate("reloadcommand.translation"));

                    // Restart WebServer
                    if (pluginInstance.webServer != null) {
                        pluginInstance.webServer.stop();
                    }
                    pluginInstance.webServer = new WebServer(pluginInstance);
                    pluginInstance.webServer.start();
                    sender.sendMessage(translator.translate("reloadcommand.webserver"));

                    sender.sendMessage(translator.translate("reloadcommand.success"));
                } else {
                    sender.sendMessage(translator.translate("commands.nopermission"));
                }
                break;

            case "hideshop":
                if (sender instanceof Player player) {
                    if (player.hasPermission("mcdealer.shop")) {
                        hideshopPlayerList.add(player.getName());
                        player.sendMessage(translator.translate("shopcommands.hideshopsuccess")); // Add success message
                    } else {
                        player.sendMessage(translator.translate("commands.nopermission")); // Permission error message
                    }
                } else {
                    sender.sendMessage(translator.translate("commands.onlyplayers")); // Message for console users
                }
                break;

            case "showshop":
                if (sender instanceof Player player) {
                    if (player.hasPermission("mcdealer.shop")) {
                        showshopPlayerList.add(player.getName());
                        player.sendMessage(translator.translate("shopcommand.showshopsuccess")); // Add success message
                    } else {
                        player.sendMessage(translator.translate("commands.nopermission")); // Permission error message
                    }
                } else {
                    sender.sendMessage(translator.translate("commands.onlyplayers")); // Message for console users
                }
                break;

            default:
                return false;
        }

        return true;
    }
    // Method to get the hideshop player list
    public static List<String> getHideshopPlayerList() {
        return hideshopPlayerList;
    }

    // Method to get the showshop player list
    public static List<String> getShowshopPlayerList() {
        return showshopPlayerList;
    }

    // Method to remove a player from the hideshop list
    public static void removeHideshopPlayer(String playerName) {
        hideshopPlayerList.remove(playerName);
    }

    // Method to remove a player from the showshop list
    public static void removeShowshopPlayer(String playerName) {
        showshopPlayerList.remove(playerName);
    }
}
