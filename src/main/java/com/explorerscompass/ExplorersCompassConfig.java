package com.explorerscompass;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(ExplorersCompassConfig.GROUP)
public interface ExplorersCompassConfig extends Config
{
	String GROUP = "explorerscompass";

	// ── Section: Display ─────────────────────────────────────────

	@ConfigSection(
		name = "Display",
		description = "How the nearest shortcut is shown",
		position = 0
	)
	String displaySection = "display";

	@ConfigItem(
		keyName = "showHintArrow",
		name = "Show hint arrow",
		description = "Points the game's hint arrow at the nearest undiscovered shortcut you can use",
		position = 1,
		section = displaySection
	)
	default boolean showHintArrow()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showInfoOverlay",
		name = "Show info overlay",
		description = "Shows the target shortcut's name, level and distance on screen",
		position = 2,
		section = displaySection
	)
	default boolean showInfoOverlay()
	{
		return true;
	}

	// ── Section: Behaviour ───────────────────────────────────────

	@ConfigSection(
		name = "Behaviour",
		description = "Discovery and targeting rules",
		position = 10
	)
	String behaviourSection = "behaviour";

	@Range(min = 1, max = 15)
	@ConfigItem(
		keyName = "discoveryRadius",
		name = "Discovery radius",
		description = "Walk within this many tiles of a shortcut to mark it discovered",
		position = 11,
		section = behaviourSection
	)
	default int discoveryRadius()
	{
		return 3;
	}

	@Range(min = 0, max = 7000)
	@ConfigItem(
		keyName = "maxDistance",
		name = "Max distance",
		description = "Ignore shortcuts farther than this many tiles away. Set to 0 for no limit.",
		position = 12,
		section = behaviourSection
	)
	default int maxDistance()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "levelBuffer",
		name = "Level buffer",
		description = "Only target shortcuts whose requirement is at least this far below your level."
			+ " 0 means target anything you can use right now.",
		position = 13,
		section = behaviourSection
	)
	@Range(min = 0, max = 99)
	default int levelBuffer()
	{
		return 0;
	}

	// ── Section: Data ────────────────────────────────────────────

	@ConfigSection(
		name = "Data",
		description = "Discovery progress",
		position = 20
	)
	String dataSection = "data";

	@ConfigItem(
		keyName = "resetDiscovered",
		name = "Reset discovered",
		description = "Tick this to wipe all discovery progress. It untoggles itself afterwards.",
		position = 21,
		section = dataSection
	)
	default boolean resetDiscovered()
	{
		return false;
	}
}
