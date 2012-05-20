// Bukkit Plugin "iWorld" by Siguza and steffengy
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.iworld;

import java.util.*;

import org.bukkit.block.Biome;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class BiomeHandler
{
    private SimplexOctaveGenerator[] temp;
    private SimplexOctaveGenerator[] wet;
    private SimplexOctaveGenerator blubb;
    private boolean sea;
    
    public BiomeHandler(long seed, boolean big, boolean highsealevel)
    {
        temp = new SimplexOctaveGenerator[2];
        temp[0] = new SimplexOctaveGenerator(seed, 8);
        temp[1] = new SimplexOctaveGenerator(seed, 8);
        wet = new SimplexOctaveGenerator[2];
        wet[0] = new SimplexOctaveGenerator(seed, 8);
        wet[1] = new SimplexOctaveGenerator(seed, 8);
        blubb = new SimplexOctaveGenerator(seed, 8);
        if(big)
        {
            temp[0].setScale(1.0/7777.0);
            temp[1].setScale(1.0/159.0);
            wet[0].setScale(1.0/4444.0);
            wet[1].setScale(1.0/111.0);
            blubb.setScale(1.0/222.0);
        }
        else
        {
            temp[0].setScale(1.0/777.0);
            temp[1].setScale(1.0/15.0);
            wet[0].setScale(1.0/444.0);
            wet[1].setScale(1.0/11.0);
            blubb.setScale(1.0/22.0);
        }
        sea = highsealevel;
    }
    
    private int getSeaLevel()
    {
        return sea ? 128 : 64;
    }
    
    private double getTemp(int x, int z)
    {
        return temp[0].noise(x, z, 0.5, 0.5) + (temp[1].noise(x, z, 0.5, 0.5) / 10D);
    }
    
    private double getWet(int x, int z)
    {
        return wet[0].noise(x, z, 0.5, 0.5) + (wet[1].noise(x, z, 0.5, 0.5) / 10D);
    }
    
    public Biome get(int x, int y, int z)
    {
        if(y < getSeaLevel())
        {
            if(getWet(x, z) > 0.5)
            {
                return Biome.SWAMPLAND;
            }
            return (getTemp(x, z) < -0.25) ? Biome.FROZEN_OCEAN : Biome.OCEAN;
        }
        if(y == getSeaLevel())
        {
            return Biome.BEACH;
        }
        int id = t(getWet(x, z)) + (4 * t(getTemp(x, z))) + ((y > (sea ? 192 : 128)) ? 16 : 0);
        switch(id)
        {
            case 0:
            case 1: return Biome.ICE_PLAINS;
                
            case 2:
            case 4: return Biome.TAIGA;
            
            case 3: return Biome.MUSHROOM_ISLAND;
                
            case 5:
            case 6:
            case 7: return Biome.PLAINS;
                
            case 8:
            case 12: return Biome.DESERT;
                
            case 9: 
            case 10:
            case 13: return Biome.FOREST;
                
            case 11:
            case 14:
            case 15: return Biome.JUNGLE;
                
            case 16:
            case 17: return Biome.ICE_MOUNTAINS;
                
            case 18:
            case 20: return Biome.TAIGA_HILLS;
                
            case 19: return Biome.MUSHROOM_ISLAND;
            case 22: return Biome.EXTREME_HILLS;
                
            case 21:
            case 23: return Biome.SMALL_MOUNTAINS;
                
            case 24:
            case 28: return Biome.DESERT_HILLS;
                
            case 25:
            case 26:
            case 29: return Biome.FOREST_HILLS;
                
            case 27:
            case 30:
            case 31: return Biome.JUNGLE_HILLS;
        }
        return Biome.PLAINS;
    }
    
    // Double to useful int
    private int t(double d)
    {
        if(d < -0.5)
        {
            return 0;
        }
        if(d < 0)
        {
            return 1;
        }
        return (d < 0.5) ? 2 : 3;
    }
    
    // Get block types for a biome.
    // 0: Basic block (dirt for almost every biome)
    // 1: Second block (like sand for deserts)
    // 2: Overlaying block (like grass)
    // 3: Water block (frozen/not)
    public int[] getBlocks(int x, int z, Biome biome)
    {
        int[] blocks = new int[4];
        blocks[0] = getBlock0(biome);
        blocks[1] = getBlock1(x, z, biome);
        blocks[2] = getBlock2(x, z, biome);
        blocks[3] = getWater(biome);
        return blocks;
    }
    
    private static int getBlock0(Biome biome)
    {
        switch(biome)
        {
            case DESERT:
            case DESERT_HILLS: return 24; // Sandstone
            default: break;
        }
        return 3; // Dirt
    }
    
    private int getBlock1(int x, int z, Biome biome)
    {
        switch(biome)
        {
            case DESERT:
            case DESERT_HILLS: return 12; // Sand
            case SWAMPLAND:
            case OCEAN:
            case FROZEN_OCEAN: return blubb.noise(x, z, 0.5, 0.5) >= 0 ? 12 : 3; // Sand or dirt
            case ICE_DESERT: return 79; // Ice
            default: break;
        }
        return 3; // Dirt
    }
    
    private int getBlock2(int x, int z, Biome biome)
    {
        switch(biome)
        {
            case ICE_DESERT: return 80; // Snow block
            case DESERT:
            case DESERT_HILLS:
            case BEACH: return 12; // Sand
            case OCEAN:
            case FROZEN_OCEAN: return blubb.noise(x, z, 0.5, 0.5) >= 0 ? 12 : 3; // Sand or dirt
            case SWAMPLAND: return 9; // Water
            case MUSHROOM_ISLAND:
            case MUSHROOM_SHORE: return 110; // Mycelium
            default: break;
        }
        return 2; // Grass
    }
    
    private static int getWater(Biome biome)
    {
        switch(biome)
        {
            case FROZEN_OCEAN:
            case ICE_DESERT:
            case ICE_PLAINS:
            case ICE_MOUNTAINS:
            case TAIGA:
            case TAIGA_HILLS: return 79; // Ice
            default: break;
        }
        return 9; // Water
    }
}