package com.stayplugged.dailyca;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class DailyCaPluginTestLauncher
{
	private DailyCaPluginTestLauncher()
	{
	}

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DailyCaPlugin.class);
		RuneLite.main(args);
	}
}
