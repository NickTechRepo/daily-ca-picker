package com.stayplugged.dailyca.model;

import java.util.Objects;

public final class CombatAchievement
{
	private final int id;
	private final String name;
	private final CaTier tier;
	private final String monster;
	private final String type;
	private final String description;
	private final String wikiUrl;

	public CombatAchievement(int id, String name, CaTier tier, String monster, String type, String description)
	{
		this(id, name, tier, monster, type, description, "");
	}

	public CombatAchievement(int id, String name, CaTier tier, String monster, String type, String description,
		String wikiUrl)
	{
		this.id = id;
		this.name = Objects.requireNonNull(name);
		this.tier = Objects.requireNonNull(tier);
		this.monster = monster == null ? "" : monster;
		this.type = type == null ? "" : type;
		this.description = description == null ? "" : description;
		this.wikiUrl = wikiUrl == null ? "" : wikiUrl;
	}

	public int getId() { return id; }
	public String getName() { return name; }
	public CaTier getTier() { return tier; }
	public String getMonster() { return monster; }
	public String getType() { return type; }
	public String getDescription() { return description; }
	public String getWikiUrl() { return wikiUrl; }
}
