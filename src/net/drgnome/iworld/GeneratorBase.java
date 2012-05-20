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
    private long seed;
    private byte[][] blocks;
    private BiomeHandler bio;
    private String genID;
    // 0 : Randomize terrain
    // 1 : No overhangs/floating parts
    // 2 : More flat world
    // 3 : Less flat world
    // 4 : Big biomes
    // 5 : Smoothing flag 1 | 5 => no smoothing   | 56 => even more smoothing
    // 6 : Smoothing flag 2 | 6 => more smoothing | 65 => max smoothing
    // 7 : Sea level at 128
    // a : Different snow heights
    // b : no water caves
    // c : Bedrock labyrinth
    private SimplexOctaveGenerator[] gen;
    // 0 : Basic surface
    // 1 : Still Basic surface
    // 2 : Stone
    // 3 : Extended terrain height
    // 4 : Extended terrain
    // 5 : Whatever height
    // 6 : A Randomizer
    // 7 : Water Cave generator
    // 8 : Water Cave limits
    
    public GeneratorBase(long seed, String id)
    {
        this.seed = seed;
        blocks = new byte[16][4096];
        genID = (id == null) ? "" : id;
        bio = new BiomeHandler(seed, isSet("4"), isSet("7"));
        gen = new SimplexOctaveGenerator[9];
        gen[0] = new SimplexOctaveGenerator(seed, 8);
        gen[0].setScale(1.0/2048.0);
        gen[1] = new SimplexOctaveGenerator(seed, 8);
        gen[1].setScale(1.0/512.0);
        gen[2] = new SimplexOctaveGenerator(seed, 8);
        gen[2].setScale(1.0/192.0);
        gen[3] = new SimplexOctaveGenerator(seed, 8);
        gen[3].setScale(1.0/(isSet("2") ? 1024.0 : 128.0));
        gen[4] = new SimplexOctaveGenerator(seed, 8);
        gen[4].setScale(1.0/384.0);
        gen[5] = new SimplexOctaveGenerator(seed, 8);
        gen[5].setScale(1.0/64.0);
        gen[6] = new SimplexOctaveGenerator(seed, 8);
        gen[6].setScale(1.0/234.0);
        gen[7] = new SimplexOctaveGenerator(seed, 8);
        gen[7].setScale(1.0/123.0);
        gen[8] = new SimplexOctaveGenerator(seed, 8);
        gen[8].setScale(1.0/24.0);
    }
    
    public static byte[][] gen(World world, int chunkX, int chunkZ, String id, BiomeGrid biomes)
    {
        return (new GeneratorBase(world.getSeed(), id)).generate(chunkX, chunkZ, biomes);
    }
    
    public boolean isSet(String ch)
    {
        return genID.contains(ch);
    }
    
    public int getSeaLevel()
    {
        return isSet("7") ? 128 : 64;
    }
    
    public int getSmoothRad()
    {
        if(isSet("65"))
        {
            return 16;
        }
        else if(isSet("56"))
        {
            return 12;
        }
        else if(isSet("6"))
        {
            return 8;
        }
        else if(isSet("5"))
        {
            return 0;
        }
        return 4;
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
        byte[][][] field = getSmoothRad() == 0 ? genField(chunkX, chunkZ) : generateField(chunkX, chunkZ);
        int x, y, z, s;
        for(x = 0; x < 16; x++)
        {
            for(z = 0; z < 16; z++)
            {
                for(y = 255; y >= 0; y--)
                {
                    if((field[x][z][y] == 4) && ((y == 255) || (field[x][z][y + 1] == 0) || (field[x][z][y + 1] == 42)))
                    {
                        field[x][z][y] = 5;
                    }
                }
            }
        }
        modifyField(chunkX, chunkZ, field);
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
                boolean cover = false;
                for(y = 255; y >= 0; y--)
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
                        case 42:
                            s = -1; // Unoverridable air
                            break;
                    }
                    if(s == 0)
                    {
                        if(y < getSeaLevel())
                        {
                            s = 9;
                        }
                        else if(y == getSeaLevel())
                        {
                            if(cover)
                            {
                                s = 9;
                            }
                            else
                            {
                                s = types[3];
                            }
                        }
                    }
                    if((s == 2) && (y <= getSeaLevel()))
                    {
                        s = 3;
                    }
                    if((!cover) && (s != 0))
                    {
                        cover = true;
                    }
                    if(s == -1)
                    {
                        s = 0;
                    }
                    set(x, y, z, s);
                }
            }
        }
        return blocks;
    }
    
    private void modifyField(int chunkX, int chunkZ, byte[][][] field)
    {
        Random rand = new Random((long)((int)seed ^ (chunkX | chunkZ)));
        rand.setSeed(rand.nextLong());
        int x, y, z;
        for(x = 0; x < 16; x++)
        {
            for(z = 0; z < 16; z++)
            {
                modifyBlock(chunkX, chunkZ, x, z, field);
            }
        }
        if(isSet("c"))
        {
            for(x = 0; x < 16; x++)
            {
                for(z = 0; z < 16; z++)
                {
                    for(y = 1; y <= 4; y++)
                    {
                        if((x % 4 == 0) || (y % 4 == 0) || (z % 4 == 0))
                        {
                            field[x][z][y] = 1;
                        }
                        else
                        {
                            field[x][z][y] = 42;
                        }
                    }
                }
            }
            for(x = 0; x < 4; x++)
            {
                for(z = 0; z < 4; z++)
                {
                    if(rand.nextInt(2) == 0)
                    {
                        field[x * 4][(z * 4) + 2][1] = 42;
                        field[x * 4][(z * 4) + 2][2] = 42;
                    }
                    if(rand.nextInt(2) == 0)
                    {
                        field[(x * 4) + 2][z * 4][1] = 42;
                        field[(x * 4) + 2][z * 4][2] = 42;
                    }
                    if(rand.nextInt(2) == 0)
                    {
                        field[(x * 4) + 2][(z * 4) + 2][4] = 42;
                    }
                }
            }
        }
    }
    
    private void modifyBlock(int chunkX, int chunkZ, int x, int z, byte[][][] field)
    {
        int y;
        // Water caves
        if(!isSet("b"))
        {
            for(y = 255; y >= 0; y--)
            {
                if(field[x][z][y] != 0)
                {
                    break;
                }
            }
            if(y > 32)
            {
                int max = isSet("7") ? 139 : 75;
                max += (int)Math.round(gen[8].noise((chunkX * 16) + x, (chunkZ * 16) + z, 0.5, 0.5) * (-7D));
                if(y >= getSeaLevel())
                {
                    while((max >= y - 3) && (max >= getSeaLevel() + (isSet("7") ? 8 : 4)))
                    {
                        max--;
                    }
                }
                max = y > max ? max : y;
                for(y = (56 + (int)Math.round(gen[8].noise((chunkX * 16) + x, (chunkZ * 16) + z, 0.5, 0.5) * (-8D))) * (isSet("7") ? 2 : 1); y <= max; y++)
                {
                    double d = gen[7].noise((chunkX * 16) + x, y, (chunkZ * 16) + z, 0.5, 0.5);
                    if((d > -0.1D) && (d < 0.1D))
                    {
                        field[x][z][y] = 0;
                    }
                }
            }
        }
    }
    
    private byte[][][] generateField(int chunkX, int chunkZ)
    {
        int radius = getSmoothRad();
        
        byte[][][][] fields = new byte[9][16][16][256];
        
        fields[0] = genField(chunkX - 1, chunkZ - 1, 16 - radius, 16, 16 - radius, 16);
        fields[1] = genField(chunkX - 1, chunkZ, 16 - radius, 16, 0, 16);
        fields[2] = genField(chunkX - 1, chunkZ + 1, 16 - radius, 16, 0, radius);
        
        fields[3] = genField(chunkX, chunkZ - 1, 0, 16, 16 - radius, 16);
        fields[4] = genField(chunkX, chunkZ);
        fields[5] = genField(chunkX, chunkZ + 1, 0, 16, 0, radius);
        
        fields[6] = genField(chunkX + 1, chunkZ - 1, 0, radius, 16 - radius, 16);
        fields[7] = genField(chunkX + 1, chunkZ, 0, radius, 0, 16);
        fields[8] = genField(chunkX + 1, chunkZ + 1, 0, radius, 0, radius);
        
        byte[][][] bigfield = new byte[48][48][256];
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
                            bigfield[(a * 16) + x][(b * 16) + z][y] = fields[(a * 3) + b][x][z][y];
                        }
                    }
                }
            }
        }
        
        // Amount of blocks
        double n = Math.pow(((double)radius * 2D) + 1D, 2D) - 1D;
        double height, factor;
        int id;
        byte[][][] field = new byte[16][16][256];
        for(x = 0; x < 16; x++)
        {
            for(z = 0; z < 16; z++)
            {
                height = getHeight(x, z, radius, bigfield) / n;
                for(y = 255; y >= 0; y--)
                {
                    if(getBlockFromCoords(fields[4], x, y, z) != 0)
                    {
                        break;
                    }
                }
                factor = (double)y + ((height - (double)y) * (n - 1D) / n);
                factor /= (double)y;
                for(y = 255; y >= 0; y--)
                {
                    field[x][z][y] = (byte)getBlockFromCoords(fields[4], x, (int)Math.round((double)y / factor), z);
                }
            }
        }
        return field;
    }
    
    private double getHeight(int x1, int z1, int rad, byte[][][] field)
    {
        double height = 0.0D;
        try
        {
            x1 += 16;
            z1 += 16;
            int x, y, z;
            int a = x1 - rad;
            int b = z1 - rad;
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
        }
        catch(Exception e)
        {
            e.printStackTrace();
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
        int[] levels = new int[4];
        int[] tmp = new int[5];
        double factor;
        for(x = x1; x < x2; x++)
        {
            for(z = z1; z < z2; z++)
            {
                // Getting heights
                tmp[0] = (int)Math.round(gen[0].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-60)) + 65;
                tmp[1] = (int)Math.round(gen[1].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-60)) + 70;
                levels[1] = max(tmp[0], tmp[1], 10);
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
                    for(y = tmp[0]; y <= tmp[1]; y++)
                    {
                        set(field, x, y, z, 4);
                    }
                }
                else
                {
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
                        for(tmp[3] = tmp[2]; tmp[3] <= tmp[1]; tmp[3]++)
                        {
                            set(field, x, tmp[3], z, 4);
                        }
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
    
    private static int getBlockFromCoords(byte[][][] field, int x, int y, int z)
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