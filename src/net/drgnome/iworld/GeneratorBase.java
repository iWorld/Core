// Bukkit Plugin "iWorld" by Siguza and steffengy
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.iworld;

import java.util.*;

import net.minecraft.server.*;

import org.bukkit.World;

public class GeneratorBase
{
    private static byte[][] blocks;
    
    public static byte[][] generate(World world, int i, int j, int gen)
    {
        blocks = new byte[16][];
        int x, y, z;
        /*long s1 = rand.nextLong() * (x ^ rand.nextInt() );
        long s2 = rand.nextLong() * (z ^ );
        rand.setSeed(s1 + s2 ^ world.getSeed());*/
        
        for(x = 0; x < 16; x++)
        {
            for(z = 0; z < 16; z++)
            {
                int max = getBiome(x + i * 16, z + j * 16, world.getSeed());
                for(y = 1; y <= max; y++)
                {
                    set(x, y, z, 1);
                }
            }
        }
        for(x = 0; x < 16; x++)
        {
            for(z = 0; z < 16; z++)
            {
                set(blocks, i, 0, j, 7);
            }
        }
        return blocks;
    }
    
    public static void set(int x, int y, int z, int id)
    {
        if(blocks[y >> 4] == null)
        {
            blocks[y >> 4] = new byte[4096];
        }
        blocks[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = (byte)id;
    }
    
    public static int getBiome(int x, int z, long seed)
    {
        Random rand = new Random(seed);
        int r1 = rand.nextInt(10) + 3;
        int r2 = rand.nextInt(10) + 3;
        int r3 = rand.nextInt(16);
        int r4 = rand.nextInt(16);
        return Math.round(Math.cos(Math.pow((x * z) % 3, Math.sin((x * r1) ^ r3) * Math.cos((z * r2) ^ r4)))) * 64) + 64;
    }
}