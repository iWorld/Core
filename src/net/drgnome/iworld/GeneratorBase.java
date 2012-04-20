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
        blocks = new byte[16][4096];
        int x, y, z;
        for(x = 0; x < 16; x++)
        {
            for(z = 0; z < 16; z++)
            {
                int max = getBiome(x + i * 16, z + j * 16, world.getSeed());
                max = max > 255 ? 255 : max;
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
                set(x, 0, z, 7);
            }
        }
        return blocks;
    }
    
    public static void set(int x, int y, int z, int id)
    {
        try
        {
            blocks[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = (byte)id;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static int getBiome(int i, int j, long seed)
    {
        // return (int)Math.round(Math.cos(Math.pow(((x * z) % 3) + 1, Math.abs(Math.sin((x ^ r3) / r1) * Math.cos((z ^ r4) / r2))) / 10) * 96) + 64;
        /*double value = (x * z) % 3;
        value = Math.pow(value, (double)Math.abs(Math.sin((x * r1) ^ r3) * Math.cos((z * r2) ^ r4)));
        value = Math.cos(value / 128) * 96;
        return (int)Math.round(value) + 160;*/
        return 128 + (int)Math.round(getFactor(i, j, seed) * 32) + (int)Math.round((getFactor((double)i / 1000, (double)j / 1000, seed) - 0.5) * (-64));
    }
    
    public static double getFactor(double i, double j, long seed)
    {
        double x = i / 100D;
        double z = j / 100D;
        Random rand = new Random(seed);
        float r1 = (float)rand.nextInt(100) / 10F;
        float r2 = (float)rand.nextInt(100) / 10F;
        return Math.cos(Math.pow((x * z) % 3, Math.abs(Math.sin(x * r1) * Math.cos(z * r2))));
    }
}