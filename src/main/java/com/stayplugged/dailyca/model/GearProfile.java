package com.stayplugged.dailyca.model;

public final class GearProfile
{
	private final int meleeTier;
	private final int rangedTier;
	private final int magicTier;
	private final boolean bankScanned;

	public GearProfile(int meleeTier, int rangedTier, int magicTier, boolean bankScanned)
	{
		this.meleeTier = requireTier(meleeTier);
		this.rangedTier = requireTier(rangedTier);
		this.magicTier = requireTier(magicTier);
		this.bankScanned = bankScanned;
	}

	private static int requireTier(int value)
	{
		if (value < 0 || value > 4)
		{
			throw new IllegalArgumentException("Gear capability tier must be between 0 and 4");
		}
		return value;
	}

	public static GearProfile midLevel()
	{
		return new GearProfile(2, 2, 2, true);
	}

	public int getMeleeTier() { return meleeTier; }
	public int getRangedTier() { return rangedTier; }
	public int getMagicTier() { return magicTier; }
	public boolean isBankScanned() { return bankScanned; }
}
