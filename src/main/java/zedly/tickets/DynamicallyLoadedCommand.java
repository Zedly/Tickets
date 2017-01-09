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
import org.bukkit.entity.Player;

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
        return onCommand(sender, commandLabel, args);
    }
    
    public abstract boolean onCommand(CommandSender sender, String commandLabel, String[] args);
}
