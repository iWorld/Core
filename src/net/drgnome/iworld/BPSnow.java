// Bukkit Plugin "iWorld" by Siguza and steffengy
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.iworld;

import java.util.Random;

import net.minecraft.server.*;

import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.block.Biome;
import org.bukkit.Chunk;
import org.bukkit.World;

public class BPSnow extends BP
{
    public BPSnow(String id)
    {
        super(id);
    }
    
    public void popBlock(World world, Random rand, int x, int z, Biome biome)
    {
        switch(biome)
        {
            case ICE_PLAINS:
            case ICE_MOUNTAINS:
            case TAIGA:
            case TAIGA_HILLS:
                set(world, x, getMaxY(world, x, z) + 1, z, 78, isSet("a") ? (byte)rand.nextInt(8) : 0);
                break;
            default: break;
        }
    }
}