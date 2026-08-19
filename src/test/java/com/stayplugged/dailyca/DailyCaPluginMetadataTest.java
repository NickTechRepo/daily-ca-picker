package com.stayplugged.dailyca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.stayplugged.dailyca.model.GearProfile;
import java.time.LocalDate;
import net.runelite.api.GameState;
import org.junit.Test;

public class DailyCaPluginMetadataTest
{
	@Test
	public void detectsDateRollover()
	{
		LocalDate today = LocalDate.of(2026, 8, 19);
		assertTrue(DailyCaPlugin.isNewDate(today, today.plusDays(1)));
		assertFalse(DailyCaPlugin.isNewDate(today, today));
		assertTrue(DailyCaPlugin.isNewDate(null, today));
	}


	@Test
	public void clearsAccountStateOnlyAtLoginScreen()
	{
		assertTrue(DailyCaPlugin.shouldClearAccountState(GameState.LOGIN_SCREEN));
		assertFalse(DailyCaPlugin.shouldClearAccountState(GameState.HOPPING));
		assertFalse(DailyCaPlugin.shouldClearAccountState(GameState.LOADING));
	}

	@Test
	public void partialWornSnapshotDoesNotEraseStoredBankCapabilities()
	{
		GearProfile stored = new GearProfile(4, 4, 3, true);
		GearProfile wornOnly = new GearProfile(1, 0, 0, true);

		GearProfile merged = DailyCaPlugin.mergeObservedGear(stored, wornOnly, false);

		assertEquals(4, merged.getMeleeTier());
		assertEquals(4, merged.getRangedTier());
		assertEquals(3, merged.getMagicTier());
		assertTrue(merged.isBankScanned());
	}

	@Test
	public void fullBankSnapshotCanLowerStoredCapabilities()
	{
		GearProfile stored = new GearProfile(4, 4, 3, true);
		GearProfile bankAndWorn = new GearProfile(1, 1, 1, true);

		GearProfile merged = DailyCaPlugin.mergeObservedGear(stored, bankAndWorn, true);

		assertEquals(1, merged.getMeleeTier());
		assertEquals(1, merged.getRangedTier());
		assertEquals(1, merged.getMagicTier());
		assertTrue(merged.isBankScanned());
	}

	@Test
	public void wornOnlySnapshotDoesNotPretendBankWasScanned()
	{
		GearProfile merged = DailyCaPlugin.mergeObservedGear(new GearProfile(0, 0, 0, false),
			new GearProfile(2, 1, 0, true), false);

		assertFalse(merged.isBankScanned());
	}
}
