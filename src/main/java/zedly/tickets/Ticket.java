/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zedly.tickets;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Dennis
 */
public class Ticket implements Comparable<Ticket> {

    private final int uniqueId;
    private final long creationTime;
    private final String message;
    private final String authorName;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final double yaw;
    private final double pitch;
    private final List<String> edits;
    private long lastEditTime;
    private long closeTime;
    private String closedBy;
    private int priority;

    public Ticket(Player player, String message, int uniqueId) {
        this.uniqueId = uniqueId;
        creationTime = System.currentTimeMillis();
        lastEditTime = creationTime;
        authorName = player.getName();
        world = player.getWorld().getName();
        Location l = player.getLocation();
        x = l.getX();
        y = l.getY();
        z = l.getZ();
        yaw = l.getYaw();
        pitch = l.getPitch();
        priority = 0;
        this.message = message;
        edits = new ArrayList<>();
    }

    public Ticket(YamlConfiguration yamlSection) {
        uniqueId = yamlSection.getInt("id", 0);
        creationTime = yamlSection.getLong("creationTime", 0);
        lastEditTime = yamlSection.getLong("lastEditTime", creationTime);
        closeTime = yamlSection.getLong("closeTime", 0);
        closedBy = yamlSection.getString("closedBy", "???");
        priority = yamlSection.getInt("priority", 0);
        message = yamlSection.getString("message", "???");
        authorName = yamlSection.getString("authorName", "???");
        world = yamlSection.getString("world", "???");
        x = yamlSection.getDouble("x", 0);
        y = yamlSection.getDouble("y", 0);
        z = yamlSection.getDouble("z", 0);
        yaw = yamlSection.getDouble("yaw", 0);
        pitch = yamlSection.getDouble("pitch", 0);
        edits = yamlSection.getList("comments", String.class);
    }

    public void close(CommandSender sender) {
        closedBy = getIdentity(sender);
        closeTime = System.currentTimeMillis();
        lastEditTime = closeTime;
    }

    public void comment(CommandSender sender, String message) {
        edits.add(getIdentity(sender) + ": " + message);
        closeTime = 0;
        lastEditTime = System.currentTimeMillis();
    }

    public Location getLocation() {
        World destWorld = Bukkit.getWorld(world);
        if (world == null) {
            return null;
        }
        return new Location(destWorld, x, y, z, (float) yaw, (float) pitch);
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public boolean isOpen() {
        return closeTime == 0;
    }

    public String getAuthorName() {
        return authorName;
    }

    public long getCloseTime() {
        return closeTime;
    }

    public String getMessage() {
        return message;
    }

    public int getNumberOfEdits() {
        return edits.size();
    }

    public String getSummary(CommandSender sender) {
        return authorName + ": " + message;
    }

    private String getIdentity(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getName();
        } else {
            return "Console";
        }
    }

    public void sendDetail(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "Message" + ChatColor.DARK_GRAY + ": "
                + ChatColor.AQUA + message);
        sender.sendMessage(ChatColor.GRAY + "Created by" + ChatColor.DARK_GRAY + ": "
                + ChatColor.DARK_AQUA + authorName
                + ChatColor.GRAY + " on " + ChatColor.DARK_AQUA + Tickets.DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(creationTime))));
        if (!isOpen()) {
            sender.sendMessage(ChatColor.GRAY + "Closed by" + ChatColor.DARK_GRAY + ": "
                    + ChatColor.DARK_AQUA + closedBy
                    + ChatColor.GRAY + " on " + ChatColor.DARK_AQUA + Tickets.DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(closeTime))));
        }

        sender.sendMessage(ChatColor.GRAY + "Location" + ChatColor.DARK_GRAY + ": "
                + ChatColor.DARK_AQUA + world + ChatColor.GRAY + " / " + ChatColor.DARK_AQUA + (int) x + " " + (int) y + " " + (int) z);
        if (!edits.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Comments" + ChatColor.DARK_GRAY + ": ");
            for (String c : edits) {
                sender.sendMessage(ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + c);
            }
        }
    }

    private String format(String key, String value) {
        return ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + key + ": " + ChatColor.DARK_AQUA + value;
    }

    private String format(String key, boolean value) {
        return ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + key + ": "
                + (value ? ChatColor.RED + "Yes" : ChatColor.DARK_AQUA + "No");
    }

    private String format(String key, int value) {
        return ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + key + ": " + ChatColor.DARK_AQUA + value;
    }

    private String[] format(String key, List<String> values) {
        return format(key, values.toArray());
    }

    private String[] format(String key, Object[] values) {
        String[] formats = new String[Math.max(values.length + 1, 2)];
        formats[0] = ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + key + ": ";
        if (values.length == 0) {
            formats[1] = ChatColor.DARK_GRAY + "   - " + ChatColor.GRAY + "(none)";
        }
        for (int i = 0; i < values.length; i++) {
            formats[i + 1] = ChatColor.DARK_GRAY + "   - " + ChatColor.DARK_AQUA + values[i];
        }
        return formats;
    }

    public YamlConfiguration save() {
        YamlConfiguration config = YamlConfiguration.emptyConfiguration();
        config.set("id", uniqueId);
        config.set("creationTime", creationTime);
        config.set("lastEditTime", lastEditTime);
        config.set("closeTime", closeTime);
        config.set("priority", priority);
        config.set("message", message);
        config.set("authorName", authorName);
        config.set("closedBy", closedBy);
        config.set("world", world);
        config.set("x", x);
        config.set("y", y);
        config.set("z", z);
        config.set("yaw", yaw);
        config.set("pitch", pitch);
        config.set("comments", edits);
        return config;
    }

    @Override
    public int compareTo(Ticket o) {
        if (o.priority > this.priority) {
            return 1;
        } else if (this.priority > o.priority) {
            return -1;
        } else if (this.creationTime > o.creationTime) {
            return 1;
        } else if (this.creationTime < o.creationTime) {
            return -1;
        }
        return 0;
    }
}
