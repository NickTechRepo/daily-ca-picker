package com.stayplugged.dailyca.recommendation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.stayplugged.dailyca.model.CaTier;
import com.stayplugged.dailyca.model.CombatAchievement;
import com.stayplugged.dailyca.model.CombatStats;
import com.stayplugged.dailyca.model.GearProfile;
import org.junit.Test;

public class TaskRequirementEstimatorTest
{
	@Test
	public void rejectsGrandmasterRaidPerfectionWithoutHybridEndgameGear()
	{
		CombatAchievement perfectTob = new CombatAchievement(500, "Perfect Theatre",
			CaTier.GRANDMASTER, "Theatre of Blood", "Perfection", "Complete a perfect Theatre run.");

		assertFalse(new TaskRequirementEstimator().isGearFeasible(
			perfectTob, new GearProfile(2, 4, 2, true)));
	}

	@Test
	public void rejectsMagicSpeedTaskWhenOnlyRangedGearIsStrong()
	{
		CombatAchievement krakenSpeed = new CombatAchievement(501, "Kraken Speed-Trialist",
			CaTier.MASTER, "Kraken", "Speed", "Kill the Kraken within the time limit.");

		assertFalse(new TaskRequirementEstimator().isGearFeasible(
			krakenSpeed, new GearProfile(2, 4, 2, true)));
	}

	@Test
	public void rejectsEndgameTaskWithLowSurvivabilityStats()
	{
		CombatAchievement zuk = new CombatAchievement(502, "Facing Jad Head-on IV",
			CaTier.GRANDMASTER, "TzKal-Zuk", "Mechanical", "Defeat Zuk with a restriction.");
		CombatStats glassCannon = new CombatStats(99, 99, 40, 99, 99, 43, 55);

		assertFalse(new TaskRequirementEstimator().isStatsFeasible(zuk, glassCannon));
	}

	@Test
	public void requiresRangedGearForRangedBoss()
	{
		CombatAchievement leviathan = new CombatAchievement(503, "Leviathan Mastery",
			CaTier.ELITE, "The Leviathan", "Mechanical", "Defeat the Leviathan.");

		assertFalse(new TaskRequirementEstimator().isGearFeasible(
			leviathan, new GearProfile(4, 1, 4, true)));
	}

	@Test
	public void appliesGeneralGearFloorToUnknownBosses()
	{
		CombatAchievement unknown = new CombatAchievement(504, "Unknown Challenge",
			CaTier.HARD, "New Boss", "Mechanical", "Defeat the new boss.");

		assertFalse(new TaskRequirementEstimator().isGearFeasible(
			unknown, new GearProfile(0, 0, 0, true)));
	}

	@Test
	public void doesNotUseBankGearForGauntletTasks()
	{
		CombatAchievement gauntlet = new CombatAchievement(505, "Gauntlet Speed",
			CaTier.MASTER, "The Gauntlet", "Speed", "Complete the Gauntlet quickly.");

		assertTrue(new TaskRequirementEstimator().isGearFeasible(
			gauntlet, new GearProfile(0, 0, 0, true)));
	}

	@Test
	public void taskRestrictionOverridesBossDefaultStyle()
	{
		TaskRequirementEstimator estimator = new TaskRequirementEstimator();
		CombatAchievement meleeJad = new CombatAchievement(153, "Facing Jad Head-on", CaTier.ELITE,
			"TzTok-Jad", "Mechanical", "Complete the Fight Caves with only melee.");
		CombatAchievement meleeVorkath = new CombatAchievement(273, "Stick 'em With the Pointy End", CaTier.ELITE,
			"Vorkath", "Restriction", "Kill Vorkath using melee weapons only.");
		CombatAchievement magicHuey = new CombatAchievement(574, "You're a wizard", CaTier.MEDIUM,
			"The Hueycoatl", "Restriction", "Kill the Hueycoatl using only earth spells.");

		assertFalse(estimator.isGearFeasible(meleeJad, new GearProfile(1, 4, 4, true)));
		assertFalse(estimator.isGearFeasible(meleeVorkath, new GearProfile(1, 4, 4, true)));
		assertFalse(estimator.isStatsFeasible(magicHuey,
			new CombatStats(99, 99, 99, 99, 30, 99, 99)));
		assertTrue(estimator.isStatsFeasible(magicHuey,
			new CombatStats(30, 30, 99, 30, 99, 99, 99)));
		assertTrue(estimator.isGearFeasible(magicHuey, new GearProfile(0, 0, 0, true)));

		CombatAchievement smokeDevilSpecs = new CombatAchievement(263, "Spec'd Out", CaTier.ELITE,
			"Thermonuclear Smoke Devil", "Restriction", "Kill using only special attacks.");
		assertTrue(estimator.isGearFeasible(smokeDevilSpecs, new GearProfile(2, 0, 0, true)));
		assertTrue(estimator.isGearFeasible(smokeDevilSpecs, new GearProfile(0, 0, 2, true)));
		assertFalse(estimator.isGearFeasible(smokeDevilSpecs, new GearProfile(1, 4, 1, true)));

		CombatAchievement fistsVorkath = new CombatAchievement(275, "The Fremennik Way", CaTier.GRANDMASTER,
			"Vorkath", "Restriction", "Kill Vorkath with only your fists.");
		assertTrue(estimator.isGearFeasible(fistsVorkath, new GearProfile(0, 0, 0, true)));
		assertFalse(estimator.isStatsFeasible(fistsVorkath,
			new CombatStats(80, 80, 99, 99, 99, 99, 99)));

		CombatAchievement meleeRangeZuk = new CombatAchievement(350, "Facing Jad Head-on II", CaTier.GRANDMASTER,
			"TzKal-Zuk", "Restriction", "Kill Tzkal-Zuk without equipping any range or mage weapons before wave 69.");
		assertTrue(estimator.isGearFeasible(meleeRangeZuk, new GearProfile(4, 4, 0, true)));
		assertFalse(estimator.isGearFeasible(meleeRangeZuk, new GearProfile(1, 4, 4, true)));
	}
}
