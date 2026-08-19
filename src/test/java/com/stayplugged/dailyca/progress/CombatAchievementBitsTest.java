package com.stayplugged.dailyca.progress;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CombatAchievementBitsTest
{
	@Test
	public void readsCompletionAcrossVarpBoundaries()
	{
		int[] values = new int[3];
		values[0] = 1 << 31;
		values[1] = 1 << 1;

		assertTrue(CombatAchievementBits.isComplete(values, 31));
		assertTrue(CombatAchievementBits.isComplete(values, 33));
		assertFalse(CombatAchievementBits.isComplete(values, 32));
	}
}
