package com.explorerscompass;

import java.util.List;
import java.util.function.Predicate;
import net.runelite.api.coords.WorldPoint;

/**
 * Pure selection logic: given where the player is and what they can do, pick the
 * nearest shortcut worth pointing at.
 * <p>
 * Depends only on a list of shortcuts and a "is this discovered?" predicate —
 * no RuneLite client state — so it can be unit-tested in isolation.
 */
public class CompassTargeting
{
	private final List<PointOfInterest> shortcuts;
	private final Predicate<PointOfInterest> isDiscovered;

	public CompassTargeting(List<PointOfInterest> shortcuts, Predicate<PointOfInterest> isDiscovered)
	{
		this.shortcuts = shortcuts;
		this.isDiscovered = isDiscovered;
	}

	/**
	 * Finds the nearest shortcut that is:
	 * <ul>
	 *   <li>usable at the player's current Agility level (respecting {@code levelBuffer}),</li>
	 *   <li>not yet discovered, and</li>
	 *   <li>within {@code maxDistance} tiles (or anywhere, when {@code maxDistance <= 0}).</li>
	 * </ul>
	 *
	 * @param from         player's current world location
	 * @param agilityLevel player's real (unboosted) Agility level
	 * @param levelBuffer  only consider shortcuts whose requirement is at least
	 *                     this far below {@code agilityLevel}
	 * @param maxDistance  distance cap in tiles, or {@code <= 0} for unlimited
	 * @return the nearest matching shortcut, or {@code null} if none qualify
	 */
	public PointOfInterest findTarget(WorldPoint from, int agilityLevel, int levelBuffer, int maxDistance)
	{
		if (from == null)
		{
			return null;
		}

		final long maxDistanceSq = maxDistance > 0 ? (long) maxDistance * maxDistance : Long.MAX_VALUE;

		PointOfInterest best = null;
		long bestDistanceSq = Long.MAX_VALUE;

		for (PointOfInterest poi : shortcuts)
		{
			if (agilityLevel < poi.getRequiredLevel())
			{
				continue;
			}

			if (agilityLevel - poi.getRequiredLevel() < levelBuffer)
			{
				continue;
			}

			if (isDiscovered.test(poi))
			{
				continue;
			}

			long distanceSq = distanceSquared2D(from, poi.getLocation());
			if (distanceSq > maxDistanceSq)
			{
				continue;
			}

			if (distanceSq < bestDistanceSq)
			{
				bestDistanceSq = distanceSq;
				best = poi;
			}
		}

		return best;
	}

	/**
	 * Squared 2D (plane-ignoring) distance between two world points. Squared to
	 * avoid needless {@code sqrt} calls while ranking — ordering is preserved.
	 */
	static long distanceSquared2D(WorldPoint a, WorldPoint b)
	{
		long dx = a.getX() - b.getX();
		long dy = a.getY() - b.getY();
		return dx * dx + dy * dy;
	}
}
