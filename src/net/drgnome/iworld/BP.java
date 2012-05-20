// Bukkit Plugin "iWorld" by Siguza and steffengy
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.iworld;

import java.util.Random;

import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.block.Biome;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.*;

public abstract class BP extends BlockPopulator
{
    private String genID;
    
    public BP(String id)
    {
        genID = id == null ? "" : id;
    }
    
    public boolean isSet(String ch)
    {
        return genID.contains(ch);
    }
    
    public void populate(World world, Random rand, Chunk chunk)
    {
        rand.setSeed((long)((int)world.getSeed() ^ (chunk.getX() | chunk.getZ())));
        rand.setSeed(rand.nextLong());
        try
        {
            pop(world, rand, chunk.getX() * 16, chunk.getZ() * 16);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void pop(World world, Random rand, int x0, int z0)
    {
        int x, z;
        for(x = x0; x < x0 + 16; x++)
        {
            for(z = z0; z < z0 + 16; z++)
            {
                popBlock(world, rand, x, z, world.getBiome(x, z));
            }
        }
    }
    
    public void popBlock(World world, Random rand, int x, int z, Biome biome)
    {
        
    }
}