/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zedly.tickets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author Dennis
 */
public class TaskPurgeAndNotify implements Runnable {
    
    private static final TaskPurgeAndNotify INSTANCE = new TaskPurgeAndNotify();
    
    public static TaskPurgeAndNotify instance() {
        return INSTANCE;
    }
    
    private TaskPurgeAndNotify() {
    }
    
    public void run() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            for(Board board : Tickets.BOARDS.values()) {
                board.purge();
                if(!board.isEmpty() && board.hasPermission(p, "notify")) {
                    board.displayMessage(p, board.getNumberOfActiveTickets() + " open tickets");
                }
            }
        }
    }
}
