package com.github.echolightmc.msnpcs;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDespawnEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.network.packet.client.play.ClientTeleportConfirmPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class NPCManager {

	static final Tag<Boolean> JOINING_INSTANCE_TAG = Tag.Boolean("msnpcs-joining-instance");

	private final Int2ObjectOpenHashMap<NPC> npcMap = new Int2ObjectOpenHashMap<>();

	public NPCManager(EventNode<Event> node) {
		node.addListener(PlayerSpawnEvent.class, event -> {
			event.getPlayer().setTag(JOINING_INSTANCE_TAG, true);
		});
		node.addListener(PlayerPacketEvent.class, event -> {
			if (event.getPacket() instanceof ClientTeleportConfirmPacket) { // player is fully in instance at this point
				Player player = event.getPlayer();
				if (player.hasTag(JOINING_INSTANCE_TAG)) {
					player.setTag(JOINING_INSTANCE_TAG, null);
					List<UUID> uuids = new ArrayList<>();
					for (NPC npc : npcMap.values()) {
						if (npc.getInstance().equals(player.getInstance())) uuids.add(npc.getUuid());
					}
					player.sendPacket(new PlayerInfoRemovePacket(uuids)); // covers joining server/instance case
				}
			}
		});
		node.addListener(EntityDespawnEvent.class, event -> {
			if (!(event.getEntity() instanceof NPC npc)) return;
			Set<Int2ObjectMap.Entry<NPC>> entrySetCopy = Set.copyOf(npcMap.int2ObjectEntrySet());
			for (Int2ObjectMap.Entry<NPC> entry : entrySetCopy) {
				if (entry.getValue().equals(npc)) {
					npcMap.remove(entry.getIntKey());
					return; // 1 npc shouldn't have 2 ids
				}
			}
		});
	}

	/**
	 * @param id the id of the npc to search for
	 * @return whether the npc with the specified id exists or not
	 */
	public boolean npcExists(int id) {
		return npcMap.containsKey(id);
	}

	/**
	 * @param id the id of the npc to search for
	 * @return the npc from the given id or null
	 */
	public @Nullable NPC getNPC(int id) {
		return npcMap.get(id);
	}

	/**
	 * Creates an npc from the given entitytype and name. Default entitytype is Player and if a name isn't provided,
	 * the id of the npc will be used.
	 *
	 * @param entityType the type this npc should be
	 * @param name the name of the npc
	 * @return a new npc entity from the given values
	 */
	public @NotNull NPC createNPC(@Nullable EntityType entityType, @Nullable String name) {
		NPC npc = new NPC(entityType, name);
		npcMap.put(npc.getNPCId(), npc);
		return npc;
	}

}
