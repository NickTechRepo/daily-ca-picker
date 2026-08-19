package com.stayplugged.dailyca.model;

import org.junit.Test;

public class PlayerProfileTest
{
	@Test(expected = IllegalArgumentException.class)
	public void rejectsOutOfRangeGearCapability()
	{
		new GearProfile(5, 0, 0, true);
	}

	@Test(expected = NullPointerException.class)
	public void rejectsNullTier()
	{
		PlayerProfile.builder().currentTier(null).build();
	}

	@Test(expected = NullPointerException.class)
	public void rejectsNullCompletedTaskSet()
	{
		PlayerProfile.builder().completedTaskIds(null).build();
	}
}
