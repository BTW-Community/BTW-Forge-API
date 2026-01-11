package net.minecraftforge.client.model;

import cpw.mods.fml.relauncher.SideOnly;

import static cpw.mods.fml.relauncher.Side.CLIENT;

public interface IModelCustom
{
    String getType();
    @SideOnly(CLIENT)
    void renderAll();
    @SideOnly(CLIENT)
    void renderOnly(String... groupNames);
    @SideOnly(CLIENT)
    void renderPart(String partName);
    @SideOnly(CLIENT)
    void renderAllExcept(String... excludedGroupNames);
}