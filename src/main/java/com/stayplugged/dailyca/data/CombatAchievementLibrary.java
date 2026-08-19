package com.stayplugged.dailyca.data;

import com.google.gson.Gson;
import com.stayplugged.dailyca.model.CaTier;
import com.stayplugged.dailyca.model.CombatAchievement;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CombatAchievementLibrary
{
	private static final String RESOURCE = "/com/stayplugged/dailyca/combat_achievements.json";
	private final List<CombatAchievement> tasks;

	private CombatAchievementLibrary(List<CombatAchievement> tasks)
	{
		this.tasks = Collections.unmodifiableList(new ArrayList<>(tasks));
	}

	public static CombatAchievementLibrary loadBundled(Gson gson)
	{
		try (InputStream stream = CombatAchievementLibrary.class.getResourceAsStream(RESOURCE))
		{
			if (stream == null)
			{
				throw new IllegalStateException("Missing bundled Combat Achievement catalog");
			}
			return load(new InputStreamReader(stream, StandardCharsets.UTF_8), gson);
		}
		catch (Exception ex)
		{
			throw catalogError(ex);
		}
	}

	static CombatAchievementLibrary load(Reader reader, Gson gson)
	{
		try
		{
			CatalogDto catalog = gson.fromJson(reader, CatalogDto.class);
			if (catalog == null || catalog.tasks == null || catalog.taskCount == null
				|| catalog.taskCount != catalog.tasks.size() || catalog.tasks.isEmpty())
			{
				throw new IllegalArgumentException("Catalog count or task list is invalid");
			}

			List<CombatAchievement> tasks = new ArrayList<>(catalog.tasks.size());
			for (int index = 0; index < catalog.tasks.size(); index++)
			{
				TaskDto task = catalog.tasks.get(index);
				if (task == null || task.id != index)
				{
					throw new IllegalArgumentException("Task IDs must be contiguous from zero");
				}
				requireText(task.name, "name", index);
				requireText(task.tier, "tier", index);
				requireText(task.monster, "monster", index);
				requireText(task.type, "type", index);
				requireText(task.description, "description", index);
				requireText(task.wikiUrl, "wikiUrl", index);
				if (!isAllowedWikiUrl(task.wikiUrl))
				{
					throw new IllegalArgumentException("Task " + index + " has an untrusted Wiki URL");
				}
				tasks.add(new CombatAchievement(task.id, task.name,
					CaTier.valueOf(task.tier.toUpperCase()), task.monster, task.type,
					task.description, task.wikiUrl));
			}
			return new CombatAchievementLibrary(tasks);
		}
		catch (Exception ex)
		{
			throw catalogError(ex);
		}
	}

	public List<CombatAchievement> getTasks()
	{
		return tasks;
	}

	public CombatAchievement findById(int id)
	{
		return id >= 0 && id < tasks.size() ? tasks.get(id) : null;
	}

	private static void requireText(String value, String field, int taskId)
	{
		if (value == null || value.trim().isEmpty())
		{
			throw new IllegalArgumentException("Task " + taskId + " is missing " + field);
		}
	}

	private static boolean isAllowedWikiUrl(String value)
	{
		try
		{
			URI uri = new URI(value);
			return "https".equalsIgnoreCase(uri.getScheme())
				&& "oldschool.runescape.wiki".equalsIgnoreCase(uri.getHost())
				&& uri.getUserInfo() == null
				&& (uri.getPort() == -1 || uri.getPort() == 443)
				&& uri.getPath() != null
				&& uri.getPath().startsWith("/w/");
		}
		catch (URISyntaxException | NullPointerException ex)
		{
			return false;
		}
	}

	private static IllegalStateException catalogError(Exception cause)
	{
		return cause instanceof IllegalStateException
			? (IllegalStateException) cause
			: new IllegalStateException("Unable to load Combat Achievement catalog", cause);
	}

	private static final class CatalogDto
	{
		private Integer taskCount;
		private List<TaskDto> tasks;
	}

	private static final class TaskDto
	{
		private int id;
		private String name;
		private String tier;
		private String monster;
		private String type;
		private String description;
		private String wikiUrl;
	}
}
