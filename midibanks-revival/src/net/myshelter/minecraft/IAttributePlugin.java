package net.myshelter.minecraft;

import org.bukkit.plugin.Plugin;

public abstract interface IAttributePlugin extends Plugin
{
  public abstract void setProvider(String paramString, IAttributeProvider paramIAttributeProvider);

  public abstract IAttributeProvider getProvider(String paramString);
}

/* Location:           C:\Users\jfmh\Downloads\midibankstest2.jar
 * Qualified Name:     net.myshelter.minecraft.IAttributePlugin
 * JD-Core Version:    0.6.0
 */