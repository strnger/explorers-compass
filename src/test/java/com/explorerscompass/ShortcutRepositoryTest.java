package com.explorerscompass;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShortcutRepositoryTest
{
	@Test
	public void loadsBundledShortcuts()
	{
		ShortcutRepository repository = new ShortcutRepository();
		assertFalse("Expected the bundled TSV to yield shortcuts", repository.all().isEmpty());
	}

	@Test
	public void everyShortcutHasSaneData()
	{
		ShortcutRepository repository = new ShortcutRepository();
		for (PointOfInterest poi : repository.all())
		{
			assertFalse("Shortcut name must not be blank", poi.getName().isEmpty());
			assertTrue("Level must be within 1-99, was " + poi.getRequiredLevel(),
				poi.getRequiredLevel() >= 1 && poi.getRequiredLevel() <= 99);
			assertTrue("Coordinates must be positive", poi.getLocation().getX() > 0);
			assertTrue("Coordinates must be positive", poi.getLocation().getY() > 0);
		}
	}

	@Test
	public void keysAreUnique()
	{
		ShortcutRepository repository = new ShortcutRepository();
		long distinctKeys = repository.all().stream().map(PointOfInterest::getKey).distinct().count();
		assertTrue("POI keys must be unique", distinctKeys == repository.all().size());
	}
}
