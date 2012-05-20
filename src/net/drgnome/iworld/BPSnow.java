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

public class BPSnow extends BP
{
    public BPSnow(String id)
    {
        super(id);
    }
    
    public void popBlock(World world, Random rand, int x, int z, Biome biome)
    {
        switch(biome)
        {
            case ICE_PLAINS:
            case ICE_MOUNTAINS:
            case TAIGA:
            case TAIGA_HILLS:
                int y = world.getHighestBlockYAt(x, z);
                while(world.getBlockTypeIdAt(x, y, z) != 0)
                {
                    y++;
                }
                if(Block.byId[78].canPlace(((CraftWorld)world).getHandle(), x, y, z))
                {
                    byte meta = 0;
                    if(isSet("a"))
                    {
                        meta = (byte)rand.nextInt(8);
                    }
                    world.getBlockAt(x, y, z).setTypeIdAndData(78, meta, false);
                }
                break;
            default: break;
        }
    }
}