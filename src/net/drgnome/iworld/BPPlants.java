// Bukkit Plugin "iWorld" by Siguza and steffengy
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.iworld;

import java.util.Random;

import net.minecraft.server.*;

import org.bukkit.block.Biome;

public class BPPlants extends BP
{
    public BPPlants(String id)
    {
        super(id);
    }
    
    public void popBlock(World world, Random rand, int x, int z, Biome biome)
    {
        int i;
        int y = getMaxY(world, x, z) + 1;
        switch(biome)
        {
            case DESERT:
            case DESERT_HILLS:
                if(rand.nextInt(400) == 0)
                {
                    set(world, x, y, z, 32); // Dead bush
                }
                break;
            case TAIGA:
            case TAIGA_HILLS:
            case ICE_PLAINS:
            case ICE_MOUNTAINS:
                i = rand.nextInt(225);
                if(i < 2)
                {
                    set(world, x, y, z, 37 + i); // Flower / Rose
                }
                break;
            case SWAMPLAND:
                if(rand.nextInt(30) == 0)
                {
                    set(world, x, y, z, 111); // Water lily
                }
                break;
            case MUSHROOM_ISLAND:
            case MUSHROOM_SHORE:
                i = rand.nextInt(25);
                if(i < 2)
                {
                    set(world, x, y, z, 39 + i); // Brown or red mushroom
                }
                break;
            case PLAINS:
            case EXTREME_HILLS:
            case SMALL_MOUNTAINS:
                i = rand.nextInt(1000);
                if(i < 2)
                {
                    set(world, x, y, z, 37); // Flower
                }
                else if(i < 4)
                {
                    set(world, x, y, z, 38); // Rose
                }
                else if(i < 100)
                {
                    set(world, x, y, z, 31, 1); // Tall grass
                }
                else if(i == 100)
                {
                    set(world, x, y, z, 31, 2); // Fern
                }
                break;
            case FOREST:
            case FOREST_HILLS:
                i = rand.nextInt(1000);
                if(i < 3)
                {
                    set(world, x, y, z, 37); // Flower
                }
                else if(i < 6)
                {
                    set(world, x, y, z, 38); // Rose
                }
                else if(i < 25)
                {
                    set(world, x, y, z, 31, 1); // Tall grass
                }
                else if(i < 40)
                {
                    set(world, x, y, z, 31, 2); // Fern
                }
                break;
            case JUNGLE:
            case JUNGLE_HILLS:
                i = rand.nextInt(1000);
                if(i < 5)
                {
                    set(world, x, y, z, 37); // Flower
                }
                else if(i < 10)
                {
                    set(world, x, y, z, 38); // Rose
                }
                else if(i < 150)
                {
                    set(world, x, y, z, 31, 1); // Tall grass
                }
                else if(i < 200)
                {
                    set(world, x, y, z, 31, 2); // Fern
                }
                break;
        }
    }
}