package net.runelite.client.plugins.spoonitemcharges;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.runelite.api.ItemID;

import java.util.Map;
import javax.annotation.Nullable;

enum SpoonItemWithConfig {
	DODGY_NECKLACE(ItemID.DODGY_NECKLACE, SpoonItemChargeConfig.KEY_DODGY_NECKLACE, SpoonItemChargeType.DODGY_NECKLACE),
	BINDING_NECKLACE(ItemID.BINDING_NECKLACE, SpoonItemChargeConfig.KEY_BINDING_NECKLACE, SpoonItemChargeType.BINDING_NECKLACE),
	EXPLORERS_RING_1(ItemID.EXPLORERS_RING_1, SpoonItemChargeConfig.KEY_EXPLORERS_RING, SpoonItemChargeType.EXPLORER_RING),
	EXPLORERS_RING_2(ItemID.EXPLORERS_RING_2, SpoonItemChargeConfig.KEY_EXPLORERS_RING, SpoonItemChargeType.EXPLORER_RING),
	EXPLORERS_RING_3(ItemID.EXPLORERS_RING_3, SpoonItemChargeConfig.KEY_EXPLORERS_RING, SpoonItemChargeType.EXPLORER_RING),
	EXPLORERS_RING_4(ItemID.EXPLORERS_RING_4, SpoonItemChargeConfig.KEY_EXPLORERS_RING, SpoonItemChargeType.EXPLORER_RING),
	RING_OF_FORGING(ItemID.RING_OF_FORGING, SpoonItemChargeConfig.KEY_RING_OF_FORGING, SpoonItemChargeType.RING_OF_FORGING),
	AMULET_OF_CHEMISTRY(ItemID.AMULET_OF_CHEMISTRY, SpoonItemChargeConfig.KEY_AMULET_OF_CHEMISTRY, SpoonItemChargeType.AMULET_OF_CHEMISTRY),
	AMULET_OF_BOUNTY(ItemID.AMULET_OF_BOUNTY, SpoonItemChargeConfig.KEY_AMULET_OF_BOUNTY, SpoonItemChargeType.AMULET_OF_BOUNTY),
	BRACELET_OF_SLAUGHTER(ItemID.BRACELET_OF_SLAUGHTER, SpoonItemChargeConfig.KEY_BRACELET_OF_SLAUGHTER, SpoonItemChargeType.BRACELET_OF_SLAUGHTER),
	EXPEDITIOUS_BRACELET(ItemID.EXPEDITIOUS_BRACELET, SpoonItemChargeConfig.KEY_EXPEDITIOUS_BRACELET, SpoonItemChargeType.EXPEDITIOUS_BRACELET),
	CHRONICLE(ItemID.CHRONICLE, SpoonItemChargeConfig.KEY_CHRONICLE, SpoonItemChargeType.TELEPORT),
	BLOOD_ESSENCE(ItemID.BLOOD_ESSENCE_ACTIVE, SpoonItemChargeConfig.KEY_BLOOD_ESSENCE, SpoonItemChargeType.BLOOD_ESSENCE);

	SpoonItemWithConfig(int itemId, String configKey, SpoonItemChargeType type) {
		this.itemId = itemId;
		this.configKey = configKey;
		this.type = type;
	}

	@Getter
	private final int itemId;

	@Getter
	private final String configKey;

	@Getter
	private final SpoonItemChargeType type;

	private static final Map<Integer, SpoonItemWithConfig> ID_MAP;

	static {
		ImmutableMap.Builder<Integer, SpoonItemWithConfig> builder = new ImmutableMap.Builder();
		for (SpoonItemWithConfig item : values())
			builder.put(item.getItemId(), item);
		ID_MAP = (Map<Integer, SpoonItemWithConfig>)builder.build();
	}

	@Nullable
	static SpoonItemWithConfig findItem(int itemId) {
		return ID_MAP.get(itemId);
	}
}
