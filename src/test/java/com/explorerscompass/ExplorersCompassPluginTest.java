package com.explorerscompass;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExplorersCompassPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ExplorersCompassPlugin.class);
		RuneLite.main(args);
	}
}
