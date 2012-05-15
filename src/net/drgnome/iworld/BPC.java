// Bukkit Plugin "iWorld" by Siguza and steffengy
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.iworld;

import java.util.Random;

import org.bukkit.block.Biome;
import org.bukkit.Chunk;
import org.bukkit.World;

public abstract class BPC extends BP
{
    public BPC(String id)
    {
        super(id);
    }
    
    public abstract void pop(World world, Random rand, int x0, int z0);
}