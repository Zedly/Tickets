/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zedly.tickets;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

/**
 *
 * @author Dennis
 */
public abstract class DynamicallyLoadedCommand extends BukkitCommand {

    public DynamicallyLoadedCommand(String permission, String name, String description,
            String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        this.setPermission(permission);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(!sender.hasPermission(getPermission()) && !sender.hasPermission("tickets.*")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        return onCommand(sender, commandLabel, args);
    }
    
    public abstract boolean onCommand(CommandSender sender, String commandLabel, String[] args);
}
