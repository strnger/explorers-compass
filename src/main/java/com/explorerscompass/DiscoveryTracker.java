package com.explorerscompass;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

/**
 * Tracks which points of interest the player has already discovered and
 * persists that set across sessions via {@link ConfigManager}.
 * <p>
 * Discovery is stored as a comma-separated list of POI keys under a hidden
 * config key (it's not a {@code @ConfigItem}, so it never clutters the settings
 * panel). The in-memory {@link Set} is the source of truth during a session;
 * every mutation writes through to config immediately so a crash can't lose
 * more than the current tick's progress.
 */
@Slf4j
public class DiscoveryTracker
{
	static final String DISCOVERED_KEY = "discoveredPois";

	private final ConfigManager configManager;
	private final Set<String> discovered = new LinkedHashSet<>();

	public DiscoveryTracker(ConfigManager configManager)
	{
		this.configManager = configManager;
		load();
	}

	/** @return {@code true} if the given POI has already been discovered. */
	public boolean isDiscovered(PointOfInterest poi)
	{
		return discovered.contains(poi.getKey());
	}

	/**
	 * Marks a POI discovered and persists immediately.
	 *
	 * @return {@code true} if this was a newly discovered POI, {@code false} if
	 *         it was already known (in which case nothing is written).
	 */
	public boolean markDiscovered(PointOfInterest poi)
	{
		if (discovered.add(poi.getKey()))
		{
			save();
			log.debug("Explorer's Compass: discovered {}", poi.getName());
			return true;
		}
		return false;
	}

	/** @return how many POIs have been discovered so far. */
	public int discoveredCount()
	{
		return discovered.size();
	}

	/** Wipes all discovery progress and clears it from config. */
	public void reset()
	{
		discovered.clear();
		configManager.unsetConfiguration(ExplorersCompassConfig.GROUP, DISCOVERED_KEY);
		log.debug("Explorer's Compass: discovery data reset");
	}

	private void load()
	{
		String stored = configManager.getConfiguration(ExplorersCompassConfig.GROUP, DISCOVERED_KEY);
		if (stored == null || stored.isEmpty())
		{
			return;
		}

		discovered.addAll(Arrays.stream(stored.split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.collect(Collectors.toList()));
	}

	private void save()
	{
		configManager.setConfiguration(ExplorersCompassConfig.GROUP, DISCOVERED_KEY,
			String.join(",", discovered));
	}

	/** Test/introspection helper: an unmodifiable view of the discovered keys. */
	Set<String> view()
	{
		return Collections.unmodifiableSet(discovered);
	}
}
