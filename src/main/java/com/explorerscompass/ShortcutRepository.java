package com.explorerscompass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

/**
 * Loads the bundled agility-shortcut catalogue from {@code agility_shortcuts.tsv}.
 * <p>
 * The file is parsed exactly once at construction. Malformed lines are logged
 * and skipped rather than aborting the whole load — a single typo shouldn't
 * nuke the entire plugin.
 * <p>
 * TSV columns: {@code name  x  y  plane  level}. Lines starting with {@code #}
 * and blank lines are ignored.
 */
@Slf4j
public class ShortcutRepository
{
	private static final String RESOURCE = "/com/explorerscompass/agility_shortcuts.tsv";
	private static final int EXPECTED_COLUMNS = 5;

	private final List<PointOfInterest> shortcuts;

	public ShortcutRepository()
	{
		this.shortcuts = Collections.unmodifiableList(load());
	}

	/** @return an unmodifiable view of every shortcut in the catalogue. */
	public List<PointOfInterest> all()
	{
		return shortcuts;
	}

	private List<PointOfInterest> load()
	{
		List<PointOfInterest> parsed = new ArrayList<>();

		try (InputStream in = ShortcutRepository.class.getResourceAsStream(RESOURCE))
		{
			if (in == null)
			{
				log.error("Explorer's Compass: shortcut resource {} not found on classpath", RESOURCE);
				return parsed;
			}

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
			{
				String line;
				int lineNumber = 0;
				while ((line = reader.readLine()) != null)
				{
					lineNumber++;
					PointOfInterest poi = parseLine(line, lineNumber);
					if (poi != null)
					{
						parsed.add(poi);
					}
				}
			}
		}
		catch (IOException e)
		{
			log.error("Explorer's Compass: failed to read shortcut catalogue", e);
		}

		log.debug("Explorer's Compass: loaded {} agility shortcut(s)", parsed.size());
		return parsed;
	}

	private PointOfInterest parseLine(String rawLine, int lineNumber)
	{
		String line = rawLine.trim();
		if (line.isEmpty() || line.startsWith("#"))
		{
			return null;
		}

		String[] parts = line.split("\t");
		if (parts.length != EXPECTED_COLUMNS)
		{
			log.warn("Explorer's Compass: skipping malformed line {} (expected {} tab-separated columns, got {}): {}",
				lineNumber, EXPECTED_COLUMNS, parts.length, line);
			return null;
		}

		try
		{
			String name = parts[0].trim();
			int x = Integer.parseInt(parts[1].trim());
			int y = Integer.parseInt(parts[2].trim());
			int plane = Integer.parseInt(parts[3].trim());
			int level = Integer.parseInt(parts[4].trim());

			return new PointOfInterest(name, new WorldPoint(x, y, plane), level);
		}
		catch (NumberFormatException e)
		{
			log.warn("Explorer's Compass: skipping line {} with non-numeric coordinate/level: {}", lineNumber, line);
			return null;
		}
	}
}
