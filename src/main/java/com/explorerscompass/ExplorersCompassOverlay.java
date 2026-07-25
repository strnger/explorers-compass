package com.explorerscompass;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * A small panel showing the currently targeted shortcut: its name, level
 * requirement, and how far away it is. Renders nothing when there's no target
 * or the overlay is disabled.
 */
public class ExplorersCompassOverlay extends OverlayPanel
{
	private final Client client;
	private final ExplorersCompassPlugin plugin;
	private final ExplorersCompassConfig config;

	@Inject
	private ExplorersCompassOverlay(Client client, ExplorersCompassPlugin plugin, ExplorersCompassConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showInfoOverlay())
		{
			return null;
		}

		final PointOfInterest target = plugin.getCurrentTarget();
		if (target == null || client.getLocalPlayer() == null)
		{
			return null;
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Nearest Shortcut")
			.color(Color.CYAN)
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Where:")
			.right(target.getName())
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Agility:")
			.right(Integer.toString(target.getRequiredLevel()))
			.build());

		final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		if (playerLocation != null)
		{
			long tiles = Math.round(Math.sqrt(
				CompassTargeting.distanceSquared2D(playerLocation, target.getLocation())));
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Distance:")
				.right(tiles + " tiles")
				.build());
		}

		return super.render(graphics);
	}
}
