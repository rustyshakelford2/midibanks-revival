package net.myshelter.minecraft.midibanks;

import net.myshelter.minecraft.IAttributeProvider;

import org.bukkit.entity.Player;

public class MidiBanksAttributeProvider implements IAttributeProvider {
	MidiBanks plugin;
	String namespace;

	public MidiBanksAttributeProvider(MidiBanks plugin, String namespace) {
		/* 13 */this.plugin = plugin;
		/* 14 */if (!namespace.equals(""))
			namespace = namespace + ".";
		/* 15 */this.namespace = namespace;
	}

	public boolean getFlag(Player p, String name, boolean def) {
		/* 19 */return this.plugin.getConfig().getBoolean(
				this.namespace + name, def);
	}

	public int getInt(Player p, String name, int def) {
		/* 23 */return this.plugin.getConfig().getInt(this.namespace + name,
				def);
	}

	public double getDouble(Player p, String name, double def) {
		/* 27 */return this.plugin.getConfig().getDouble(this.namespace + name,
				def);
	}

	public String getString(Player p, String name, String def) {
		String s;
		/* 32 */if ((s = this.plugin.getConfig().getString(
				this.namespace + name)) != null) {
			/* 33 */return s;
		}
		/* 35 */return def;
	}

	public IAttributeProvider getNamespace(String name) {
		/* 39 */return new MidiBanksAttributeProvider(this.plugin,
				this.namespace + name);
	}
}

/*
 * Location: C:\Users\jfmh\Downloads\midibankstest2.jar Qualified Name:
 * net.myshelter.minecraft.midibanks.MidiBanksAttributeProvider JD-Core Version:
 * 0.6.0
 */