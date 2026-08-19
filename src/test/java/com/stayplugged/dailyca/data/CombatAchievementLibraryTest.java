package com.stayplugged.dailyca.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.Gson;
import com.stayplugged.dailyca.model.CaTier;
import com.stayplugged.dailyca.model.CombatAchievement;
import java.io.StringReader;
import org.junit.Test;

public class CombatAchievementLibraryTest
{
	@Test
	public void loadsCompleteBundledCatalogWithStableGameIds()
	{
		CombatAchievementLibrary library = CombatAchievementLibrary.loadBundled(new Gson());

		assertEquals(646, library.getTasks().size());
		CombatAchievement vardorvis = library.findById(486);
		assertNotNull(vardorvis);
		assertEquals("Vardorvis Speed-Chaser", vardorvis.getName());
		assertEquals(CaTier.MASTER, vardorvis.getTier());
		String[] finalNames = {"Maggot King Speed Chaser", "Trying to fit in", "King-sized clobbering",
			"Digging in", "Cordoned Off", "Perfect Maggot King"};
		for (int offset = 0; offset < finalNames.length; offset++)
		{
			CombatAchievement task = library.findById(640 + offset);
			assertNotNull(task);
			assertEquals(finalNames[offset], task.getName());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void rejectsCatalogWithNonContiguousIds()
	{
		String json = "{\"taskCount\":2,\"tasks\":["
			+ "{\"id\":0,\"name\":\"A\",\"tier\":\"Easy\",\"monster\":\"M\",\"type\":\"T\","
			+ "\"description\":\"D\",\"wikiUrl\":\"https://oldschool.runescape.wiki/w/A\"},"
			+ "{\"id\":2,\"name\":\"B\",\"tier\":\"Hard\",\"monster\":\"M\",\"type\":\"T\","
			+ "\"description\":\"D\",\"wikiUrl\":\"https://oldschool.runescape.wiki/w/B\"}]}";
		CombatAchievementLibrary.load(new StringReader(json), new Gson());
	}

	@Test(expected = IllegalStateException.class)
	public void rejectsCatalogWithUntrustedUrl()
	{
		String json = "{\"taskCount\":1,\"tasks\":["
			+ "{\"id\":0,\"name\":\"A\",\"tier\":\"Easy\",\"monster\":\"M\",\"type\":\"T\","
			+ "\"description\":\"D\",\"wikiUrl\":\"https://evil.example/A\"}]}";
		CombatAchievementLibrary.load(new StringReader(json), new Gson());
	}
}
