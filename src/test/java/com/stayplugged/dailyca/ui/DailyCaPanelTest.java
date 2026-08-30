package com.stayplugged.dailyca.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.stayplugged.dailyca.model.CaTier;
import com.stayplugged.dailyca.model.CombatAchievement;
import com.stayplugged.dailyca.model.GearProfile;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
		panel.revealImmediatelyForTest();

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
		panel.revealImmediatelyForTest();

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

	@Test
	public void concealsTaskUntilPlayerRollsTheDailyChallenge()
	{
		DailyCaPanel panel = new DailyCaPanel(ignored -> { });
		panel.render(new CombatAchievement(3, "Secret Challenge", CaTier.MASTER, "Zulrah",
			"Mechanical", "Complete the hidden challenge.",
			"https://oldschool.runescape.wiki/w/Zulrah"),
			new GearProfile(4, 4, 4, true), LocalDate.of(2026, 8, 19), "Daily selection.");

		assertFalse(panel.isRevealedForTest());
		assertFalse(panel.getDisplayedTextForTest().contains("Secret Challenge"));
		assertTrue(panel.getDisplayedTextForTest().contains("ROLL TODAY'S CA"));

		panel.revealImmediatelyForTest();

		assertTrue(panel.isRevealedForTest());
		assertTrue(panel.getDisplayedTextForTest().contains("Secret Challenge"));
		assertTrue(panel.getDisplayedTextForTest().contains("TODAY'S CA REVEALED"));
	}

	@Test
	public void sameAssignmentRefreshDoesNotHideAnAlreadyRevealedScroll()
	{
		CombatAchievement task = new CombatAchievement(4, "Steady Challenge", CaTier.ELITE, "Vorkath",
			"Mechanical", "Complete the challenge.", "https://oldschool.runescape.wiki/w/Vorkath");
		DailyCaPanel panel = new DailyCaPanel(ignored -> { });
		LocalDate date = LocalDate.of(2026, 8, 19);
		panel.render(task, new GearProfile(3, 3, 3, true), date, "Initial selection.");
		panel.revealImmediatelyForTest();

		panel.render(task, new GearProfile(4, 4, 4, true), date, "Refreshed gear.");

		assertTrue(panel.isRevealedForTest());
		assertTrue(panel.getDisplayedTextForTest().contains("Steady Challenge"));
	}

	@Test
	public void announcesOnlyWhenTheScrollIsRevealed()
	{
		AtomicInteger announcements = new AtomicInteger();
		AtomicReference<LocalDate> announcedDate = new AtomicReference<>();
		DailyCaPanel panel = new DailyCaPanel(ignored -> { }, (ignored, date) ->
		{
			announcements.incrementAndGet();
			announcedDate.set(date);
		});
		LocalDate assignmentDate = LocalDate.of(2026, 8, 19);
		panel.render(new CombatAchievement(5, "Announced Challenge", CaTier.HARD, "Barrows",
			"Mechanical", "Complete the challenge.", "https://oldschool.runescape.wiki/w/Barrows"),
			new GearProfile(2, 2, 2, true), assignmentDate, "Daily selection.");

		assertTrue(announcements.get() == 0);
		panel.revealImmediatelyForTest();
		assertTrue(announcements.get() == 1);
		assertTrue(assignmentDate.equals(announcedDate.get()));
		panel.revealImmediatelyForTest();
		assertTrue(announcements.get() == 1);
	}

	@Test
	public void nullTaskCancelsAnActiveRevealTimer()
	{
		DailyCaPanel panel = new DailyCaPanel(ignored -> { });
		LocalDate date = LocalDate.of(2026, 8, 19);
		panel.render(new CombatAchievement(7, "Interrupted Challenge", CaTier.HARD, "Boss",
			"Mechanical", "Complete the challenge.", ""),
			new GearProfile(2, 2, 2, true), date, "Daily selection.");
		panel.startRevealForTest();
		assertTrue(panel.isRevealTimerRunningForTest());

		panel.render(null, new GearProfile(0, 0, 0, false), date, "Log in to continue.");

		assertFalse(panel.isRevealTimerRunningForTest());
		assertTrue(panel.getDisplayedTextForTest().contains("The ledger is silent."));
	}

	@Test
	public void accountResetHidesSameTaskAndAllowsAnotherReveal()
	{
		CombatAchievement task = new CombatAchievement(8, "Shared Challenge", CaTier.ELITE, "Boss",
			"Mechanical", "Complete the challenge.", "");
		DailyCaPanel panel = new DailyCaPanel(ignored -> { });
		LocalDate date = LocalDate.of(2026, 8, 19);
		panel.render(task, new GearProfile(3, 3, 3, true), date, "First account.");
		panel.revealImmediatelyForTest();
		assertTrue(panel.isRevealedForTest());

		panel.resetTransientState();
		panel.render(task, new GearProfile(3, 3, 3, true), date, "Second account.");

		assertFalse(panel.isRevealedForTest());
		assertTrue(panel.getDisplayedTextForTest().contains("ROLL TODAY'S CA"));
	}

	@Test
	public void paintsWaitingAndRevealedParchmentWithoutError()
	{
		DailyCaPanel panel = new DailyCaPanel(ignored -> { });
		panel.setSize(225, 520);
		panel.doLayout();
		panel.render(new CombatAchievement(6, "Painted Challenge", CaTier.MASTER, "Theatre of Blood",
			"Perfection", "Complete the challenge without taking avoidable damage.",
			"https://oldschool.runescape.wiki/w/Theatre_of_Blood"),
			new GearProfile(4, 4, 4, true), LocalDate.of(2026, 8, 19), "Daily selection.");

		BufferedImage image = new BufferedImage(225, 520, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		panel.printAll(graphics);
		panel.revealImmediatelyForTest();
		panel.doLayout();
		panel.printAll(graphics);
		graphics.dispose();

		assertTrue(panel.isRevealedForTest());
	}
}
