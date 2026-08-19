package com.stayplugged.dailyca.progress;

public final class CombatAchievementBits
{
	private CombatAchievementBits()
	{
	}

	public static boolean isComplete(int[] varpValues, int taskId)
	{
		if (taskId < 0 || taskId / 32 >= varpValues.length)
		{
			return false;
		}
		return (varpValues[taskId / 32] & (1 << (taskId % 32))) != 0;
	}
}
