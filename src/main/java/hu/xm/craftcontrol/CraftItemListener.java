package hu.xm.craftcontrol;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.logging.Level;

public class CraftItemListener implements Listener {
    private final EnumSet<Material> deniedCrafts;
    private final String message;
    private final boolean closeOnDeny;
    private final boolean denyAll;

    public CraftItemListener(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();

        String messageEntry = config.getString("deny-message");
        this.message = messageEntry == null ? "" : ChatColor.translateAlternateColorCodes('&', messageEntry);
        this.closeOnDeny = config.getBoolean("close-inventory-on-deny");
        this.denyAll = config.getBoolean("deny-all-crafting-recipes");

        if(denyAll) {
            deniedCrafts = null;
            plugin.getLogger().log(Level.WARNING, "deny-all-crafting-recipes is set to 'true', denying ALL crafting recipes.");
        } else {
            deniedCrafts = EnumSet.noneOf(Material.class);
            for(String entry : config.getStringList("denied-items")) {
                String materialName = entry.toUpperCase();

                try {
                    Material mat = Material.valueOf(materialName);
                    deniedCrafts.add(mat);
                } catch(IllegalArgumentException noonecares) {
                    plugin.getLogger().log(Level.SEVERE, "Invalid material name in 'denied-items' list: " + materialName);
                }
            }

            plugin.getLogger().log(Level.INFO, "deny-all-crafting-recipes is set to 'false', denying the crafting of " + deniedCrafts.size() + " item types.");
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent e) {
        if((denyAll || (e.getCurrentItem() != null && deniedCrafts.contains(e.getCurrentItem().getType()))) && !e.getWhoClicked().hasPermission("craftcontrol.bypass")) {
            e.setCancelled(true);

            if(!message.isEmpty()) {
                e.getWhoClicked().sendMessage(message);
            }

            if(closeOnDeny) {
                e.getWhoClicked().closeInventory();
            }
        }
    }

}
