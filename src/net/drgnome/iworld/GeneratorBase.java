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
    private SimplexOctaveGenerator[] gen;
    // 0 : Basic surface
    // 1 : Stone
    // 2 : Extended terrain height
    // 3 : Extended terrain
    // 4 : Whatever height
    // 5 : A Randomizer
    
    public GeneratorBase(long seed, String id)
    {
        blocks = new byte[16][4096];
        genID = (id == null) ? "" : id;
        bio = new BiomeHandler(seed, isSet("4"));
        gen = new SimplexOctaveGenerator[6];
        gen[0] = new SimplexOctaveGenerator(seed, 8);
        gen[0].setScale(1.0/512.0);
        gen[1] = new SimplexOctaveGenerator(seed, 8);
        gen[1].setScale(1.0/192.0);
        gen[2] = new SimplexOctaveGenerator(seed, 8);
        gen[2].setScale(1.0/(isSet("2") ? 1024.0 : 48.0));
        gen[3] = new SimplexOctaveGenerator(seed, 8);
        gen[3].setScale(1.0/384.0);
        gen[4] = new SimplexOctaveGenerator(seed, 8);
        gen[4].setScale(1.0/64.0);
        gen[5] = new SimplexOctaveGenerator(seed, 8);
        gen[5].setScale(1.0/123.0);
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
        byte[][][] field = genField(chunkX, chunkZ); // TODO
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
        byte[][][] aa = genField(chunkX - 1, chunkZ - 1, 8, 16, 8, 16);
        byte[][][] ab = genField(chunkX - 1, chunkZ, 8, 16, 0, 16);
        byte[][][] ac = genField(chunkX - 1, chunkZ + 1, 8, 16, 0, 8);
        
        byte[][][] ba = genField(chunkX, chunkZ - 1, 0, 16, 8, 16);
        byte[][][] bb = genField(chunkX, chunkZ);
        byte[][][] bc = genField(chunkX, chunkZ + 1, 0, 16, 0, 8);
        
        byte[][][] ca = genField(chunkX + 1, chunkZ - 1, 0, 8, 8, 16);
        byte[][][] cb = genField(chunkX + 1, chunkZ, 0, 8, 0, 16);
        byte[][][] cc = genField(chunkX + 1, chunkZ + 1, 0, 8, 0, 8);
        
        double height0 = getLeDurchschnitt(bb, 0, 16, 0, 16);
        double height = 0D;
        
        height += getValid(height0, getLeDurchschnitt(aa, 8, 16, 8, 16), 32D);
        height += getValid(height0, getLeDurchschnitt(ab, 8, 16, 0, 16), 32D);
        height += getValid(height0, getLeDurchschnitt(ac, 8, 16, 0, 8), 32D);
        
        height += getValid(height0, getLeDurchschnitt(ba, 0, 16, 8, 16), 32D);
        height += height0;
        height += getValid(height0, getLeDurchschnitt(bc, 0, 16, 0, 8), 32D);
        
        height += getValid(height0, getLeDurchschnitt(ca, 0, 8, 8, 16), 32D);
        height += getValid(height0, getLeDurchschnitt(cb, 0, 8, 0, 16), 32D);
        height += getValid(height0, getLeDurchschnitt(cc, 0, 8, 0, 8), 32D);
        
        height /= 1024;
        
        int x, y, z, h;
        double factor;
        byte[][][] field = new byte[16][16][256];
        for(x = 0; x < 16; x++)
        {
            for(z = 0; z < 16; z++)
            {
                h = 0;
                for(y = 255; y >= 0; y--)
                {
                    if(bb[x][z][y] != 0)
                    {
                        h = y;
                        break;
                    }
                }
                factor = ((height + (double)h) / 2D) / height;
                for(y = 0; y < 256; y++)
                {
                    h = (int)Math.round((double)y / factor);
                    field[x][z][y] = h > 255 ? 0 : bb[x][z][h];
                }
            }
        }
        return field;
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
        // + Debugging stuff
        int[] levels = new int[4];
        int[] tmp = new int[4];
        double factor;
        // Need to generate?
        boolean g = false;
        for(x = x1; x < x2; x++)
        {
            for(z = z1; z < z2; z++)
            {
                // Getting heights
                levels[1] = (int)Math.round(gen[0].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-50)) + 88;
                levels[0] = (int)Math.round(((double)levels[1] / 2.0) + (gen[1].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * ((double)levels[1] / (-3.0))));
                for(y = 1; y < levels[0]; y++)
                {
                    field[x][z][y] = 2;
                }
                levels[2] = (int)Math.round(gen[2].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-110)) + 120;
                factor = isSet("3") ? 1D : (isSet("2") ? 0.3D : (gen[5].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) / 3.0) + 0.5);
                levels[2] = (int)Math.round((double)levels[2] * factor);
                if(isSet("1"))
                {
                    tmp[1] = levels[2] > levels[1] ? levels[2] : levels[1];
                    if(isSet("0"))
                    {
                        tmp[1] += (int)Math.round(gen[4].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-10));
                    }
                    tmp[0] = (int)Math.floor((double)tmp[1] * 0.9D);
                    for(y = levels[0]; y < tmp[0]; y++)
                    {
                        field[x][z][y] = 3;
                    }
                    for(y = tmp[0]; y < tmp[1]; y++)
                    {
                        field[x][z][y] = 4;
                    }
                    field[x][z][tmp[1]] = 5;
                }
                else
                {
                    g = true;
                    tmp[0] = levels[0];
                    tmp[1] = y = levels[1];
                    levels[2] = levels[2] > levels[1] ? levels[2] : levels[1];
                    while(y <= levels[2])
                    {
                        while((gen[3].noise(chunkX * 16 + x, tmp[1] + 1, chunkZ * 16 + z, 0.5, 0.5) >= 0D) && (tmp[1] < levels[2]))
                        {
                            tmp[1]++;
                        }
                        tmp[2] = (int)Math.floor((double)tmp[1] * 0.8D);
                        for(tmp[3] = tmp[0]; tmp[3] < tmp[2]; tmp[3]++)
                        {
                            field[x][z][tmp[3]] = 3;
                        }
                        for(tmp[3] = tmp[2]; tmp[3] < tmp[1]; tmp[3]++)
                        {
                            field[x][z][tmp[3]] = 4;
                        }
                        field[x][z][tmp[1]] = 5;
                        tmp[1]++;
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
    
    private double getLeDurchschnitt(byte[][][] field, int x1, int x2, int z1, int z2)
    {
        int x, y, z;
        double h1 = 0;
        double h2 = 0;
        for(x = x1; x < x2; x++)
        {
            for(z = z1; z < z2; z++)
            {
                for(y = 255; y >= 0; y--)
                {
                    if(field[x][z][y] != 0)
                    {
                        h1 = (double)y;
                        break;
                    }
                }
                h2 += h1;
            }
        }
        return h2;
    }
    
    private double getValid(double correct, double check, double space)
    {
        if(Math.abs(correct - check) > Math.abs(space))
        {
            return correct;
        }
        return check;
    }
}