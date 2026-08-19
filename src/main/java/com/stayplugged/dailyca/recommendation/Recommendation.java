package com.stayplugged.dailyca.recommendation;

import com.stayplugged.dailyca.model.CombatAchievement;

public final class Recommendation
{
	private final CombatAchievement task;

	public Recommendation(CombatAchievement task)
	{
		this.task = task;
	}

	public CombatAchievement getTask()
	{
		return task;
	}
}
