package com.stayplugged.dailyca.model;

public enum CaTier
{
	EASY(1),
	MEDIUM(2),
	HARD(3),
	ELITE(4),
	MASTER(5),
	GRANDMASTER(6);

	private final int points;

	CaTier(int points)
	{
		this.points = points;
	}

	public int getPoints()
	{
		return points;
	}
}
