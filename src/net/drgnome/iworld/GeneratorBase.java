// Bukkit Plugin "iWorld" by Siguza and steffengy
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.iworld;

import java.util.*;

import net.minecraft.server.*;

import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.block.Biome;
import org.bukkit.World;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class GeneratorBase
{
    private byte[][] blocks;
    private String genID;
    // 0 : Randomize terrain
    // 1 : No overhangs/floating parts
    // 2 : More flat world
    // 3 : Less flat world
    private SimplexOctaveGenerator[] gen;
    // 0 : Stone
    // 1 : Whatever
    // 2 : Extended terrain height
    // 3 : Extended terrain
    // 4 : Whatever height
    // 5 : Whatever (smaller)
    // 6 : A Randomizer
    
    public GeneratorBase(long seed, String id)
    {
        blocks = new byte[16][4096];
        genID = (id == null) ? "" : id;
        gen = new SimplexOctaveGenerator[7];
        gen[0] = new SimplexOctaveGenerator(seed, 8);
        gen[0].setScale(1.0/512.0);
        gen[1] = new SimplexOctaveGenerator(seed, 8);
        gen[1].setScale(1.0/192.0);
        gen[2] = new SimplexOctaveGenerator(seed, 8);
        gen[2].setScale(1.0/(isSet("2") ? 1024.0 : 128.0));
        gen[3] = new SimplexOctaveGenerator(seed, 8);
        gen[3].setScale(1.0/384.0);
        gen[4] = new SimplexOctaveGenerator(seed, 8);
        gen[4].setScale(1.0/64.0);
        gen[5] = new SimplexOctaveGenerator(seed, 8);
        gen[5].setScale(1.0/16.0);
        gen[6] = new SimplexOctaveGenerator(seed, 8);
        gen[6].setScale(1.0/123.0);
    }
    
    public static byte[][] gen(World world, int chunkX, int chunkZ, String id, BiomeGrid biomes)
    {
        return (new GeneratorBase(world.getSeed(), id)).generate(world, chunkX, chunkZ);
    }
    
    /*public static int[] getBlocks()
    {
        
    }*/
    
    public static boolean isIn(int check, int... options)
    {
        for(int i = 0; i < options.length; i++)
        {
            if(check == options[i])
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isSet(String ch)
    {
        return genID.contains(ch);
    }
    
    public int getMaxY(int x, int z)
    {
        for(int y = 255; y >= 0; y--)
        {
            if(get(x, y, z) > 0)
            {
                return y;
            }
        }
        return 0;
    }
    
    public int get(int x, int y, int z)
    {
        if((x < 0) || (x >= 16) || (z < 0) || (z >= 16) || (y < 0) || (y >= 256))
        {
            return 0;
        }
        return blocks[y >> 4][((y & 0xF) << 8) | (z << 4) | x];
    }
    
    public void set(int x, int y, int z, int id)
    {
        set(x, y, z, id, false);
    }
    
    public void set(int x, int y, int z, int id, boolean override)
    {
        try
        {
            if((x < 0) || (x >= 16) || (z < 0) || (z >= 16) || (y < 0) || (y >= 256))
            {
                return;
            }
            if(override || (get(x, y, z) == 0))
            {
                blocks[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = (byte)id;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void fillup(int x, int z)
    {
        int y;
        for(y = 0; y < 64; y++)
        {
            set(x, y, z, 9);
        }
        for(y = 1; y < 256; y++)
        {
            if((get(x, y + 1, z) == 0) && isIn(get(x, y, z), 1, 3))
            {
                set(x, y, z, 2, true);
            }
        }
    }
    
    public byte[][] generate(World world, int chunkX, int chunkZ)
    {
        int x, y, z;
        int[] levels = new int[5];
        double factor;
        for(x = 0; x < 16; x++)
        {
            for(z = 0; z < 16; z++)
            {
                levels[0] = (int)Math.round(gen[0].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-50)) + 78;
                // Generating basic layer (stone)
                for(y = 1; y <= levels[0]; y++)
                {
                    set(x, y, z, 1);
                }
                levels[1] = (int)Math.round(gen[1].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-5)) + 88;
                // Generating second layer (dirt)
                for(y = 1; y <= levels[1]; y++)
                {
                    set(x, y, z, 3);
                }
                levels[2] = (int)Math.round(gen[2].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-110)) + 120;
                if(isSet("3"))
                {
                    System.
                    factor = 1.0D;
                }
                else if(isSet("2"))
                {
                    factor = 0.1D;
                }
                else
                {
                    factor = (gen[6].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) / 3.0) + 0.5;
                }
                // Generating some weird things
                // Overhangs off
                if(isSet("1"))
                {
                    for(y = 1; y < factor * (double)levels[2]; y++)
                    {
                        set(x, y, z, 3);
                    }
                }
                // Overhangs on
                else
                {
                    for(y = levels[0] + levels[1]; y < levels[2]; y++)
                    {
                        if(gen[3].noise(chunkX * 16 + x, y, chunkZ * 16 + z, 0.5, 0.5) >= 0)
                        {
                            set(x, (int)Math.floor((double)y * factor), z, 3);
                        }
                    }
                }
                if(isSet("0"))
                {
                    levels[3] = getMaxY(x, z);
                    levels[4] = (int)Math.round(gen[4].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-10)) + levels[3];
                    // Generating some small weird things
                    for(y = levels[3]; y != levels[4]; y += (levels[4] >= levels[3]) ? 1 : -1)
                    {
                        set(x, y, z, gen[3].noise(chunkX * 16 + x, y, chunkZ * 16 + z, 0.5, 0.5) >= 0 ? 3 : 0, true);
                    }
                }
                fillup(x, z);
                // You shall not pass
                set(x, 0, z, 7, true);
            }
        }
        return blocks;
    }
}