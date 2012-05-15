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

public class BPPlants extends BP
{
    public BPPlants(String id)
    {
        super(id);
    }
    
    public void popBlock(World world, Random rand, int x, int z, Biome biome)
    {
        /*int y = world.getHighestBlockYAt(x, z);
        switch(biome)
        {
            case DESERT:
            case DESERT_HILLS:
                if(rand.nextInt() == 0)
                break;
            case TAIGA:
            case TAIGA_HILLS:
            case ICE_PLAINS:
            case ICE_MOUNTAINS:
                break;
            case SWAMPLAND:
                break;
            case FOREST:
            case FOREST_HILLS:
            case JUNGLE:
            case JUNGLE_HILLS:
                break;
            case PLAINS:
            case EXTREME_HILLS:
            case SMALL_MOUNTAINS:
                break;
            case MUSHROOM_ISLAND:
            case MUSHROOM_SHORE:
                break;
            case FROZEN_OCEAN:
            case ICE_DESERT:
            case ICE_PLAINS:
            case ICE_MOUNTAINS:
            case TAIGA:
            case TAIGA_HILLS:
                
                if(Block.byId[78].canPlace(((CraftWorld)world).getHandle(), x, y, z))
                {
                    byte meta = 0;
                    if(isSet("a"))
                    {
                        meta = (byte)rand.nextInt(8);
                    }
                    world.getBlockAt(x, y, z).setTypeIdAndData(78, meta, false);
                }
                break;
            default: break;
        }*/
    }
}