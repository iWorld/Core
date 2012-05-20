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
        int i;
        int y = world.getHighestBlockYAt(x, z);
        while(world.getBlockTypeIdAt(x, y, z) != 0)
        {
            y++;
        }
        switch(biome)
        {
            case DESERT:
            case DESERT_HILLS:
                if(rand.nextInt(400) == 0)
                {
                    world.getBlockAt(x, y, z).setTypeId(32); // Dead bush
                }
                break;
            case TAIGA:
            case TAIGA_HILLS:
            case ICE_PLAINS:
            case ICE_MOUNTAINS:
                i = rand.nextInt(225);
                if(i < 2)
                {
                    world.getBlockAt(x, y, z).setTypeId(37 + i); // Flower / Rose
                }
                break;
            case SWAMPLAND:
                if(rand.nextInt(30) == 0)
                {
                    world.getBlockAt(x, y, z).setTypeId(111); // Water lily
                }
                break;
            case MUSHROOM_ISLAND:
            case MUSHROOM_SHORE:
                i = rand.nextInt(25);
                if(i < 2)
                {
                    world.getBlockAt(x, y, z).setTypeId(39 + i); // Brown or red mushroom
                }
                break;
            case PLAINS:
            case EXTREME_HILLS:
            case SMALL_MOUNTAINS:
                i = rand.nextInt(1000);
                if(i < 2)
                {
                    world.getBlockAt(x, y, z).setTypeId(37); // Flower
                }
                else if(i < 4)
                {
                    world.getBlockAt(x, y, z).setTypeId(38); // Rose
                }
                else if(i < 100)
                {
                    world.getBlockAt(x, y, z).setTypeIdAndData(31, (byte)1, true); // Tall grass
                }
                else if(i == 100)
                {
                    world.getBlockAt(x, y, z).setTypeIdAndData(31, (byte)2, true); // Fern
                }
                break;
            case FOREST:
            case FOREST_HILLS:
                i = rand.nextInt(1000);
                if(i < 3)
                {
                    world.getBlockAt(x, y, z).setTypeId(37); // Flower
                }
                else if(i < 6)
                {
                    world.getBlockAt(x, y, z).setTypeId(38); // Rose
                }
                else if(i < 25)
                {
                    world.getBlockAt(x, y, z).setTypeIdAndData(31, (byte)1, true); // Tall grass
                }
                else if(i < 40)
                {
                    world.getBlockAt(x, y, z).setTypeIdAndData(31, (byte)2, true); // Fern
                }
                break;
            case JUNGLE:
            case JUNGLE_HILLS:
                i = rand.nextInt(1000);
                if(i < 5)
                {
                    world.getBlockAt(x, y, z).setTypeId(37); // Flower
                }
                else if(i < 10)
                {
                    world.getBlockAt(x, y, z).setTypeId(38); // Rose
                }
                else if(i < 150)
                {
                    world.getBlockAt(x, y, z).setTypeIdAndData(31, (byte)1, true); // Tall grass
                }
                else if(i < 200)
                {
                    world.getBlockAt(x, y, z).setTypeIdAndData(31, (byte)2, true); // Fern
                }
                break;
        }
    }
}