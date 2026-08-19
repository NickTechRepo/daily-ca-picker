package com.stayplugged.dailyca.recommendation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.stayplugged.dailyca.model.CaTier;
import com.stayplugged.dailyca.model.CombatAchievement;
import com.stayplugged.dailyca.model.CombatStats;
import com.stayplugged.dailyca.model.GearProfile;
import com.stayplugged.dailyca.model.PlayerProfile;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.junit.Test;

public class RecommendationEngineTest
{
	@Test
	public void selectsStableTaskIndependentOfCatalogOrder()
	{
		CombatAchievement firstTask = new CombatAchievement(
			1, "First", CaTier.MEDIUM, "Barrows", "Mechanical", "Defeat a Barrows brother.");
		CombatAchievement secondTask = new CombatAchievement(
			2, "Second", CaTier.MEDIUM, "Barrows", "Mechanical", "Defeat a Barrows brother.");
		CombatAchievement thirdTask = new CombatAchievement(
			3, "Third", CaTier.MEDIUM, "Barrows", "Mechanical", "Defeat a Barrows brother.");
		PlayerProfile profile = PlayerProfile.builder()
			.currentTier(CaTier.HARD)
			.goalTier(CaTier.ELITE)
			.gearProfile(GearProfile.midLevel())
			.build();

		RecommendationEngine engine = new RecommendationEngine();
		LocalDate date = LocalDate.of(2026, 8, 19);
		Recommendation original = engine.select(
			Arrays.asList(firstTask, secondTask, thirdTask), profile, date, 42L);
		Recommendation reordered = engine.select(
			Arrays.asList(thirdTask, secondTask, firstTask), profile, date, 42L);

		assertNotNull(original.getTask());
		assertEquals(original.getTask().getId(), reordered.getTask().getId());
	}

	@Test
	public void excludesTasksAboveGoalTier()
	{
		CombatAchievement attainable = new CombatAchievement(
			10, "Hard task", CaTier.HARD, "Barrows", "Mechanical", "Complete a hard task.");
		CombatAchievement tooHigh = new CombatAchievement(
			11, "Perfect Theatre", CaTier.GRANDMASTER, "Theatre of Blood", "Perfection", "Complete a perfect raid.");
		PlayerProfile profile = PlayerProfile.builder()
			.currentTier(CaTier.HARD)
			.goalTier(CaTier.ELITE)
			.gearProfile(GearProfile.midLevel())
			.build();

		Recommendation result = new RecommendationEngine().select(
			Arrays.asList(tooHigh, attainable), profile, LocalDate.of(2026, 8, 20), 7L);

		assertEquals(attainable, result.getTask());
	}

	@Test
	public void rejectsVardorvisSpeedTaskWithoutHighEndMeleeGear()
	{
		CombatAchievement vardorvisSpeed = new CombatAchievement(
			20, "Vardorvis Speed-Chaser", CaTier.ELITE, "Vardorvis", "Speed", "Kill Vardorvis within 1:15.");
		CombatAchievement realistic = new CombatAchievement(
			21, "Mole Mechanical", CaTier.HARD, "Giant Mole", "Mechanical", "Kill the Giant Mole without taking damage.");
		PlayerProfile profile = PlayerProfile.builder()
			.currentTier(CaTier.HARD)
			.goalTier(CaTier.ELITE)
			.gearProfile(GearProfile.midLevel())
			.build();

		Recommendation result = new RecommendationEngine().select(
			Arrays.asList(vardorvisSpeed, realistic), profile, LocalDate.of(2026, 8, 21), 0L);

		assertEquals(realistic, result.getTask());
	}

	@Test
	public void rejectsMasterSpeedTaskWhenCombatStatsAreTooLow()
	{
		CombatAchievement masterSpeed = new CombatAchievement(
			30, "Vardorvis Speed-Chaser", CaTier.MASTER, "Vardorvis", "Speed", "Kill Vardorvis in less than one minute.");
		CombatAchievement hardTask = new CombatAchievement(
			31, "A Hard Day", CaTier.HARD, "Giant Mole", "Mechanical", "Kill the Giant Mole.");
		PlayerProfile profile = PlayerProfile.builder()
			.currentTier(CaTier.HARD)
			.goalTier(CaTier.MASTER)
			.gearProfile(new GearProfile(4, 4, 4, true))
			.combatStats(new CombatStats(70, 70, 70, 70, 70, 70, 70))
			.build();

		Recommendation result = new RecommendationEngine().select(
			Arrays.asList(masterSpeed, hardTask), profile, LocalDate.of(2026, 8, 21), 0L);

		assertEquals(hardTask, result.getTask());
	}

	@Test
	public void returnsNoTaskWhenEligiblePoolIsEmpty()
	{
		PlayerProfile profile = PlayerProfile.builder()
			.currentTier(CaTier.HARD)
			.goalTier(CaTier.ELITE)
			.gearProfile(GearProfile.midLevel())
			.build();

		assertNull(new RecommendationEngine().select(Collections.emptyList(), profile,
			LocalDate.of(2026, 8, 19), 1L).getTask());
	}

	@Test
	public void exposesExactTierWeightContract()
	{
		assertEquals(45, RecommendationEngine.tierWeight(CaTier.HARD, CaTier.HARD));
		assertEquals(25, RecommendationEngine.tierWeight(CaTier.MEDIUM, CaTier.HARD));
		assertEquals(10, RecommendationEngine.tierWeight(CaTier.EASY, CaTier.HARD));
		assertEquals(20, RecommendationEngine.tierWeight(CaTier.ELITE, CaTier.HARD));
		assertEquals(5, RecommendationEngine.tierWeight(CaTier.MASTER, CaTier.HARD));
	}

	@Test
	public void weightsCurrentTierWhileKeepingLowerTiersInRotation()
	{
		PlayerProfile profile = PlayerProfile.builder()
			.currentTier(CaTier.HARD)
			.goalTier(CaTier.ELITE)
			.gearProfile(new GearProfile(4, 4, 4, true))
			.build();
		Map<CaTier, Integer> counts = new EnumMap<>(CaTier.class);
		for (CaTier tier : CaTier.values())
		{
			counts.put(tier, 0);
		}
		for (int day = 0; day < 360; day++)
		{
			Recommendation result = new RecommendationEngine().select(Arrays.asList(
				new CombatAchievement(40, "Easy", CaTier.EASY, "", "Mechanical", ""),
				new CombatAchievement(41, "Medium", CaTier.MEDIUM, "", "Mechanical", ""),
				new CombatAchievement(42, "Hard", CaTier.HARD, "", "Mechanical", ""),
				new CombatAchievement(43, "Elite", CaTier.ELITE, "", "Mechanical", "")),
				profile, LocalDate.of(2026, 1, 1).plusDays(day), 123L);
			counts.put(result.getTask().getTier(), counts.get(result.getTask().getTier()) + 1);
		}

		assertTrue(counts.get(CaTier.EASY) > 0);
		assertTrue(counts.get(CaTier.MEDIUM) > 0);
		assertTrue(counts.get(CaTier.HARD) > counts.get(CaTier.MEDIUM));
		assertTrue(counts.get(CaTier.HARD) > counts.get(CaTier.EASY) * 2);
		assertEquals(Integer.valueOf(0), counts.get(CaTier.MASTER));
		assertEquals(Integer.valueOf(0), counts.get(CaTier.GRANDMASTER));
	}
}
