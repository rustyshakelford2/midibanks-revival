package net.myshelter.minecraft;

import org.bukkit.entity.Player;

public abstract interface IAttributeProvider
{
  public abstract IAttributeProvider getNamespace(String paramString);

  public abstract boolean getFlag(Player paramPlayer, String paramString, boolean paramBoolean);

  public abstract int getInt(Player paramPlayer, String paramString, int paramInt);

  public abstract double getDouble(Player paramPlayer, String paramString, double paramDouble);

  public abstract String getString(Player paramPlayer, String paramString1, String paramString2);
}