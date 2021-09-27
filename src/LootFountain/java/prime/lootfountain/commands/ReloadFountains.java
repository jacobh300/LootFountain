package prime.lootfountain.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import prime.lootfountain.FountainManager;
import prime.lootfountain.LootFountain;

public class ReloadFountains implements CommandExecutor {

    private LootFountain plugin;

    public ReloadFountains(LootFountain plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if(!commandSender.hasPermission("lootfountain.edit")){
            commandSender.sendMessage(ChatColor.RED + "Insufficient Permissions");
            return true;
        }

        //Remove all fountains.
        plugin.fountainManager.removeFountainEntities();
        //Refresh config.
        plugin.reloadConfig();
        //Reload all fountains from config.
        plugin.fountainManager.LoadFountainsFromConfigFunction();

        commandSender.sendMessage(ChatColor.GREEN + "Reload Successful");
        return true;
    }
}
