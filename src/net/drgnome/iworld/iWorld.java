// Bukkit Plugin "iWorld" by Siguza and steffengy
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.iworld;

import java.util.*;

import net.minecraft.server.*;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.generator.*;
import org.bukkit.block.Biome;

public class iWorld extends ChunkGenerator
{
    String genID;
    
    public iWorld(String s)
    {
        genID = s;
    }
    
    public boolean canSpawn(World world, int x, int z)
    {
        return true;
    }
    
    public Location getFixedSpawnLocation(World world, Random rand)
    {
        return new Location(world, 0, 256, 0);
    }
    
    public short[][] generateExtBlockSections(World world, Random rand, int x, int z, BiomeGrid biomes)
    {
        try
        {
            byte[][] raw = generateBlockSections(world, rand, x, z, biomes);
            short[][] blocks = new short[16][4096];
            for(int i = 0; i < raw.length; i++)
            {
                for(int j = 0; j < raw[i].length; j++)
                {
                    blocks[i][j] = ((Byte)raw[i][j]).shortValue();
                }
            }
            return blocks;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return (short[][])null;
        }
    }
    
    public byte[][] generateBlockSections(World world, Random rand, int x, int z, BiomeGrid biomes)
    {
        return GeneratorBase.gen(world, x, z, genID, biomes);
    }
    
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
        return new ArrayList();
    }
}