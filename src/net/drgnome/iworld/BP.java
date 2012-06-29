// Bukkit Plugin "iWorld" by Siguza and steffengy
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.iworld;

import java.util.Random;
import java.util.logging.Logger;

import net.minecraft.server.*;

import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.block.Biome;
import org.bukkit.Chunk;
// import org.bukkit.World;
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
    
    public void populate(org.bukkit.World world, Random rand, Chunk chunk)
    {
        if(world instanceof CraftWorld)
        {
            World worldObj = ((CraftWorld)world).getHandle();
            rand.setSeed(worldObj.getSeed());
            long l = (rand.nextLong() / 2L) * 2L + 1L;
            long l1 = (rand.nextLong() / 2L) * 2L + 1L;
            rand.setSeed((long)chunk.getX() * l + (long)chunk.getZ() * l1 ^ worldObj.getSeed());
            try
            {
                pop(world, worldObj, rand, chunk.getX() * 16, chunk.getZ() * 16);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Logger.getLogger("Minecraft").severe("[iWorld] World not an instance of CraftWorld!");
            Logger.getLogger("Minecraft").severe("[iWorld] Instance of: " + world.getClass().getName());
            return;
        }
        /*rand.setSeed((long)((int)world.getSeed() ^ (chunk.getX() | chunk.getZ())));
        rand.setSeed(rand.nextLong());*/
    }
    
    public void pop(org.bukkit.World world, World worldObj, Random rand, int x0, int z0)
    {
        int x, z;
        for(x = x0; x < x0 + 16; x++)
        {
            for(z = z0; z < z0 + 16; z++)
            {
                popBlock(worldObj, rand, x, z, world.getBiome(x, z));
            }
        }
    }
    
    public abstract void popBlock(World world, Random rand, int x, int z, Biome biome);
    
    public static void set(World world, int x, int y, int z, int id)
    {
        world.setTypeId(x, y, z, id);
    }
    
    public static void set(World world, int x, int y, int z, int id, int meta)
    {
        world.setTypeIdAndData(x, y, z, id, meta);
    }
    
    public static int get(World world, int x, int y, int z)
    {
        return world.getTypeId(x, y, z);
    }
    
    public static int getMeta(World world, int x, int y, int z)
    {
        return world.getData(x, y, z);
    }
    
    public static int getMaxY(World world, int x, int z)
    {
        if(world == null)
        {
            return 0;
        }
        int y = 255;
        while(world.getTypeId(x, y, z) == 0)
        {
            y--;
        }
        return y;
    }
}