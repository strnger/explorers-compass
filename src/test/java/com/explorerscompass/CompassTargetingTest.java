package com.explorerscompass;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CompassTargetingTest
{
	private static PointOfInterest poi(String name, int x, int y, int level)
	{
		return new PointOfInterest(name, new WorldPoint(x, y, 0), level);
	}

	private final PointOfInterest near = poi("Near", 3000, 3000, 10);
	private final PointOfInterest far = poi("Far", 3200, 3200, 10);
	private final PointOfInterest highLevel = poi("HighLevel", 3001, 3000, 80);

	private final List<PointOfInterest> shortcuts = Arrays.asList(near, far, highLevel);

	@Test
	public void picksNearestEligibleUndiscovered()
	{
		CompassTargeting targeting = new CompassTargeting(shortcuts, p -> false);
		PointOfInterest target = targeting.findTarget(new WorldPoint(3000, 3000, 0), 50, 0, 0);
		assertEquals(near, target);
	}

	@Test
	public void skipsShortcutsAboveLevel()
	{
		// Only the high-level shortcut is closest, but level 50 can't use it.
		CompassTargeting targeting = new CompassTargeting(
			Arrays.asList(highLevel, far), p -> false);
		PointOfInterest target = targeting.findTarget(new WorldPoint(3000, 3000, 0), 50, 0, 0);
		assertEquals(far, target);
	}

	@Test
	public void skipsDiscoveredShortcuts()
	{
		Set<String> discovered = new HashSet<>();
		discovered.add(near.getKey());
		CompassTargeting targeting = new CompassTargeting(shortcuts, p -> discovered.contains(p.getKey()));
		PointOfInterest target = targeting.findTarget(new WorldPoint(3000, 3000, 0), 50, 0, 0);
		assertEquals(far, target);
	}

	@Test
	public void respectsMaxDistance()
	{
		// Near is discovered, Far is ~283 tiles away; a 100-tile cap leaves nothing.
		Set<String> discovered = new HashSet<>();
		discovered.add(near.getKey());
		CompassTargeting targeting = new CompassTargeting(shortcuts, p -> discovered.contains(p.getKey()));
		PointOfInterest target = targeting.findTarget(new WorldPoint(3000, 3000, 0), 50, 0, 100);
		assertNull(target);
	}

	@Test
	public void respectsLevelBuffer()
	{
		// Level 12 with a buffer of 5 can't target a level-10 shortcut (12 - 10 = 2 < 5).
		CompassTargeting targeting = new CompassTargeting(
			Arrays.asList(near), p -> false);
		assertNull(targeting.findTarget(new WorldPoint(3000, 3000, 0), 12, 5, 0));
		// With enough headroom it targets fine.
		assertEquals(near, targeting.findTarget(new WorldPoint(3000, 3000, 0), 20, 5, 0));
	}

	@Test
	public void nullLocationYieldsNoTarget()
	{
		CompassTargeting targeting = new CompassTargeting(shortcuts, p -> false);
		assertNull(targeting.findTarget(null, 99, 0, 0));
	}
}
