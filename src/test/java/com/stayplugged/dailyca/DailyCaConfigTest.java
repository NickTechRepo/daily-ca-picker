package com.stayplugged.dailyca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.stayplugged.dailyca.model.CaTier;
import org.junit.Test;

public class DailyCaConfigTest
{
	@Test
	public void defaultsToHardProgressingTowardEliteWithBankFiltering()
	{
		DailyCaConfig config = new DailyCaConfig() { };

		assertEquals(CaTier.HARD, config.currentTier());
		assertEquals(CaTier.ELITE, config.goalTier());
		assertTrue(config.useBankGear());
	}
}
