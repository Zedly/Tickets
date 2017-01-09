/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zedly.tickets;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Dennis
 */
public class Tickets extends JavaPlugin {

    private static Tickets INSTANCE;
    public static final HashMap<String, Board> BOARDS = new HashMap<>();
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, HH:mm");

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        new File(getDataFolder(), "boards").mkdir();
        YamlConfiguration conf = YamlConfiguration.read(new File(getDataFolder(), "config.yml"));

        YamlConfiguration boardsRoot = conf.getOrCreateSection("boards");
        for (String boardName : boardsRoot.keySet()) {
            YamlConfiguration boardSection = boardsRoot.getOrCreateSection(boardName);
            String description = boardSection.getString("description", "Missing Description!");
            Board board = new Board(boardName, description, boardSection);
            BOARDS.put(boardName, board);
        }

        long notify_millis = conf.getLong("notify-minutes", 15) * 18000;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, TaskPurgeAndNotify.instance(),
                 0, notify_millis);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            for (Board board : BOARDS.values()) {
                System.out.println("Dynamically registering command: " + board.getName());
                try {
                    registerCommand(board, this);
                } catch (ReflectiveOperationException ex) {
                    ex.printStackTrace();
                }
            }
        }, 1);
    }

    @Override
    public void onDisable() {
        for (Board board : BOARDS.values()) {
            try {
                board.save(new File(getDataFolder(), "boards/" + board.getName() + ".yml"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Tickets instance() {
        return INSTANCE;
    }

    private static void registerCommand(Command whatCommand, Plugin plugin)
            throws ReflectiveOperationException {
        //Getting command map from CraftServer
        Method commandMap = plugin.getServer().getClass().getMethod("getCommandMap");
        //Invoking the method and getting the returned object (SimpleCommandMap)
        Object cmdmap = commandMap.invoke(plugin.getServer());
        //getting register method with parameters String and Command from SimpleCommandMap
        Method register = cmdmap.getClass().getMethod("register", String.class, Command.class);
        //Registering the command provided above
        register.invoke(cmdmap, whatCommand.getName(), whatCommand);
        //All the exceptions thrown above are due to reflection, They will be thrown if any of the above methods
        //and objects used above change location or turn private. IF they do, let me know to update the thread!
    }
}
