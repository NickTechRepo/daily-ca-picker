package com.stayplugged.dailyca.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class PlayerProfile
{
	private final CaTier currentTier;
	private final CaTier goalTier;
	private final Set<Integer> completedTaskIds;
	private final GearProfile gearProfile;
	private final CombatStats combatStats;

	private PlayerProfile(Builder builder)
	{
		this.currentTier = Objects.requireNonNull(builder.currentTier, "currentTier");
		this.goalTier = Objects.requireNonNull(builder.goalTier, "goalTier");
		this.completedTaskIds = Collections.unmodifiableSet(new HashSet<>(
			Objects.requireNonNull(builder.completedTaskIds, "completedTaskIds")));
		this.gearProfile = Objects.requireNonNull(builder.gearProfile, "gearProfile");
		this.combatStats = Objects.requireNonNull(builder.combatStats, "combatStats");
	}

	public static Builder builder() { return new Builder(); }
	public CaTier getCurrentTier() { return currentTier; }
	public CaTier getGoalTier() { return goalTier; }
	public Set<Integer> getCompletedTaskIds() { return completedTaskIds; }
	public GearProfile getGearProfile() { return gearProfile; }
	public CombatStats getCombatStats() { return combatStats; }

	public static final class Builder
	{
		private CaTier currentTier = CaTier.EASY;
		private CaTier goalTier = CaTier.EASY;
		private Set<Integer> completedTaskIds = Collections.emptySet();
		private GearProfile gearProfile = new GearProfile(0, 0, 0, false);
		private CombatStats combatStats = new CombatStats(99, 99, 99, 99, 99, 99, 99);

		public Builder currentTier(CaTier value) { currentTier = value; return this; }
		public Builder goalTier(CaTier value) { goalTier = value; return this; }
		public Builder completedTaskIds(Set<Integer> value) { completedTaskIds = value; return this; }
		public Builder gearProfile(GearProfile value) { gearProfile = value; return this; }
		public Builder combatStats(CombatStats value) { combatStats = value; return this; }
		public PlayerProfile build() { return new PlayerProfile(this); }
	}
}
