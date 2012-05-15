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

import static net.drgnome.iworld.Util.*;

public class GeneratorBase
{
    private byte[][] blocks;
    private BiomeHandler bio;
    private String genID;
    // 0 : Randomize terrain
    // 1 : No overhangs/floating parts
    // 2 : More flat world
    // 3 : Less flat world
    // 4 : Big biomes
    // 5 : Less smoothing
    // 6 : No smoothing
    private SimplexOctaveGenerator[] gen;
    // 0 : Basic surface
    // 1 : Still Basic surface
    // 2 : Stone
    // 3 : Extended terrain height
    // 4 : Extended terrain
    // 5 : Whatever height
    // 6 : A Randomizer
    
    public GeneratorBase(long seed, String id)
    {
        blocks = new byte[16][4096];
        genID = (id == null) ? "" : id;
        bio = new BiomeHandler(seed, isSet("4"));
        gen = new SimplexOctaveGenerator[7];
        gen[0] = new SimplexOctaveGenerator(seed, 8);
        gen[0].setScale(1.0/2048.0);
        gen[1] = new SimplexOctaveGenerator(seed, 8);
        gen[1].setScale(1.0/512.0);
        gen[2] = new SimplexOctaveGenerator(seed, 8);
        gen[2].setScale(1.0/192.0);
        gen[3] = new SimplexOctaveGenerator(seed, 8);
        gen[3].setScale(1.0/(isSet("2") ? 1024.0 : 48.0));
        gen[4] = new SimplexOctaveGenerator(seed, 8);
        gen[4].setScale(1.0/384.0);
        gen[5] = new SimplexOctaveGenerator(seed, 8);
        gen[5].setScale(1.0/64.0);
        gen[6] = new SimplexOctaveGenerator(seed, 8);
        gen[6].setScale(1.0/123.0);
    }
    
    public static byte[][] gen(World world, int chunkX, int chunkZ, String id, BiomeGrid biomes)
    {
        return (new GeneratorBase(world.getSeed(), id)).generate(chunkX, chunkZ, biomes);
    }
    
    public boolean isSet(String ch)
    {
        return genID.contains(ch);
    }
    
    public int get(int x, int y, int z)
    {
        if((x < 0) || (x >= 16) || (z < 0) || (z >= 16) || (y < 0) || (y >= 256))
        {
            return 0;
        }
        return blocks[y >> 4][((y & 0xF) << 8) | (z << 4) | x];
    }
    
    public static void set(byte[][][] field, int x, int y, int z, int id)
    {
        if(x < 0) { x = 0; }
        if(x > 15) { x = 15; }
        if(y < 0) { y = 0; }
        if(y > 255) { y = 255; }
        if(z < 0) { z = 0; }
        if(z > 15) { z = 15; }
        field[x][z][y] = (byte)id;
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
    
    public int getYMax(int x, int z)
    {
        for(int y = 255; y >= 0; y--)
        {
            if(get(x, y, z) != 0)
            {
                return y;
            }
        }
        return 0;
    }
    
    public byte[][] generate(int chunkX, int chunkZ, BiomeGrid biomes)
    {
        byte[][][] field = isSet("6") ? genField(chunkX, chunkZ) : generateField(chunkX, chunkZ);
        int x, y, z, s;
        for(x = 0; x < 16; x++)
        {
            for(z = 0; z < 16; z++)
            {
                for(y = 255; y >= 0; y--)
                {
                    if(field[x][z][y] != 0)
                    {
                        break;
                    }
                }
                Biome b = bio.get(chunkX * 16 + x, y, chunkZ * 16 + z);
                biomes.setBiome(x, z, b);
                int[] types = bio.getBlocks(chunkX * 16 + x, chunkZ * 16 + z, b);
                for(y = 0; y < 256; y++)
                {
                    s = 0;
                    switch(field[x][z][y])
                    {
                        case 1:
                            s = 7; // Bedrock
                            break;
                        case 2:
                            s = 1; // Stone
                            break;
                        case 3:
                            s = types[0];
                            break;
                        case 4:
                            s = types[1];
                            break;
                        case 5:
                            s = types[2];
                            break;
                    }
                    if(s == 0)
                    {
                        if(y < 64)
                        {
                            s = 9;
                        }
                        else if(y == 64)
                        {
                            s = types[3];
                        }
                    }
                    if((s == 2) && (y <= 64))
                    {
                        s = 3;
                    }
                    set(x, y, z, s);
                }
            }
        }
        return blocks;
    }
    
    private byte[][][] generateField(int chunkX, int chunkZ)
    {
        int radius = isSet("5") ? 4 : 8;
        
        byte[][][] aa = genField(chunkX - 1, chunkZ - 1, 16 - radius, 16, 16 - radius, 16);
        byte[][][] ab = genField(chunkX - 1, chunkZ, 16 - radius, 16, 0, 16);
        byte[][][] ac = genField(chunkX - 1, chunkZ + 1, 16 - radius, 16, 0, radius);
        
        byte[][][] ba = genField(chunkX, chunkZ - 1, 0, 16, 16 - radius, 16);
        byte[][][] bb = genField(chunkX, chunkZ);
        byte[][][] bc = genField(chunkX, chunkZ + 1, 0, 16, 0, radius);
        
        byte[][][] ca = genField(chunkX + 1, chunkZ - 1, 0, radius, 16 - radius, 16);
        byte[][][] cb = genField(chunkX + 1, chunkZ, 0, radius, 0, 16);
        byte[][][] cc = genField(chunkX + 1, chunkZ + 1, 0, radius, 0, radius);
        
        double height, factor;
        // Amount of blocks
        int n = (int)Math.pow((radius * 2) + 1, 2) - 1;
        int x, y, z, id;
        byte[][][] field = new byte[16][16][256];
        for(x = 0; x < 16; x++)
        {
            for(z = 0; z < 16; z++)
            {
                height = getAverageHeight(x, z, radius, aa, ab, ac, ba, bb, bc, ca, cb, cc);
                for(y = 255; y >= 0; y--)
                {
                    if(getBlockFromCoords(bb, x, y, z) != 0)
                    {
                        break;
                    }
                }
                factor = (double)y + ((height - (double)y) * (n - 1) / n);
                factor /= (double)y;
                for(y = 0; y < 256; y++)
                {
                    id = getBlockFromCoords(bb, x, (int)Math.round((double)y * factor), z);
                    // Overlay check
                    if((id == 5) && (getBlockFromCoords(bb, x, (int)Math.round((double)(y + 1D) * factor), z) != 0))
                    {
                        id = 4;
                    }
                    field[x][z][y] = (byte)id;
                }
            }
        }
        return field;
    }
    
    private double getAverageHeight(int x1, int z1, int rad, byte[][][]... oldfield)
    {
        double height = 0.0D;
        if(oldfield.length == 9)
        {
            x1 += 16;
            z1 += 16;
            byte[][][] field = new byte[48][48][256];
            int a, b, x, y, z;
            for(a = 0; a < 3; a++)
            {
                for(b = 0; b < 3; b++)
                {
                    for(x = 0; x < 16; x++)
                    {
                        for(z = 0; z < 16; z++)
                        {
                            for(y = 0; y < 256; y++)
                            {
                                field[(a * 3 * 16) + x][(b * 16) + z][y] = oldfield[(a * 3) + b][x][z][y];
                            }
                        }
                    }
                }
            }
            a = x1 - rad;
            b = z1 - rad;
            for(x = a < 0 ? 0 : a; (x < 48) && (x <= x1 + rad); x++)
            {
                for(z = b < 0 ? 0 : b; (z < 48) && (z <= z1 + rad); z++)
                {
                    if((x == x1) && (z == z1))
                    {
                        continue;
                    }
                    for(y = 255; y >= 0; y--)
                    {
                        if(getBlockFromCoords(field, x, y, z) != 0)
                        {
                            height += (double)y;
                            break;
                        }
                    }
                }
            }
            /*int n, x, y, z, x3, z3;
             int x2 = x1;
             int z2 = z1;
             for(n = 0; n < 9; n++)
             {
             switch(n)
             {
             case 0:
             case 1:
             case 2:
             x2 = x1 + 16;
             break;
             case 3:
             case 4:
             case 5:
             x2 = x1;
             break;
             case 6:
             case 7:
             case 8:
             x2 = x1 - 16;
             break;
             }
             switch(n)
             {
             case 0:
             case 3:
             case 6:
             z2 = z1 + 16;
             break;
             case 1:
             case 4:
             case 7:
             z2 = z1;
             break;
             case 2:
             case 5:
             case 8:
             z2 = z1 - 16;
             break;
             }
             x3 = (x2 - rad) < 0 ? 0 : (x2 - rad);
             z3 = (z2 - rad) < 0 ? 0 : (z2 - rad);
             for(x = x3; (x < 16) && (x <= x2 + rad); x++)
             {
             for(z = z3; (z < 16) && (z <= z2 + rad); z++)
             {
             // Center block
             if((x2 == x1) && (z2 == z1) && (x == x1) && (z == z1))
             {
             continue;
             }
             for(y = 255; y >= 0; y--)
             {
             if(getBlockFromCoords(field[n], x, y, z) != 0)
             {
             height += (double)y;
             break;
             }
             }
             }
             }
             }*/
            height /= ((double)Math.pow(((double)rad * 2D) + 1D, 2D) - 1D);
        }
        return height;
    }
    
    private byte[][][] genField(int chunkX, int chunkZ)
    {
        return genField(chunkX, chunkZ, 0, 16, 0, 16);
    }
    
    private byte[][][] genField(int chunkX, int chunkZ, int x1, int x2, int z1, int z2)
    {
        int x, y, z;
        byte[][][] field = new byte[16][16][256];
        // 0: Air
        // 1: Bedrock
        // 2: Stone
        // 3: Biome block
        // 4: Second biome block
        // 5: Overlaying block
        int[] levels = new int[4];
        int[] tmp = new int[5];
        double factor;
        // Need to generate?
        boolean g = false;
        for(x = x1; x < x2; x++)
        {
            for(z = z1; z < z2; z++)
            {
                // Getting heights
                tmp[0] = (int)Math.round(gen[0].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-50)) + 75;
                tmp[1] = (int)Math.round(gen[1].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-50)) + 70;
                levels[1] = Math.max(tmp[0], tmp[1]);
                levels[0] = (int)Math.round(((double)levels[1] / 2.0) + (gen[2].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * ((double)levels[1] / (-3.0))));
                for(y = 1; y < levels[0]; y++)
                {
                    set(field, x, y, z, 2);
                }
                levels[2] = (int)Math.round(gen[3].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-110)) + 120;
                factor = isSet("3") ? 1D : (isSet("2") ? 0.3D : (gen[6].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) / 3.0) + 0.5);
                levels[2] = (int)Math.round((double)levels[2] * factor);
                if(levels[2] > 255)
                {
                    levels[2] = 255;
                }
                if(isSet("1"))
                {
                    tmp[1] = levels[2] > levels[1] ? levels[2] : levels[1];
                    if(isSet("0"))
                    {
                        tmp[1] += (int)Math.round(gen[5].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-5));
                    }
                    tmp[0] = (int)Math.floor((double)tmp[1] * 0.9D);
                    for(y = levels[0]; y < tmp[0]; y++)
                    {
                        set(field, x, y, z, 3);
                    }
                    for(y = tmp[0]; y < tmp[1]; y++)
                    {
                        set(field, x, y, z, 4);
                    }
                    set(field, x, tmp[1], z, 5);
                }
                else
                {
                    g = true;
                    tmp[0] = levels[0];
                    tmp[1] = y = levels[1];
                    levels[2] = levels[2] > levels[1] ? levels[2] : levels[1];
                    while(y <= levels[2])
                    {
                        while((gen[4].noise(chunkX * 16 + x, tmp[1] + 1, chunkZ * 16 + z, 0.5, 0.5) >= 0D) && (tmp[1] < levels[2]))
                        {
                            tmp[1]++;
                        }
                        tmp[4] = tmp[1];
                        if(isSet("0"))
                        {
                            tmp[1] += (int)Math.round(gen[5].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-5));
                        }
                        tmp[2] = (int)Math.floor((double)tmp[1] * 0.8D);
                        for(tmp[3] = tmp[0]; tmp[3] < tmp[2]; tmp[3]++)
                        {
                            set(field, x, tmp[3], z, 3);
                        }
                        for(tmp[3] = tmp[2]; tmp[3] < tmp[1]; tmp[3]++)
                        {
                            set(field, x, tmp[3], z, 4);
                        }
                        set(field, x, tmp[1], z, 5);
                        tmp[1] = tmp[4] + 1;
                        while((gen[3].noise(chunkX * 16 + x, tmp[1], chunkZ * 16 + z, 0.5, 0.5) < 0D) && (tmp[1] <= levels[2]))
                        {
                            tmp[1]++;
                        }
                        y = tmp[0] = tmp[1];
                    }
                }
                // You shall not pass
                field[x][z][0] = 1;
            }
        }
        return field;
    }
    
    private int getBlockFromCoords(byte[][][] field, int x, int y, int z)
    {
        try
        {
            if((x < 0) || (y < 0) || (z < 0) || (x >= field.length) || (z >= field[x].length) || (y >= field[x][z].length))
            {
                return 0;
            }
            else
            {
                return field[x][z][y];
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }
}