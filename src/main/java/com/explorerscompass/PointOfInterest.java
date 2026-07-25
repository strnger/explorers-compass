package com.explorerscompass;

import lombok.Value;
import net.runelite.api.coords.WorldPoint;

/**
 * An immutable point of interest the player might want to explore — currently
 * an agility shortcut gated behind a skill level.
 * <p>
 * POIs are loaded once from a bundled TSV resource and never mutated. "Have you
 * discovered it yet" state lives separately in {@link DiscoveryTracker} so this
 * model stays a pure description of the world.
 */
@Value
public class PointOfInterest
{
	/** Human-readable label shown in the info overlay (e.g. "Yanille Wall Shortcut"). */
	String name;

	/** Where the shortcut lives in the world. */
	WorldPoint location;

	/** Minimum (real, unboosted) Agility level required to use the shortcut. */
	int requiredLevel;

	/**
	 * A stable identifier used to persist discovery state. Two POIs must never
	 * share a key; name + packed coordinates guarantees uniqueness even if two
	 * shortcuts happen to share a display name.
	 */
	public String getKey()
	{
		return name + "@" + location.getX() + "," + location.getY() + "," + location.getPlane();
	}
}
