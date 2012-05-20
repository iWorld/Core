// Bukkit Plugin "iWorld" by Siguza and steffengy
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.iworld;

public class Util
{
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
    
    public static int max(int... check)
    {
        if(check.length == 0)
        {
            return 0;
        }
        int v = check[0];
        for(int i = 1; i < check.length; i++)
        {
            if(check[i] > v)
            {
                v = check[i];
            }
        }
        return v;
    }
}