package com.stayplugged.dailyca.model;

public final class CombatStats
{
	private final int attack;
	private final int strength;
	private final int defence;
	private final int ranged;
	private final int magic;
	private final int prayer;
	private final int hitpoints;

	public CombatStats(int attack, int strength, int defence, int ranged, int magic, int prayer, int hitpoints)
	{
		this.attack = attack;
		this.strength = strength;
		this.defence = defence;
		this.ranged = ranged;
		this.magic = magic;
		this.prayer = prayer;
		this.hitpoints = hitpoints;
	}

	public int getAttack() { return attack; }
	public int getStrength() { return strength; }
	public int getDefence() { return defence; }
	public int getRanged() { return ranged; }
	public int getMagic() { return magic; }
	public int getPrayer() { return prayer; }
	public int getHitpoints() { return hitpoints; }
}
