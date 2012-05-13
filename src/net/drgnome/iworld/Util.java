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
}