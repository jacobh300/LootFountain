package prime.lootfountain.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import prime.lootfountain.LootFountain;

public class DamageEventListener implements Listener {

    LootFountain plugin;

    public DamageEventListener(LootFountain plugin){
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void entityDamage(EntityDamageEvent event){
        if(event.getEntityType() == EntityType.ARMOR_STAND){
            if(event.getEntity().isInvulnerable()) event.setCancelled(true);
        }
    }

    @EventHandler
    public void entityDamage(EntityDamageByEntityEvent event){
        if(event.getEntityType() == EntityType.ARMOR_STAND){
            if(event.getEntity().isInvulnerable()) event.setCancelled(true);
        }
    }

}
