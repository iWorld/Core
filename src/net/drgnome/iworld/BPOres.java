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

public class BPOres extends BP
{
    public BPOres(String id)
    {
        super(id);
    }
    
    public void popBlock(World world, Random rand, int x, int z, Biome biome)
    {
        int[] params = new int[]{   2000, 2, 20, 1, 0, 56, // Diamond
                                    1500, 2, 30, 1, 0, 14, // Gold
                                     500, 2, 40, 1, 1, 15, // Iron
                                     200, 2, 80, 2, 1, 73, // Redstone
                                    1000,10,150, 1, 0, 21, // Lapis Lazuli
                                     200,10,200, 1, 1, 16}; // Coal
        int a, b, c, d, i, x0, y0, z0;
        for(i = 0; i < 6; i++)
        {
            a = params[(i * 6) + 3] < (x % 16) ? params[(i * 6) + 3] : (x % 16);
            b = params[(i * 6) + 4] < (15 - (x % 16)) ? params[(i * 6) + 4] : (15 - (x % 16));
            c = params[(i * 6) + 3] < (z % 16) ? params[(i * 6) + 3] : (z % 16);
            d = params[(i * 6) + 4] < (15 - (z % 16)) ? params[(i * 6) + 4] : (15 - (z % 16));
            for(int y = params[(i * 6) + 1]; y < params[(i * 6) + 2]; y++)
            {
                if(rand.nextInt(params[i * 6]) == 0)
                {
                    for(x0 = x - a; x0 <= x + b; x0++)
                    {
                        for(y0 = y - params[(i * 6) + 3]; y0 <= y + params[(i * 6) + 4]; y0++)
                        {
                            for(z0 = z - c; z0 <= z + d; z0++)
                            {
                                if((rand.nextInt(2) == 0) && (get(world, x, y, z) == 1))
                                {
                                    set(world, x0, y0, z0, params[(i * 6) + 5]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}