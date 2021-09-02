package prime.lootfountain;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import prime.lootfountain.commands.*;
import prime.lootfountain.listeners.DamageEventListener;
import prime.lootfountain.listeners.InventoryClickListener;
import java.util.Objects;

public final class LootFountain extends JavaPlugin {

    public FountainManager fountainManager;
    public boolean debugMessages = true;




    @Override
    public void onEnable() {
        // Plugin startup logic
        fountainManager = new FountainManager(this);

        Objects.requireNonNull(getCommand("AddFountainItem")).setExecutor(new AddFountainItem(this));
        Objects.requireNonNull(getCommand("ViewFountains")).setExecutor(new ViewFountains(this));
        Objects.requireNonNull(getCommand("CreateFountain")).setExecutor( new CreateFountain(this));

        InventoryClickListener inventoryClickListener = new InventoryClickListener(this);
        DamageEventListener damageEventListener = new DamageEventListener(this);

        getConfig().options().copyDefaults(true);
        saveConfig();

    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        fountainManager.removeFountainEntities();
    }



}
