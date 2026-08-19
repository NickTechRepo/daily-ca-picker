package com.stayplugged.dailyca.recommendation;

import com.stayplugged.dailyca.model.CombatAchievement;
import com.stayplugged.dailyca.model.CombatStats;
import com.stayplugged.dailyca.model.GearProfile;
import java.util.Locale;

public final class TaskRequirementEstimator
{
	private static final int[] OFFENCE_LEVELS = {30, 45, 60, 75, 85, 92};
	private static final int[] DEFENCE_LEVELS = {20, 40, 55, 65, 75, 85};
	private static final int[] PRAYER_LEVELS = {1, 25, 43, 60, 70, 77};
	private static final int[] HITPOINT_LEVELS = {30, 50, 65, 75, 85, 90};
	private static final int[] GEAR_LEVELS = {0, 0, 1, 2, 3, 4};

	public boolean isStatsFeasible(CombatAchievement task, CombatStats stats)
	{
		int tier = task.getTier().ordinal();
		if (stats.getDefence() < DEFENCE_LEVELS[tier]
			|| stats.getPrayer() < PRAYER_LEVELS[tier]
			|| stats.getHitpoints() < HITPOINT_LEVELS[tier])
		{
			return false;
		}

		int required = Math.min(99, OFFENCE_LEVELS[tier] + (isSpeedTask(task) ? 5 : 0));
		switch (combatStyle(task))
		{
			case MELEE:
			case MELEE_NO_GEAR:
				return Math.min(stats.getAttack(), stats.getStrength()) >= required;
			case RANGED:
				return stats.getRanged() >= required;
			case MAGIC:
				return stats.getMagic() >= required;
			case RANGE_MAGIC:
				return Math.min(stats.getRanged(), stats.getMagic()) >= required;
			case MELEE_MAGIC:
				return Math.max(Math.min(stats.getAttack(), stats.getStrength()), stats.getMagic()) >= required;
			case MELEE_RANGE:
				return Math.min(Math.min(stats.getAttack(), stats.getStrength()), stats.getRanged()) >= required;
			case HYBRID:
				return Math.min(Math.min(stats.getAttack(), stats.getStrength()),
					Math.min(stats.getRanged(), stats.getMagic())) >= required;
			case SUPPLIED:
			case ANY:
			default:
				return bestOffence(stats) >= required;
		}
	}

	public boolean isGearFeasible(CombatAchievement task, GearProfile gear)
	{
		CombatStyle style = combatStyle(task);
		if (style == CombatStyle.SUPPLIED || style == CombatStyle.MELEE_NO_GEAR)
		{
			return true;
		}
		if (!gear.isBankScanned())
		{
			return false;
		}

		int required = Math.min(4, GEAR_LEVELS[task.getTier().ordinal()]
			+ (isSpeedTask(task) ? 1 : 0));
		switch (style)
		{
			case MELEE:
				return gear.getMeleeTier() >= required;
			case RANGED:
				return gear.getRangedTier() >= required;
			case MAGIC:
				return gear.getMagicTier() >= required;
			case RANGE_MAGIC:
				return Math.min(gear.getRangedTier(), gear.getMagicTier()) >= required;
			case MELEE_MAGIC:
				return Math.max(gear.getMeleeTier(), gear.getMagicTier()) >= required;
			case MELEE_RANGE:
				return Math.min(gear.getMeleeTier(), gear.getRangedTier()) >= required;
			case HYBRID:
				return Math.min(Math.min(gear.getMeleeTier(), gear.getRangedTier()),
					gear.getMagicTier()) >= required;
			case ANY:
			default:
				return Math.max(Math.max(gear.getMeleeTier(), gear.getRangedTier()),
					gear.getMagicTier()) >= required;
		}
	}

	private CombatStyle combatStyle(CombatAchievement task)
	{
		CombatStyle restrictedStyle = restrictedCombatStyle(task.getId());
		if (restrictedStyle != null)
		{
			return restrictedStyle;
		}

		String monster = normalizedMonster(task);
		if (containsAny(monster, "gauntlet"))
		{
			return CombatStyle.SUPPLIED;
		}
		if (containsAny(monster, "theatre of blood", "chambers of xeric", "tombs of amascut",
			"dagannoth kings", "kalphite queen", "grotesque guardians"))
		{
			return CombatStyle.HYBRID;
		}
		if (containsAny(monster, "zulrah", "phantom muspah"))
		{
			return CombatStyle.RANGE_MAGIC;
		}
		if (containsAny(monster, "kraken", "thermonuclear smoke devil", "the whisperer"))
		{
			return CombatStyle.MAGIC;
		}
		if (containsAny(monster, "tzkal-zuk", "tztok-jad", "fight caves", "inferno",
			"leviathan", "vorkath", "alchemical hydra", "kree'arra", "nex"))
		{
			return CombatStyle.RANGED;
		}
		if (containsAny(monster, "vardorvis", "duke sucellus", "cerberus", "araxxor",
			"sarachnis", "hespori", "general graardor", "phosani", "nightmare"))
		{
			return CombatStyle.MELEE;
		}
		return CombatStyle.ANY;
	}

	private CombatStyle restrictedCombatStyle(int taskId)
	{
		switch (taskId)
		{
			case 143: // Dharok's greataxe only
			case 153: // Fight Caves melee only
			case 161: // Verac's flail only
			case 165: // Stab weapon
			case 210: // Crush weapon
			case 216: // Zilyana melee only
			case 251: // Xarpus without ranged or magic weapons
			case 273: // Vorkath melee only
			case 367: // TzHaar challenge melee only
			case 368: // TzHaar challenge melee only
			case 428: // Zebak melee only
			case 429: // Wardens melee only
			case 534: // Dragon scimitar only
			case 540: // Spear, hasta, or halberd only
			case 548: // Claw special attack
			case 584: // Glacial temotli only
			case 618: // One-handed melee only
				return CombatStyle.MELEE;
			case 275: // Vorkath fists only
				return CombatStyle.MELEE_NO_GEAR;
			case 263: // Smoke Devil special attacks can be melee or magic
				return CombatStyle.MELEE_MAGIC;
			case 28: // Barrows magical damage only
			case 72: // Crazy Archaeologist magic only
			case 78: // Deranged Archaeologist magic only
			case 336: // K'ril demonbane spells only
			case 574: // Hueycoatl earth spells only
				return CombatStyle.MAGIC;
			case 511: // Leviathan mithril ammunition only
				return CombatStyle.RANGED;
			case 350: // Melee before wave 69, ranged for Zuk
				return CombatStyle.MELEE_RANGE;
			default:
				return null;
		}
	}

	private int bestOffence(CombatStats stats)
	{
		return Math.max(Math.max(Math.min(stats.getAttack(), stats.getStrength()), stats.getRanged()),
			stats.getMagic());
	}

	private boolean containsAny(String value, String... needles)
	{
		for (String needle : needles)
		{
			if (value.contains(needle))
			{
				return true;
			}
		}
		return false;
	}

	private String normalizedMonster(CombatAchievement task)
	{
		return task.getMonster().toLowerCase(Locale.ENGLISH);
	}

	private boolean isSpeedTask(CombatAchievement task)
	{
		String type = task.getType().toLowerCase(Locale.ENGLISH);
		String name = task.getName().toLowerCase(Locale.ENGLISH);
		String description = task.getDescription().toLowerCase(Locale.ENGLISH);
		return type.contains("speed") || name.contains("speed") || description.contains("within")
			|| description.contains("less than") || description.contains("under ");
	}

	private enum CombatStyle
	{
		MELEE,
		MELEE_NO_GEAR,
		RANGED,
		MAGIC,
		RANGE_MAGIC,
		MELEE_MAGIC,
		MELEE_RANGE,
		HYBRID,
		ANY,
		SUPPLIED
	}
}
