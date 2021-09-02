package prime.lootfountain;

import org.bukkit.ChatColor;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;



public class FountainManager {

    LootFountain plugin;
    ArrayList<Fountain> fountainList;


    public FountainManager(LootFountain plugin){
        this.plugin = plugin;
        if(plugin.debugMessages) plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Enabling things" );
        fountainList = new ArrayList<Fountain>();
        LoadFountainsFromConfigFunction(); //Get fountain list from config.
    }

    public void LoadFountainsFromConfigFunction(){
        fountainList = LoadFountainsFromConfig();
    }

    private ArrayList<Fountain> LoadFountainsFromConfig(){

        if(plugin.getConfig().getConfigurationSection("ID") == null){
            return new ArrayList<Fountain>();
        }

        ArrayList<Fountain> newFountainList = new ArrayList<>();

        ConfigurationSection configurationSection = plugin.getConfig().getConfigurationSection("ID");

        for(String fountainName : configurationSection.getKeys(false)){

            int dropEventInterval =  plugin.getConfig().getInt("ID." + fountainName + ".dropEventFrequency" );
            Location location = plugin.getConfig().getLocation("ID." + fountainName + ".location");
            double dropEventDuration = plugin.getConfig().getDouble("ID." + fountainName + ".dropEventDuration");
            double itemFrequency = plugin.getConfig().getDouble("ID." + fountainName + ".itemFrequency");

            Fountain fountain = new Fountain(plugin, fountainName, location, dropEventInterval, dropEventDuration, itemFrequency);
            fountain.startFountain();
            newFountainList.add(fountain);

        }

        return newFountainList;
    }


    public void removeFountain(String fountainID){

        if(plugin.debugMessages) plugin.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Searching to delete: " + ChatColor.BLUE + fountainID);

        Fountain removedFountain = null;
        //Find fountain in list and set fountain reference.
        for(Fountain fountain : fountainList){
            if(fountain.fountainID.equalsIgnoreCase(fountainID)){
                removedFountain = fountain;
            }
        }

        //If fountain has been found delete it.
        if(removedFountain != null) {
            //Delete from list
            if(plugin.debugMessages) plugin.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "Found and Removing fountain " + ChatColor.BLUE + fountainID);
            fountainList.remove(removedFountain);
            removedFountain.RemoveFountain();

            //Clear config of fountain
            plugin.getConfig().set("ID." + fountainID, null);
            plugin.saveConfig();
        }
    }


    public void addFountain(String fountainName, String lootTable, Location location, float dropEventInterval, double dropEventDuration, double itemFrequency){
        //Add to list
        Fountain fountain = new Fountain(plugin, fountainName, location, (int)dropEventInterval, dropEventDuration, itemFrequency);
        fountain.startFountain();
        fountainList.add(fountain);

        //Update Config
        plugin.getConfig().createSection("ID." + fountainName);
        plugin.getConfig().set("ID." + fountainName + ".loottable", lootTable);
        plugin.getConfig().set("ID." + fountainName + ".location", location);
        plugin.getConfig().set("ID." + fountainName + ".dropEventFrequency", dropEventInterval);

        plugin.getConfig().set("ID." + fountainName + ".dropEventDuration", dropEventDuration);
        plugin.getConfig().set("ID." + fountainName + ".itemFrequency", itemFrequency);



        plugin.saveConfig();
    }



    //Used on reload, to remove fountain tags.
    public void removeFountainEntities(){
        for(Fountain fountain : fountainList){
            fountain.removeFountainNameTag();
        }
    }

}
