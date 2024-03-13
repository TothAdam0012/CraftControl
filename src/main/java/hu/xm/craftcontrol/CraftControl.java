package hu.xm.craftcontrol;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public final class CraftControl extends JavaPlugin {

    @Override
    public void onEnable() {
        // Creating the default config if it doesn't already exist
        saveDefaultConfig();

        // adding default values to the config in case some missing
        getConfig().options().copyDefaults(true);
        getConfig().addDefault("deny-method", "DENY");
        getConfig().addDefault("deny-message", "&oCrafting this item is not permitted!");
        getConfig().addDefault("close-inventory-on-deny", true);
        getConfig().addDefault("denied-items", List.of("ACACIA_BOAT", "ACACIA_BUTTON", "ACACIA_CHEST_BOAT", "ACACIA_DOOR"));
        getConfig().addDefault("deny-all-crafting-recipes", false);

        String denyMethod = getConfig().getString("deny-method");
        if(denyMethod == null || ( !denyMethod.equalsIgnoreCase("DENY") && !denyMethod.equalsIgnoreCase("REMOVE") )) {
            getLogger().log(Level.INFO, "Value of 'deny-method' in the main config has to be either 'DENY' or 'REMOVE' but found '" + denyMethod + "', defaulting to using 'DENY'.");
            denyMethod = "DENY";
            getConfig().set("deny-method", denyMethod);
        }
        saveConfig();

        if(denyMethod.equalsIgnoreCase("REMOVE")) {
            getLogger().log(Level.INFO, "Deny method is set to 'REMOVE'.");
            // Removing chosen recipes via the recipe iterator

            // Removing all recipes
            if(getConfig().getBoolean("deny-all-crafting-recipes")) {
                getLogger().log(Level.WARNING, "deny-all-crafting-recipes is set to 'true', removing ALL crafting recipes.");
                Iterator<Recipe> it = Bukkit.recipeIterator();
                while(it.hasNext()) {
                    if(it.next() instanceof CraftingRecipe) {
                        it.remove();
                    }
                }
                // Removing recipes that result in any of the materials given in the config
            } else {
                // Building an EnumSet to check recipe results' types against it
                EnumSet<Material> deniedCrafts = EnumSet.noneOf(Material.class);
                for(String entry : getConfig().getStringList("denied-items")) {
                    String materialName = entry.toUpperCase();

                    try {
                        Material mat = Material.valueOf(materialName);
                        deniedCrafts.add(mat);
                    } catch(IllegalArgumentException noonecares) {
                        getLogger().log(Level.SEVERE, "Invalid material name in 'denied-items' list: " + materialName);
                    }
                }

                getLogger().log(Level.INFO, "deny-all-crafting-recipes is set to 'false', removing the crafting recipes of " + deniedCrafts.size() + " item types.");

                Iterator<Recipe> it = Bukkit.recipeIterator();
                while(it.hasNext()) {
                    if(it.next() instanceof CraftingRecipe craftingRecipe && deniedCrafts.contains(craftingRecipe.getResult().getType())) {
                        it.remove();
                    }
                }
            }
        } else {
            getLogger().log(Level.INFO, "Deny method is set to 'DENY'.");
            // deny-method is 'DENY', the CraftEvent handles the recipes
            Bukkit.getPluginManager().registerEvents(new CraftItemListener(this), this);
        }


        getLogger().log(Level.INFO, "CraftControl started.");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "CraftControl stopped.");
    }
}
