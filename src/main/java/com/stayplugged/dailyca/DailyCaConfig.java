package com.stayplugged.dailyca;

import com.stayplugged.dailyca.model.CaTier;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(DailyCaConfig.GROUP)
public interface DailyCaConfig extends Config
{
	String GROUP = "stayplugged-daily-ca-picker";

	@ConfigItem(
		keyName = "currentTier",
		name = "Current CA tier",
		description = "The Combat Achievement tier you are currently working at.",
		position = 0
	)
	default CaTier currentTier()
	{
		return CaTier.HARD;
	}

	@ConfigItem(
		keyName = "goalTier",
		name = "Goal CA tier",
		description = "The highest tier the daily picker may recommend.",
		position = 1
	)
	default CaTier goalTier()
	{
		return CaTier.ELITE;
	}

	@ConfigItem(
		keyName = "useBankGear",
		name = "Use bank gear",
		description = "Filter gear-sensitive tasks using the strongest gear observed when your bank was opened.",
		position = 2
	)
	default boolean useBankGear()
	{
		return true;
	}

	@ConfigItem(
		keyName = "announceInChat",
		name = "Announce daily task",
		description = "Show the daily Combat Achievement in game chat after login.",
		position = 3
	)
	default boolean announceInChat()
	{
		return true;
	}
}
