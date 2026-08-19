package com.stayplugged.dailyca.progress;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import net.runelite.api.gameval.VarPlayerID;

public final class CombatAchievementProgressReader
{
	private static final int[] COMPLETION_VARPS = {
		VarPlayerID.CA_TASK_COMPLETED_0,
		VarPlayerID.CA_TASK_COMPLETED_1,
		VarPlayerID.CA_TASK_COMPLETED_2,
		VarPlayerID.CA_TASK_COMPLETED_3,
		VarPlayerID.CA_TASK_COMPLETED_4,
		VarPlayerID.CA_TASK_COMPLETED_5,
		VarPlayerID.CA_TASK_COMPLETED_6,
		VarPlayerID.CA_TASK_COMPLETED_7,
		VarPlayerID.CA_TASK_COMPLETED_8,
		VarPlayerID.CA_TASK_COMPLETED_9,
		VarPlayerID.CA_TASK_COMPLETED_10,
		VarPlayerID.CA_TASK_COMPLETED_11,
		VarPlayerID.CA_TASK_COMPLETED_12,
		VarPlayerID.CA_TASK_COMPLETED_13,
		VarPlayerID.CA_TASK_COMPLETED_14,
		VarPlayerID.CA_TASK_COMPLETED_15,
		VarPlayerID.CA_TASK_COMPLETED_16,
		VarPlayerID.CA_TASK_COMPLETED_17,
		VarPlayerID.CA_TASK_COMPLETED_18,
		VarPlayerID.CA_TASK_COMPLETED_19,
		VarPlayerID.CA_TASK_COMPLETED_20
	};

	public Set<Integer> readCompleted(int taskCount, IntUnaryOperator varpReader)
	{
		int[] values = new int[COMPLETION_VARPS.length];
		for (int index = 0; index < COMPLETION_VARPS.length; index++)
		{
			values[index] = varpReader.applyAsInt(COMPLETION_VARPS[index]);
		}
		Set<Integer> completed = new HashSet<>();
		for (int taskId = 0; taskId < taskCount; taskId++)
		{
			if (CombatAchievementBits.isComplete(values, taskId))
			{
				completed.add(taskId);
			}
		}
		return completed;
	}

	public boolean isCompletionVarp(int varpId)
	{
		for (int completionVarp : COMPLETION_VARPS)
		{
			if (completionVarp == varpId)
			{
				return true;
			}
		}
		return false;
	}
}
