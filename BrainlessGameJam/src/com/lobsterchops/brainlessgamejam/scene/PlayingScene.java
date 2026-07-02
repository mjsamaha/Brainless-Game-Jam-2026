package com.lobsterchops.brainlessgamejam.scene;

import java.awt.Graphics2D;

import com.lobsterchops.brainlessgamejam.entity.FriendlyEntity;
import com.lobsterchops.brainlessgamejam.entity.ImposterEntity;
import com.lobsterchops.brainlessgamejam.entity.PlayerEntity;
import com.lobsterchops.brainlessgamejam.entity.UpdateContext;
import com.lobsterchops.brainlessgamejam.event.CollisionEvent;
import com.lobsterchops.brainlessgamejam.event.EventBus;
import com.lobsterchops.brainlessgamejam.render.RenderPipeline;
import com.lobsterchops.brainlessgamejam.world.GameSystem;
import com.lobsterchops.brainlessgamejam.world.TrailSystem;

/**
 * <h4>Wraps the existing gameplay loop (GameSystem + RenderPipeline) as a {@link Scene}.</h4>
 *
 * <p>This is an adapter only — it does not change how GameSystem or RenderPipeline
 * behave. Update/render calls are simply forwarded.</p>
 *
 * <p>Collision reactions for swarm entities are wired here in {@link #enter()} via
 * the {@link EventBus}, and cleared on {@link #exit()} via {@code eventBus.clear()}.</p>
 */
public class PlayingScene implements Scene {

	private final GameSystem gameSystem;
	private final RenderPipeline renderPipeline;
	private final EventBus eventBus;
	private final TrailSystem trailSystem;

	public PlayingScene(GameSystem gameSystem, RenderPipeline renderPipeline,
			EventBus eventBus, TrailSystem trailSystem) {
		this.gameSystem     = gameSystem;
		this.renderPipeline = renderPipeline;
		this.eventBus       = eventBus;
		this.trailSystem    = trailSystem;
	}

	@Override
	public void enter() {
		eventBus.subscribe(CollisionEvent.class, this::onCollision);
	}

	@Override
	public void update(UpdateContext context) {
		gameSystem.update();
	}

	@Override
	public void render(Graphics2D g2) {
		renderPipeline.render(g2);
	}

	private void onCollision(CollisionEvent e) {
		boolean playerInvolved =
			e.a() instanceof PlayerEntity || e.b() instanceof PlayerEntity;

		if (!playerInvolved) return;

		if (e.a() instanceof FriendlyEntity || e.b() instanceof FriendlyEntity) {
			FriendlyEntity friendly = (e.a() instanceof FriendlyEntity)
				? (FriendlyEntity) e.a()
				: (FriendlyEntity) e.b();
			friendly.markInactive();
			trailSystem.attach(true);
			// scoreTracker.penalise();  Phase D
		}

		if (e.a() instanceof ImposterEntity || e.b() instanceof ImposterEntity) {
			ImposterEntity imposter = (e.a() instanceof ImposterEntity)
				? (ImposterEntity) e.a()
				: (ImposterEntity) e.b();
			imposter.markInactive();
			trailSystem.attach(false);
			trailSystem.detach();
			// scoreTracker.reward();    Phase D
		}
	}

}