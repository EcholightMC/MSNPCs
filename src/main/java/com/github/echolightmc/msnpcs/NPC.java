package com.github.echolightmc.msnpcs;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.echolightmc.msnpcs.NPCManager.JOINING_INSTANCE_TAG;

public class NPC extends EntityCreature {

	private static final AtomicInteger LAST_NPC_ID = new AtomicInteger();

	private static final Tag<String> MSNAMETAGS_USERNAME_TAG = Tag.String("msnametags-username"); // msnametags support
	private static final Tag<PlayerSkin> PLAYER_SKIN_TAG = Tag.Structure("player-skin", PlayerSkin.class);

	private final int npcId;
	protected String name;

	private final CachedPacket playerInfoRemovePacket = new CachedPacket(new PlayerInfoRemovePacket(getUuid()));

	NPC(@Nullable EntityType entityType, @Nullable String name) {
		super(entityType == null ? EntityType.PLAYER : entityType);
		this.npcId = LAST_NPC_ID.incrementAndGet();
		this.name = "[NPC] ";
		if (name != null) {
			this.name += name;
		} else {
			this.name += npcId;
		}
		initCustomName();
		if (entityType == EntityType.PLAYER) {
			initPlayerMeta();
			setTag(MSNAMETAGS_USERNAME_TAG, this.name);
		}
	}

	@Override
	public void updateNewViewer(Player player) {
		if (entityType == EntityType.PLAYER) {
			List<PlayerInfoUpdatePacket.Property> properties = new ArrayList<>();
			PlayerSkin playerSkin = getPlayerSkin();
			if (playerSkin != null)
				properties.add(new PlayerInfoUpdatePacket.Property("textures", playerSkin.textures(), playerSkin.signature()));
			PlayerInfoUpdatePacket.Entry playerEntry = new PlayerInfoUpdatePacket.Entry(getUuid(), getTrimmedPlayerUsername(), properties,
					false, 0, GameMode.CREATIVE, Component.empty(), null);
			PlayerInfoUpdatePacket updatePacket = new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, playerEntry);
			player.sendPacket(updatePacket);
			if (!player.hasTag(JOINING_INSTANCE_TAG)) MinecraftServer.getSchedulerManager().scheduleNextTick(() -> player.sendPacket(playerInfoRemovePacket));
		}
		super.updateNewViewer(player);
	}

	@Override
	public void updateOldViewer(@NotNull Player player) {
		super.updateOldViewer(player);
		if (entityType == EntityType.PLAYER) player.sendPacket(playerInfoRemovePacket);
	}

	/**
	 * Swaps the entity type of this npc seamlessly and has MSNameTags support done for you.
	 * Also ensures that the custom name is persistent.
	 *
	 * @param entityType the new entity type
	 */
	@Override
	public synchronized void switchEntityType(@NotNull EntityType entityType) {
		if (this.entityType == entityType) return; // no need to do anything if the entitytype requested is the same as current
		if (entityType == EntityType.PLAYER) {
			initPlayerMeta();
			setTag(MSNAMETAGS_USERNAME_TAG, getTrimmedPlayerUsername()); // should set to trimmed version so they get added to the team properly
		} else setTag(MSNAMETAGS_USERNAME_TAG, null); // makes msnametags recognize this as a non player, so it can use its uuid to add it to a team
		super.switchEntityType(entityType);
		initCustomName();
	}

	/**
	 * @return the id of this npc
	 */
	public int getNPCId() {
		return npcId;
	}

	/**
	 * @return The name of this npc (e.g. [NPC] 1)
	 */
	@Nullable
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this npc. If a proper custom nametag is needed, I recommend using MSNameTags.
	 *
	 * @param name the future name of this npc with '[NPC] ' prefixed
	 */
	public void setName(String name) {
		this.name = "[NPC] " + name;
		entityMeta.setCustomName(Component.text(this.name)); // make sure to update visually in-game when this method is called
	}

	/**
	 * @return the playerskin value of this npc if present
	 */
	public @Nullable PlayerSkin getPlayerSkin() {
		return getTag(PLAYER_SKIN_TAG);
	}

	/**
	 * Sets the playerskin value of this NPC. Only changes visually if/once the npc is of type player.
	 *
	 * @param playerSkin the new skin
	 */
	public void setPlayerSkin(PlayerSkin playerSkin) {
		setTag(PLAYER_SKIN_TAG, playerSkin);
		if (entityType != EntityType.PLAYER || !isActive()) return;
		PlayerInfoRemovePacket removePacket = new PlayerInfoRemovePacket(getUuid());
		DestroyEntitiesPacket destroyPacket = new DestroyEntitiesPacket(getEntityId());
		for (Player p : viewers) {
			p.sendPacket(removePacket);
			p.sendPacket(destroyPacket);
			updateNewViewer(p);
		}
	}

	private String getTrimmedPlayerUsername() {
		return name.substring(0, Math.min(name.length(), 15));
	}

	private void initCustomName() {
		entityMeta.setNotifyAboutChanges(false);
		entityMeta.setCustomName(Component.text(this.name));
		entityMeta.setCustomNameVisible(true);
		entityMeta.setNotifyAboutChanges(true);
	}

	private void initPlayerMeta() {
		if (entityType != EntityType.PLAYER) return;
		PlayerMeta playerMeta = (PlayerMeta) getEntityMeta();
		playerMeta.setNotifyAboutChanges(false);
		playerMeta.setCapeEnabled(true);
		playerMeta.setHatEnabled(true);
		playerMeta.setJacketEnabled(true);
		playerMeta.setLeftLegEnabled(true);
		playerMeta.setLeftSleeveEnabled(true);
		playerMeta.setRightLegEnabled(true);
		playerMeta.setRightSleeveEnabled(true);
		playerMeta.setNotifyAboutChanges(true);
	}

}
