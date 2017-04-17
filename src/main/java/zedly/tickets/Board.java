/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zedly.tickets;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Dennis
 */
public class Board extends DynamicallyLoadedCommand {

    private static final int JOBS_PER_PAGE = 6;
    private final long MAX_AGE_MILLIS;

    private final HashMap<String, Ticket> allTickets = new HashMap<>();
    private final ArrayList<Ticket> activeTickets = new ArrayList<>();

    private final String boardname;
    private final String boardDescription;
    private int nextUniqueId;

    public Board(String name, String description, YamlConfiguration boardSection) {
        super("tickets." + name, name, description, description, new LinkedList<>());
        this.boardname = name;
        this.boardDescription = description;
        MAX_AGE_MILLIS = boardSection.getLong("max-age-days", 60) * 86400000;

        YamlConfiguration blob = YamlConfiguration.read(new File(Tickets.instance().getDataFolder(), "boards/" + name + ".yml"));
        nextUniqueId = blob.getInt("next-unique-id", 0);
        List<YamlConfiguration> jobSections = blob.getSectionList("tickets");
        for (YamlConfiguration yaml : jobSections) {
            Ticket job = new Ticket(yaml);
            if (job.isOpen()) {
                activeTickets.add(job);
            }
            allTickets.put("@" + job.getUniqueId(), job);
        }
        Collections.sort(activeTickets);
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (hasPermission(sender, "list")) {
                listTickets(sender, 1);
            } else {
                displayDescription(sender);
            }
        } else {
            switch (args[0]) {
                case "list":
                case "read":
                    if (!ensurePermission(sender, "list")) {
                        break;
                    }
                    if (args.length == 1) {
                        listTickets(sender, 1);
                    } else if (args[1].matches("\\d{1,8}")) {
                        listTickets(sender, Integer.parseInt(args[1]));
                    } else {
                        listTickets(sender, 1);
                    }
                    break;
                case "history":
                    if (!ensurePermission(sender, "list")) {
                        break;
                    }
                    if (args.length == 1) {
                        listHistory(sender, 1);
                    } else if (args[1].matches("\\d{1,8}")) {
                        listHistory(sender, Integer.parseInt(args[1]));
                    } else {
                        listHistory(sender, 1);
                    }
                    break;
                case "warp":
                case "tp":
                    if (!(sender instanceof Player)) {
                        displayMessage(sender, "This can only be done ingame");
                        break;
                    }
                    if (!ensurePermission(sender, "warp")) {
                        break;
                    }
                    if (args.length == 1) {
                        displayMessage(sender, "Specify a ticket");
                        break;
                    }
                    Ticket ticket = getJob(args[1]);
                    if (ticket == null) {
                        displayMessage(sender, "Invalid Ticket ID");
                        break;
                    }
                    Location loc = ticket.getLocation();
                    if (loc == null) {
                        displayMessage(sender, "Invalid Location");
                        break;
                    }
                    displayMessage(sender, "Teleporting to " + boardname.toUpperCase() + "-@" + ticket.getUniqueId());
                    ((Player) sender).teleport(loc);
                    break;
                case "done":
                case "close":
                    if (!ensurePermission(sender, "close")) {
                        break;
                    }
                    if (args.length == 1) {
                        displayMessage(sender, "Specify a ticket");
                        break;
                    }
                    ticket = getJob(args[1]);
                    if (ticket == null) {
                        displayMessage(sender, "Invalid Ticket ID");
                        break;
                    }
                    if (!ticket.isOpen()) {
                        displayMessage(sender, "This ticket is already closed");
                        break;
                    }
                    ticket.close(sender);
                    activeTickets.remove(ticket);
                    displayMessage(sender, "Closed " + boardname.toUpperCase() + "-@" + ticket.getUniqueId());
                    break;
                case "re":
                case "com":
                case "comment":
                    if (!ensurePermission(sender, "comment")) {
                        break;
                    }
                    if (args.length == 1) {
                        displayMessage(sender, "Specify a ticket");
                        break;
                    }
                    ticket = getJob(args[1]);
                    if (ticket == null) {
                        displayMessage(sender, "Invalid Ticket ID");
                        break;
                    }
                    if (!ticket.isOpen()) {
                        displayMessage(sender, "[Re-Opening " + boardname.toUpperCase() + "-@" + ticket.getUniqueId() + "]");
                        activeTickets.add(ticket);
                    }
                    ticket.comment(sender, StringUtil.joinAndFormat(args, 2));
                    displayMessage(sender, "Added a comment to " + boardname.toUpperCase() + "-@" + ticket.getUniqueId());
                    break;
                case "help":
                    displayDescription(sender);
                    break;
                default:
                    if (args[0].matches("@?\\d{1,8}")) {
                        ticket = getJob(args[0]);
                        if (ticket == null) {
                            displayMessage(sender, "Invalid Ticket ID");
                            break;
                        }
                        if (args.length == 1) {
                            displayTicketDetail(sender, ticket);
                        } else {
                            if (!ensurePermission(sender, "comment")) {
                                break;
                            }
                            if (!ticket.isOpen()) {
                                displayMessage(sender, "Re-Opening " + boardname.toUpperCase() + "-@" + ticket.getUniqueId());
                                activeTickets.add(ticket);
                            }
                            ticket.comment(sender, StringUtil.joinAndFormat(args, 1));
                            displayMessage(sender, "Added a comment to " + boardname.toUpperCase() + "-@" + ticket.getUniqueId());
                        }
                    } else if (sender instanceof Player) {
                        createTicket((Player) sender, String.join(" ", args));
                        displayMessage(sender, "Ticket created");
                    } else {
                        displayMessage(sender, "Tickets can only be created ingame");
                    }
            }
        }
        return true;
    }

    private void listTickets(CommandSender sender, int page) {
        int activePages = getNumberOfActivePages();
        if (activePages == 0) {
            displayMessage(sender, "There are no open tickets on this board!");
            return;
        }
        if (page <= 0) {
            page = 1;
        }
        if (page > activePages) {
            page = activePages;
        }
        sender.sendMessage(StringUtil.generateHLineTitle("Open " + boardname.toUpperCase() + " Tickets"));
        sender.sendMessage("");
        for (int i = JOBS_PER_PAGE * (page - 1); i < JOBS_PER_PAGE * page && i < activeTickets.size(); i++) {
            Ticket ticket = activeTickets.get(i);
            sender.sendMessage(ChatColor.DARK_GRAY + " ["
                    + ChatColor.BLUE + (i + 1)
                    + ChatColor.DARK_GRAY + "] "
                    + ChatColor.DARK_AQUA + ticket.getAuthorName()
                    + ChatColor.AQUA + " on " + Tickets.DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(ticket.getCreationTime())))
                    + ((ticket.getNumberOfEdits() > 0)
                    ? ("  " + ChatColor.AQUA + ChatColor.ITALIC
                    + "[Comments: " + ticket.getNumberOfEdits() + "]") : ""));
            sender.sendMessage("     " + ChatColor.GRAY + ticket.getMessage());
        }
        sender.sendMessage("");
        sender.sendMessage(StringUtil.generateHLineTitle("Page " + page + " of " + getNumberOfActivePages()));
    }

    private void listHistory(CommandSender sender, int page) {
        ArrayList<Ticket> closedTickets = new ArrayList<>();
        allTickets.values().forEach(
                (t) -> {
                    if (!t.isOpen()) {
                        closedTickets.add(t);
                    }
                });

        Collections.sort(closedTickets, (a, b) -> {
            if (b.getCreationTime() > a.getCreationTime()) {
                return 1;
            } else if (b.getCreationTime() < a.getCreationTime()) {
                return -1;
            }
            return 0;
        });

        int closedPages = getNumberOfClosedPages();
        if (closedPages == 0) {
            displayMessage(sender, "There are no closed tickets on this board!");
            return;
        }
        if (page <= 0) {
            page = 1;
        }
        if (page > closedPages) {
            page = closedPages;
        }
        sender.sendMessage(StringUtil.generateHLineTitle("Recently closed " + boardname.toUpperCase() + " Tickets"));
        sender.sendMessage("");
        for (int i = JOBS_PER_PAGE * (page - 1); i < JOBS_PER_PAGE * page && i < closedTickets.size(); i++) {
            Ticket ticket = closedTickets.get(i);
            sender.sendMessage(ChatColor.DARK_GRAY + " ["
                    + ChatColor.BLUE + "@" + ticket.getUniqueId()
                    + ChatColor.DARK_GRAY + "] "
                    + ChatColor.DARK_AQUA + ticket.getAuthorName()
                    + ChatColor.AQUA + ", closed by "
                    + ChatColor.DARK_AQUA + ticket.getClosedByName()
                    + ChatColor.AQUA + " on " + Tickets.DATE_FORMAT.format(Date.from(Instant.ofEpochMilli(ticket.getCloseTime())))
                    + ((ticket.getNumberOfEdits() > 0)
                    ? ("  " + ChatColor.AQUA + ChatColor.ITALIC
                    + "[Comments: " + ticket.getNumberOfEdits() + "]") : ""));
            sender.sendMessage("     " + ChatColor.GRAY + ticket.getMessage());
        }
        sender.sendMessage("");
        sender.sendMessage(StringUtil.generateHLineTitle("Page " + page + " of " + getNumberOfClosedPages()));
    }

    private void displayTicketDetail(CommandSender sender, Ticket job) {
        sender.sendMessage(StringUtil.generateHLineTitle(boardname.toUpperCase() + "-@" + job.getUniqueId() + " - Details"));
        job.sendDetail(sender);
        sender.sendMessage(StringUtil.generateHLineTitle(""));
    }

    private void displayDescription(CommandSender sender) {
        displayMessage(sender, boardDescription);
        displayCommandUsage(sender, "{message}", "Create a Ticket");
        if (hasPermission(sender, "list")) {
            displayCommandUsage(sender, "[id /@uid]", "Display all details of a Ticket");
        }
        if (hasPermission(sender, "comment")) {
            displayCommandUsage(sender, "[id / @uid] {comment}", "Add a comment to a ticket");
        }
        if (hasPermission(sender, "list")) {
            displayCommandUsage(sender, "list [page]", "List open Tickets");
        }
        if (hasPermission(sender, "warp")) {
            displayCommandUsage(sender, "warp [id /@uid]", "Teleport to where a ticket was created");
        }
        if (hasPermission(sender, "close")) {
            displayCommandUsage(sender, "done [id /@uid]", "Close a Ticket");
        }
    }

    public void displayMessage(CommandSender sender, String message) {
        sender.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + boardname.toUpperCase() + ": "
                + ChatColor.GRAY + message);
    }

    private void displayCommandUsage(CommandSender sender, String syntax, String description) {
        sender.sendMessage(ChatColor.BLUE + "/" + boardname + " " + syntax + " "
                + ChatColor.DARK_GRAY + "-"
                + ChatColor.GRAY + " " + description);
    }

    private boolean ensurePermission(CommandSender sender, String action) {
        if (hasPermission(sender, action)) {
            return true;
        }
        displayMessage(sender, "You do not have permission to use \"" + action + "\"!");
        return false;
    }

    public boolean hasPermission(CommandSender sender, String action) {
        return sender.hasPermission("tickets." + boardname + "." + action)
                || sender.hasPermission("tickets." + boardname + ".*")
                || sender.hasPermission("tickets.*");
    }

    public Ticket getJob(String jobTag) {
        if (jobTag.startsWith("@")) {
            return allTickets.get(jobTag);
        } else {
            int i = Integer.parseInt(jobTag) - 1;
            if (i >= activeTickets.size() || i < 0) {
                return null;
            }
            return activeTickets.get(i);
        }
    }

    public void createTicket(Player player, String message) {
        Ticket job = new Ticket(player, message, nextUniqueId++);
        activeTickets.add(job);
        allTickets.put("@" + job.getUniqueId(), job);
        String notification = "" + ChatColor.BLUE + ChatColor.BOLD + boardname.toUpperCase() + ": "
                + ChatColor.GRAY + player.getName() + " has created " + boardname.toUpperCase() + "-@" + job.getUniqueId();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (hasPermission(p, "notify")) {
                p.sendMessage(notification);
            }
        }
    }

    public int getNumberOfActiveTickets() {
        return activeTickets.size();
    }

    public int getNumberOfActivePages() {
        return activeTickets.size() / JOBS_PER_PAGE
                + ((activeTickets.size() % JOBS_PER_PAGE == 0) ? 0 : 1);
    }

    public int getNumberOfClosedTickets() {
        return allTickets.size() - activeTickets.size();
    }

    public int getNumberOfClosedPages() {
        return getNumberOfClosedTickets() / JOBS_PER_PAGE
                + ((getNumberOfClosedTickets() % JOBS_PER_PAGE == 0) ? 0 : 1);
    }

    public String getName() {
        return boardname;
    }

    public boolean isEmpty() {
        return activeTickets.isEmpty();
    }

    public void purge() {
        long now = System.currentTimeMillis();
        Iterator<Entry<String, Ticket>> historyIterator = allTickets.entrySet().iterator();
        while (historyIterator.hasNext()) {
            Entry<String, Ticket> entry = historyIterator.next();
            if (!entry.getValue().isOpen() && now - entry.getValue().getCloseTime() > MAX_AGE_MILLIS) {
                historyIterator.remove();
            }
        }
    }

    public void save(File file) throws IOException {
        List<YamlConfiguration> jobSections = new LinkedList<>();
        for (Ticket job : allTickets.values()) {
            jobSections.add(job.save());
        }
        YamlConfiguration config = YamlConfiguration.emptyConfiguration();
        config.set("next-unique-id", nextUniqueId);
        config.set("tickets", jobSections);
        config.save(file);
    }
}
