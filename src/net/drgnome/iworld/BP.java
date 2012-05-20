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
    
    public abstract void popBlock(World world, Random rand, int x, int z, Biome biome);
    
    public static void set(World world, int x, int y, int z, int id)
    {
        world.getBlockAt(x, y, z).setTypeId(id);
    }
    
    public static void set(World world, int x, int y, int z, int id, boolean update)
    {
        world.getBlockAt(x, y, z).setTypeId(id, update);
    }
    
    public static void set(World world, int x, int y, int z, int id, int meta)
    {
        set(world, x, y, z, id, meta, false);
    }
    
    public static void set(World world, int x, int y, int z, int id, int meta, boolean update)
    {
        world.getBlockAt(x, y, z).setTypeIdAndData(id, (byte)meta, update);
    }
    
    public static int get(World world, int x, int y, int z)
    {
        return world.getBlockTypeIdAt(x, y, z);
    }
    
    public static int getMeta(World world, int x, int y, int z)
    {
        return (int)world.getBlockAt(x, y, z).getData();
    }
    
    public static int getMaxY(World world, int x, int z)
    {
        if(world == null)
        {
            return 0;
        }
        int y = 255;
        while(world.getBlockTypeIdAt(x, y, z) == 0)
        {
            y--;
        }
        return y;
    }
}