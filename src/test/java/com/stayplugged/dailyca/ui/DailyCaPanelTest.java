package com.stayplugged.dailyca.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.stayplugged.dailyca.model.CaTier;
import com.stayplugged.dailyca.model.CombatAchievement;
import com.stayplugged.dailyca.model.GearProfile;
import java.time.LocalDate;
import org.junit.Test;

public class DailyCaPanelTest
{
	@Test
	public void rendersDailyTaskAndBankStatus()
	{
		DailyCaPanel panel = new DailyCaPanel(ignored -> { });
		panel.render(new CombatAchievement(1, "A Hard Day", CaTier.HARD, "Giant Mole",
			"Mechanical", "Defeat the Giant Mole without taking damage.", "https://oldschool.runescape.wiki/"),
			new GearProfile(2, 4, 2, true), LocalDate.of(2026, 8, 19),
			"Matches your Hard progression and observed bank gear.");

		String text = panel.getDisplayedTextForTest();
		assertTrue(text.contains("A Hard Day"));
		assertTrue(text.contains("Hard"));
		assertTrue(text.contains("Giant Mole"));
		assertTrue(text.contains("Bank scanned"));
	}

	@Test
	public void escapesMarkupCharactersInWikiDescription()
	{
		DailyCaPanel panel = new DailyCaPanel(ignored -> { });
		panel.render(new CombatAchievement(2, "Speed", CaTier.ELITE, "Boss",
			"Speed", "Kill in < 2:30 & take no damage.", ""),
			new GearProfile(2, 2, 2, true), LocalDate.of(2026, 8, 19), "Reason & fit");

		String text = panel.getDisplayedTextForTest();
		assertTrue(text.contains("&lt; 2:30 &amp; take no damage"));
		assertTrue(text.contains("Reason &amp; fit"));
	}

	@Test
	public void displaysCallerGuidanceWhenNoTaskIsAvailable()
	{
		DailyCaPanel panel = new DailyCaPanel(ignored -> { });
		panel.render(null, new GearProfile(0, 0, 0, false), LocalDate.of(2026, 8, 19),
			"Log in to read your Combat Achievement progress.");

		String text = panel.getDisplayedTextForTest();
		assertTrue(text.contains("Log in to read your Combat Achievement progress."));
		assertFalse(text.contains("No unfinished task found"));
	}

	@Test
	public void onlyAllowsOfficialHttpsWikiUrls()
	{
		assertTrue(DailyCaPanel.isAllowedWikiUrl("https://oldschool.runescape.wiki/w/Vardorvis"));
		assertFalse(DailyCaPanel.isAllowedWikiUrl("http://oldschool.runescape.wiki/w/Vardorvis"));
		assertFalse(DailyCaPanel.isAllowedWikiUrl("https://oldschool.runescape.wiki.evil.example/steal"));
		assertFalse(DailyCaPanel.isAllowedWikiUrl("javascript:alert(1)"));
	}
}
