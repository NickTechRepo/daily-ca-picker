package com.stayplugged.dailyca.recommendation;

import com.stayplugged.dailyca.model.CaTier;
import com.stayplugged.dailyca.model.CombatAchievement;
import com.stayplugged.dailyca.model.PlayerProfile;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class RecommendationEngine
{
	private final TaskRequirementEstimator requirements = new TaskRequirementEstimator();

	public Recommendation select(List<CombatAchievement> tasks, PlayerProfile profile, LocalDate date,
		long accountSeed)
	{
		Map<CaTier, List<CombatAchievement>> eligibleByTier = new EnumMap<>(CaTier.class);
		for (CaTier tier : CaTier.values())
		{
			eligibleByTier.put(tier, new ArrayList<>());
		}
		for (CombatAchievement task : tasks)
		{
			if (!profile.getCompletedTaskIds().contains(task.getId())
				&& task.getTier().ordinal() <= profile.getGoalTier().ordinal()
				&& requirements.isGearFeasible(task, profile.getGearProfile())
				&& requirements.isStatsFeasible(task, profile.getCombatStats()))
			{
				eligibleByTier.get(task.getTier()).add(task);
			}
		}

		for (List<CombatAchievement> tierTasks : eligibleByTier.values())
		{
			tierTasks.sort(Comparator.comparingInt(CombatAchievement::getId));
		}

		int totalWeight = 0;
		for (CaTier tier : CaTier.values())
		{
			if (!eligibleByTier.get(tier).isEmpty())
			{
				totalWeight += tierWeight(tier, profile.getCurrentTier());
			}
		}
		if (totalWeight == 0)
		{
			return new Recommendation(null);
		}

		long seed = mix(date.toEpochDay() ^ (accountSeed * 0x9E3779B97F4A7C15L));
		int tierRoll = (int) Math.floorMod(seed, totalWeight);
		CaTier selectedTier = CaTier.EASY;
		for (CaTier tier : CaTier.values())
		{
			if (eligibleByTier.get(tier).isEmpty())
			{
				continue;
			}
			int weight = tierWeight(tier, profile.getCurrentTier());
			if (tierRoll < weight)
			{
				selectedTier = tier;
				break;
			}
			tierRoll -= weight;
		}

		List<CombatAchievement> tierTasks = eligibleByTier.get(selectedTier);
		long taskSeed = mix(seed ^ (selectedTier.ordinal() * 0xC2B2AE3D27D4EB4FL));
		int index = (int) Math.floorMod(taskSeed, tierTasks.size());
		return new Recommendation(tierTasks.get(index));
	}

	static int tierWeight(CaTier tier, CaTier current)
	{
		int delta = tier.ordinal() - current.ordinal();
		if (delta == 0)
		{
			return 45;
		}
		if (delta == -1)
		{
			return 25;
		}
		if (delta < -1)
		{
			return 10;
		}
		if (delta == 1)
		{
			return 20;
		}
		return 5;
	}

	private static long mix(long value)
	{
		value ^= value >>> 33;
		value *= 0xff51afd7ed558ccdL;
		value ^= value >>> 33;
		value *= 0xc4ceb9fe1a85ec53L;
		return value ^ (value >>> 33);
	}
}
