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
                    if((field[x][z][y] == 4) && ((y == 255) || (field[x][z][y + 1] == 0) || (field[x][z][y + 1] == 1)))
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
                        case 0:
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
                            break;
                        case 1:
                            s = 0; // Unoverridable air
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
                        case 7:
                            s = 7; // Bedrock stays bedrock
                            break;
                    }
                    if(y == 0)
                    {
                        // You shall not pass!
                        s = 7;
                    }
                    else if((s == 2) && (y < getSeaLevel()))
                    {
                        s = 3;
                    }
                    if((!cover) && (s != 0))
                    {
                        cover = true;
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
                            field[x][z][y] = 7;
                        }
                        else
                        {
                            field[x][z][y] = 7;
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
                        field[x * 4][(z * 4) + 2][1] = 7;
                        field[x * 4][(z * 4) + 2][2] = 7;
                    }
                    if(rand.nextInt(2) == 0)
                    {
                        field[(x * 4) + 2][z * 4][1] = 7;
                        field[(x * 4) + 2][z * 4][2] = 7;
                    }
                    if(rand.nextInt(2) == 0)
                    {
                        field[(x * 4) + 2][(z * 4) + 2][4] = 7;
                    }
                }
            }
        }
    }
    
    private void modifyBlock(int chunkX, int chunkZ, int x, int z, byte[][][] field)
    {
        int y, y0, y1;
        // Grass, dirt, ...
        for(y = 255; y > 11; y--)
        {
            if((field[x][z][y] == 0) && (field[x][z][y - 1] != 0))
            {
                y0 = (int)Math.round((gen[2].noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5, 0.5) * (-2)) + 3);
                if(y0 < 1)
                {
                    y0 = 1;
                }
                if(y0 > 5)
                {
                    y0 = 5;
                }
                if(field[x][z][y - 1] == 2)
                {
                    field[x][z][y - 1] = 5;
                }
                for(y1 = y - 2; y1 >= y - (y0 + 1); y1--)
                {
                    if(field[x][z][y1] == 2)
                    {
                        field[x][z][y1] = 4;
                    }
                }
                for(y1 = y - (y0 + 1); y1 >= y - ((2 * y0) + 1); y1--)
                {
                    if(field[x][z][y1] == 2)
                    {
                        field[x][z][y1] = 3;
                    }
                }
            }
        }
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
                    while((max >= y - 3) && (max >= getSeaLevel() + 3))
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
        byte[][][][] fields = new byte[9][][][];
        fields[0] = genField(chunkX - 1, chunkZ - 1, 16 - radius, 16, 16 - radius, 16);
        fields[1] = genField(chunkX - 1, chunkZ, 16 - radius, 16, 0, 16);
        fields[2] = genField(chunkX - 1, chunkZ + 1, 16 - radius, 16, 0, radius);
        fields[3] = genField(chunkX, chunkZ - 1, 0, 16, 16 - radius, 16);
        fields[4] = genField(chunkX, chunkZ);
        fields[5] = genField(chunkX, chunkZ + 1, 0, 16, 0, radius);
        fields[6] = genField(chunkX + 1, chunkZ - 1, 0, radius, 16 - radius, 16);
        fields[7] = genField(chunkX + 1, chunkZ, 0, radius, 0, 16);
        fields[8] = genField(chunkX + 1, chunkZ + 1, 0, radius, 0, radius);
        byte[][][] bigfield = new byte[16 + (2 * radius)][16 + (2 * radius)][256];
        int id, a, b, x, y, z;
        for(x = 0; x < bigfield.length; x++)
        {
            for(z = 0; z < bigfield[x].length; z++)
            {
                id = (x < radius ? 0 : (x < radius + 16 ? 1 : 2)) + (z < radius ? 0 : (z < radius + 16 ? 3 : 6));
                a = x < radius ? 0 : (x < radius + 16 ? radius : (radius + 16));
                b = z < radius ? 0 : (z < radius + 16 ? radius : (radius + 16));
                for(y = 0; y < 256; y++)
                {
                    bigfield[x][z][y] = fields[id][x - a][z - b][y];
                }
            }
        }
        boolean[][][][] snap = new boolean[(2 * radius) + 16][(2 * radius) + 16][(2 * radius) + 1][(2 * radius) + 1];
        for(a = 0; a < snap.length; a++)
        {
            for(b = 0; b < snap[a].length; b++)
            {
                for(id = 255; id >= 0; id--)
                {
                    if(bigfield[a][b][id] != 0)
                    {
                        break;
                    }
                }
                for(x = 0; x < snap[a][b].length; x++)
                {
                    for(z = 0; z < snap[a][b][x].length; z++)
                    {
                        if(((x == radius) && (z == radius)) || (a + x - radius < 0) ||Ê(a + x - radius > ))
                        {
                            continue;
                        }
                        for(y = 255; y >= 0; y--)
                        {
                            if(bigfield[][][][])
                        }
                    }
                }
            }
        }
        /*for(a = 0; a < 3; a++)
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
        }*/
        // Amount of blocks
        double n = Math.pow(((double)radius * 2D) + 1D, 2D) - 1D;
        double height, factor;
        byte[][][] field = new byte[16][16][256];
        for(x = 0; x < 16; x++)
        {
            for(z = 0; z < 16; z++)
            {
                for(y = 255; y >= 0; y--)
                {
                    if(getBlockFromCoords(fields[4], x, y, z) != 0)
                    {
                        break;
                    }
                }
                height = getHeight(x, z, radius, y, n, bigfield, snap);
                factor = (double)y + ((height - (double)y) * (n - 1D) / n);
                factor /= (double)y;
                y = (int)Math.round((double)y * factor);
                if(y > 255)
                {
                    y = 255;
                }
                if(y < 0)
                {
                    y = 0;
                }
                for(; y >= 0; y--)
                {
                    id = getBlockFromCoords(fields[4], x, (int)Math.round((double)y / factor), z);
                    if((id == 0) || (id == 1))
                    {
                        factor = 1D;
                    }
                    field[x][z][y] = (byte)id;
                }
            }
        }
        return field;
    }
    
    private double getHeight(int x1, int z1, int rad, int mid, double amount, byte[][][] field, boolean[][][][] snap)
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
                    if((getBlockFromCoords(field, x, mid, z) != 0) && (getBlockFromCoords(field, x, mid, z) != 1))
                    {
                        for(y = mid; y < 256; y++)
                        {
                            if((getBlockFromCoords(field, x, y + 1, z) == 0) && (getBlockFromCoords(field, x, y + 1, z) != 1))
                            {
                                break;
                            }
                        }
                    }
                    else
                    {
                        for(y = mid; y >= 0; y--)
                        {
                            if((getBlockFromCoords(field, x, y, z) != 0) && (getBlockFromCoords(field, x, y, z) != 1))
                            {
                                break;
                            }
                        }
                    }
                    if(Math.abs(mid - y) <= 16)
                    {
                        height += (double)y;
                    }
                    else
                    {
                        amount--;
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return amount == 0D ? 0D : height/amount;
    }
    
    private byte[][][] genField(int chunkX, int chunkZ)
    {
        return genField(chunkX, chunkZ, 0, 16, 0, 16);
    }
    
    private byte[][][] genField(int chunkX, int chunkZ, int x1, int x2, int z1, int z2)
    {
        int x, y, z;
        byte[][][] field = new byte[x2 - x1][z2 - z1][256];
        // 0: Air
        // 1: Hard air
        // 2: Stone
        // 3: Biome block
        // 4: Second biome block
        // 5: Cover block
        int[] levels = new int[2];
        int[] tmp = new int[3];
        for(x = 0; x < (x2 - x1); x++)
        {
            for(z = 0; z < (z2 - z1); z++)
            {
                // Getting heights
                levels[0] = (int)Math.round(gen[0].noise(chunkX * 16 + x1 + x, chunkZ * 16 + z1 + z, 0.5, 0.5) * (-60)) + 65;
                tmp[0] = (int)Math.round(gen[1].noise(chunkX * 16 + x1 + x, chunkZ * 16 + z1 + z, 0.5, 0.5) * (-60)) + 70;
                levels[0] = max(levels[0], tmp[0], 10);
                levels[1] = (int)Math.round((Math.round(gen[3].noise(chunkX * 16 + x1 + x, chunkZ * 16 + z1 + z, 0.5, 0.5) * (-70)) + 180) * (isSet("3") ? 1D : (isSet("2") ? 0.3D : ((gen[6].noise(chunkX * 16 + x1 + x, chunkZ * 16 + z1 + z, 0.5, 0.5) / 2.2) + 0.5))));
                if(levels[1] > 255)
                {
                    levels[1] = 255;
                }
                levels[1] = max(levels[0], levels[1]);
                if(isSet("1"))
                {
                    if(isSet("0"))
                    {
                        levels[1] += (int)Math.round(gen[5].noise(chunkX * 16 + x1 + x, chunkZ * 16 + z1 + z, 0.5, 0.5) * (-5));
                    }
                    for(y = 1; y <= levels[1]; y++)
                    {
                        set(field, x, y, z, 2);
                    }
                }
                else
                {
                    tmp[0] = y = 1;
                    tmp[1] = levels[0];
                    while(y <= levels[1])
                    {
                        while((gen[4].noise(chunkX * 16 + x1 + x, tmp[1] + 1, chunkZ * 16 + z1 + z, 0.5, 0.5) >= 0D) && (tmp[1] < levels[1]))
                        {
                            tmp[1]++;
                        }
                        tmp[2] = tmp[1];
                        if(isSet("0"))
                        {
                            tmp[1] += (int)Math.round(gen[5].noise(chunkX * 16 + x1 + x, chunkZ * 16 + z1 + z, 0.5, 0.5) * (-5));
                        }
                        for(; tmp[0] <= tmp[1]; tmp[0]++)
                        {
                            set(field, x, tmp[0], z, 2);
                        }
                        tmp[1] = tmp[2] + 1;
                        while((gen[3].noise(chunkX * 16 + x1 + x, tmp[1], chunkZ * 16 + z1 + z, 0.5, 0.5) < 0D) && (tmp[1] <= levels[1]))
                        {
                            tmp[1]++;
                        }
                        y = tmp[0] = tmp[1];
                    }
                }
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