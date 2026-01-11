package net.minecraftforge.client;

import net.minecraft.src.EnumOptions;
import net.minecraft.src.EnumOS;
import net.minecraft.src.EnumGameType;
import net.minecraftforge.common.util.EnumHelper;
public class EnumHelperClient extends EnumHelper
{
    @SuppressWarnings("rawtypes")
    private static Class[][] clentTypes =
    {
        {EnumGameType.class, int.class, String.class},
        {EnumOptions.class, String.class, boolean.class, boolean.class},
        {EnumOS.class}
    };
    
    public static EnumGameType addGameType(String name, int id, String displayName)
    {
        return addEnum(EnumGameType.class, name, id, displayName);
    }
    
    public static EnumOptions addOptions(String name, String langName, boolean isSlider, boolean isToggle)
    {
        return addEnum(EnumOptions.class, name, langName, isSlider, isToggle);
    }
    
    public static EnumOS addOS2(String name)
    {
        return addEnum(EnumOS.class, name);
    }

    public static <T extends Enum<? >> T addEnum(Class<T> enumType, String enumName, Object... paramValues)
    {
        return addEnum(clentTypes, enumType, enumName, paramValues);
    }
}