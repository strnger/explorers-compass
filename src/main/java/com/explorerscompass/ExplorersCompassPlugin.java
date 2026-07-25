package com.explorerscompass;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Explorer's Compass",
	description = "Points a compass arrow to the nearest agility shortcut you can use but haven't discovered yet",
	tags = {"agility", "shortcut", "explore", "discovery", "compass", "arrow", "navigation", "poi"}
)
public class ExplorersCompassPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ExplorersCompassConfig config;

	@Inject
	private ExplorersCompassOverlay overlay;

	private ShortcutRepository repository;
	private DiscoveryTracker discovery;
	private CompassTargeting targeting;

	/** The shortcut we're currently pointing at, or {@code null} when there's nothing to explore. */
	@Getter
	private PointOfInterest currentTarget;

	/** Whether we've placed a hint arrow this session, so we only clear our own. */
	private boolean hintArrowActive;

	@Provides
	ExplorersCompassConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExplorersCompassConfig.class);
	}

	@Override
	protected void startUp()
	{
		repository = new ShortcutRepository();
		discovery = new DiscoveryTracker(configManager);
		targeting = new CompassTargeting(repository.all(), discovery::isDiscovered);
		overlayManager.add(overlay);
		log.info("Explorer's Compass started with {} shortcut(s)", repository.all().size());
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		clearHintArrow();
		currentTarget = null;
		repository = null;
		discovery = null;
		targeting = null;
		log.info("Explorer's Compass stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		// Hint arrows don't survive loading screens; forget our bookkeeping so we re-place cleanly.
		if (event.getGameState() == GameState.LOADING || event.getGameState() == GameState.HOPPING)
		{
			hintArrowActive = false;
			currentTarget = null;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!ExplorersCompassConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		if ("resetDiscovered".equals(event.getKey()) && config.resetDiscovered())
		{
			if (discovery != null)
			{
				discovery.reset();
			}
			// Untoggle so it behaves like a button.
			configManager.setConfiguration(ExplorersCompassConfig.GROUP, "resetDiscovered", false);
		}

		if ("showHintArrow".equals(event.getKey()) && !config.showHintArrow())
		{
			clearHintArrow();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (targeting == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		final Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		final WorldPoint playerLocation = player.getWorldLocation();
		if (playerLocation == null)
		{
			return;
		}

		markNearbyDiscovered(playerLocation);

		final int agilityLevel = client.getRealSkillLevel(Skill.AGILITY);
		final PointOfInterest newTarget = targeting.findTarget(
			playerLocation, agilityLevel, config.levelBuffer(), config.maxDistance());

		updateTarget(newTarget);
	}

	/**
	 * Marks any undiscovered shortcut the player is standing next to (same plane,
	 * within the configured radius) as discovered. If the freshly-discovered POI
	 * was our current target, drop it so the next tick re-targets.
	 */
	private void markNearbyDiscovered(WorldPoint playerLocation)
	{
		final int radius = config.discoveryRadius();

		for (PointOfInterest poi : repository.all())
		{
			if (discovery.isDiscovered(poi))
			{
				continue;
			}

			final WorldPoint loc = poi.getLocation();
			if (loc.getPlane() != playerLocation.getPlane())
			{
				continue;
			}

			final int chebyshev = Math.max(
				Math.abs(loc.getX() - playerLocation.getX()),
				Math.abs(loc.getY() - playerLocation.getY()));

			if (chebyshev <= radius && discovery.markDiscovered(poi) && poi.equals(currentTarget))
			{
				currentTarget = null;
			}
		}
	}

	/** Applies a newly computed target, updating the hint arrow only when it actually changes. */
	private void updateTarget(PointOfInterest newTarget)
	{
		if (java.util.Objects.equals(newTarget, currentTarget))
		{
			return;
		}

		currentTarget = newTarget;

		if (newTarget == null)
		{
			clearHintArrow();
			return;
		}

		if (config.showHintArrow())
		{
			client.setHintArrow(newTarget.getLocation());
			hintArrowActive = true;
		}
	}

	private void clearHintArrow()
	{
		if (hintArrowActive)
		{
			client.clearHintArrow();
			hintArrowActive = false;
		}
	}
}
