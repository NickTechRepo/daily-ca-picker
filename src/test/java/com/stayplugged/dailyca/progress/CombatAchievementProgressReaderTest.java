package com.stayplugged.dailyca.progress;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import net.runelite.api.gameval.VarPlayerID;
import org.junit.Test;

public class CombatAchievementProgressReaderTest
{
	@Test
	public void readsCompletedIdsAcrossAllCompletionBoundaries()
	{
		CombatAchievementProgressReader reader = new CombatAchievementProgressReader();
		Set<Integer> completed = reader.readCompleted(646, varpId ->
		{
			if (varpId == VarPlayerID.CA_TASK_COMPLETED_0)
			{
				return (1 << 0) | (1 << 31);
			}
			if (varpId == VarPlayerID.CA_TASK_COMPLETED_1)
			{
				return (1 << 0) | (1 << 1);
			}
			if (varpId == VarPlayerID.CA_TASK_COMPLETED_19)
			{
				return 1 << 31;
			}
			if (varpId == VarPlayerID.CA_TASK_COMPLETED_20)
			{
				return (1 << 0) | (1 << 5);
			}
			return 0;
		});

		for (int taskId : new int[]{0, 31, 32, 33, 639, 640, 645})
		{
			assertTrue("Expected completion for task " + taskId, completed.contains(taskId));
		}
		assertFalse(completed.contains(34));
		assertTrue(reader.isCompletionVarp(VarPlayerID.CA_TASK_COMPLETED_20));
	}
}
