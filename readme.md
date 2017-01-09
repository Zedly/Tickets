Tickets is a lightweight ticket management system hosting a configurable sets of boards.
Each board has its own command for access which appears as an item in the /help command, its own set of permissions, save file and history.
Tickets can be viewed, teleported to and commented by server staff.

In the following sections, an example ticket board with the name "ti" is used for illustration purposes.
The name of the board determines its title as displayed in ticket lists as well as the command to access it and the set of permissions for management.

Every ticket has a unique ID according to the pattern TI-@1234. Within each board, a ticket can be accessed using the @1234 portion.
In addition, open tickets have a short index, which is simply its position in the list of open tickets.

#Commands

###/ti {message}
Creates a new ticket with the provided message.
Example: /ti my house has been griefed.
Permission: *tickets.ti*

###/ti list [page]
Lists open tickets page-wise.
Example: /ti list, /ti list 2
Permission: *tickets.ti.list*

###/ti [index / @uid]
Displays all available details on one ticket. This includes date and location of creation, author and subsequently added admin comments.
Example: /ti 1, /ti @315
Permission: *tickets.ti.list*

###/ti [index / @uid] {message}
Adds a comment to the specified ticket. This will appear in /ti [index / @uid]. Accepts ampersand color codes.
If a comment is made on a closed ticket, the ticket will be re-opened.
Example: /ti 5 The griefer's identity is DankHax0rz420
Permission: *tickets.ti.comment*

###/ti warp [index / @uid]
Teleports the user to the location where the specified ticket has been created.
Example: /ti warp 4
Permission: *tickets.ti.warp*

###/ti done [index / @uid]
Marks a ticket as closed. The ticket be removed from the list of active tickets, but remain in the history until the configuration entry max-age-days has elapsed.
Example: /ti done 4
Permission: *tickets.ti.close*


#Chat
At a configurable interval, notifications about open tickets are broadcast to all users with the permission *tickets.ti.notify*.

In addition, notifications will be broadcast to users with *tickets.ti.notify-create* when a ticket is created.