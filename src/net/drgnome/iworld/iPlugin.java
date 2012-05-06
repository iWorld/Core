// Bukkit Plugin "iWorld" by Siguza and steffengy
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.iworld;

import java.util.logging.Logger;

import net.minecraft.server.*;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.generator.ChunkGenerator;

public class iPlugin extends JavaPlugin
{
    public static String version = "Alpha 0.1.0";
    private Logger log = Logger.getLogger("Minecraft");

    public void onEnable()
    {
        log.info("Enabling iWorld " + version);
    }

    public void onDisable()
    {
        log.info("Disabling iWorld " + version);
    }
    
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
    {
        return new iWorld(id);
    }
}