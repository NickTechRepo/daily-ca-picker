package com.stayplugged.dailyca;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.stayplugged.dailyca.bank.BankGearClassifier;
import com.stayplugged.dailyca.data.CombatAchievementLibrary;
import com.stayplugged.dailyca.model.CombatAchievement;
import com.stayplugged.dailyca.model.CombatStats;
import com.stayplugged.dailyca.model.GearProfile;
import com.stayplugged.dailyca.model.PlayerProfile;
import com.stayplugged.dailyca.progress.CombatAchievementProgressReader;
import com.stayplugged.dailyca.recommendation.Recommendation;
import com.stayplugged.dailyca.recommendation.RecommendationEngine;
import com.stayplugged.dailyca.ui.DailyCaPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.LinkBrowser;

@PluginDescriptor(
	name = "Daily CA Picker",
	description = "Picks one gear- and progression-aware Combat Achievement each day",
	tags = {"combat", "achievement", "ca", "daily", "pvm", "progression", "bank"}
)
public class DailyCaPlugin extends Plugin
{
	private static final String GEAR_MELEE_KEY = "observedGearMelee";
	private static final String GEAR_RANGED_KEY = "observedGearRanged";
	private static final String GEAR_MAGIC_KEY = "observedGearMagic";
	private static final String GEAR_SCANNED_KEY = "observedGearScanned";

	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private ClientToolbar clientToolbar;
	@Inject private ConfigManager configManager;
	@Inject private DailyCaConfig config;
	@Inject private Gson gson;

	private final RecommendationEngine recommendationEngine = new RecommendationEngine();
	private final CombatAchievementProgressReader progressReader = new CombatAchievementProgressReader();
	private final BankGearClassifier gearClassifier = new BankGearClassifier();
	private final Set<Integer> bankItemIds = new HashSet<>();
	private final Set<Integer> wornItemIds = new HashSet<>();

	private CombatAchievementLibrary library;
	private DailyCaPanel panel;
	private NavigationButton navigationButton;
	private GearProfile gearProfile = emptyGearProfile();
	private boolean bankSnapshotLoaded;
	private LocalDate lastRenderedDate;
	private LocalDate lastAnnouncedDate;
	private long lastAnnouncedAccountHash = -1L;

	@Override
	protected void startUp()
	{
		library = CombatAchievementLibrary.loadBundled(gson);
		panel = new DailyCaPanel(LinkBrowser::browse);
		navigationButton = NavigationButton.builder()
			.tooltip("Daily CA Picker")
			.icon(createIcon())
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navigationButton);
		clientThread.invoke(this::initializeCurrentSession);
	}

	@Override
	protected void shutDown()
	{
		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
		}
		navigationButton = null;
		panel = null;
		library = null;
		bankItemIds.clear();
		wornItemIds.clear();
		gearProfile = emptyGearProfile();
		bankSnapshotLoaded = false;
		lastRenderedDate = null;
		lastAnnouncedDate = null;
		lastAnnouncedAccountHash = -1L;
	}

	@Provides
	DailyCaConfig provideConfig(ConfigManager manager)
	{
		return manager.getConfig(DailyCaConfig.class);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (shouldClearAccountState(event.getGameState()))
		{
			bankItemIds.clear();
			wornItemIds.clear();
			gearProfile = emptyGearProfile();
			bankSnapshotLoaded = false;
			lastAnnouncedDate = null;
			lastAnnouncedAccountHash = -1L;
			refresh();
			return;
		}
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		bankItemIds.clear();
		wornItemIds.clear();
		bankSnapshotLoaded = false;
		initializeCurrentSession();
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		bankItemIds.clear();
		wornItemIds.clear();
		gearProfile = emptyGearProfile();
		bankSnapshotLoaded = false;
		lastAnnouncedDate = null;
		lastAnnouncedAccountHash = -1L;
		initializeCurrentSession();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		LocalDate today = LocalDate.now();
		if (isNewDate(lastRenderedDate, today))
		{
			refresh();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.BANK)
		{
			replaceItems(bankItemIds, event.getItemContainer());
			replaceItems(wornItemIds, client.getItemContainer(InventoryID.WORN));
			bankSnapshotLoaded = true;
			updateGearFromObservedItems(true);
		}
		else if (event.getContainerId() == InventoryID.WORN)
		{
			replaceItems(wornItemIds, event.getItemContainer());
			updateGearFromObservedItems(bankSnapshotLoaded);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (progressReader.isCompletionVarp(event.getVarpId()))
		{
			refresh();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!DailyCaConfig.GROUP.equals(event.getGroup()) || isInternalGearKey(event.getKey()))
		{
			return;
		}
		clientThread.invoke(this::refresh);
	}

	private void initializeCurrentSession()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			refresh();
			return;
		}

		loadPersistedGear();
		replaceItems(wornItemIds, client.getItemContainer(InventoryID.WORN));
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank != null)
		{
			replaceItems(bankItemIds, bank);
			bankSnapshotLoaded = true;
		}
		updateGearFromObservedItems(bankSnapshotLoaded);
	}

	private void updateGearFromObservedItems(boolean fullBankSnapshot)
	{
		Set<Integer> allItems = new HashSet<>(bankItemIds);
		allItems.addAll(wornItemIds);
		GearProfile observed = gearClassifier.classify(allItems);
		gearProfile = mergeObservedGear(gearProfile, observed, fullBankSnapshot);
		persistGear();
		refresh();
	}

	static GearProfile mergeObservedGear(GearProfile stored, GearProfile observed, boolean fullBankSnapshot)
	{
		if (fullBankSnapshot)
		{
			return new GearProfile(observed.getMeleeTier(), observed.getRangedTier(),
				observed.getMagicTier(), true);
		}
		if (stored.isBankScanned())
		{
			return new GearProfile(
				Math.max(stored.getMeleeTier(), observed.getMeleeTier()),
				Math.max(stored.getRangedTier(), observed.getRangedTier()),
				Math.max(stored.getMagicTier(), observed.getMagicTier()),
				true);
		}
		return new GearProfile(observed.getMeleeTier(), observed.getRangedTier(),
			observed.getMagicTier(), false);
	}

	private void refresh()
	{
		if (panel == null || library == null)
		{
			return;
		}

		LocalDate today = LocalDate.now();
		lastRenderedDate = today;
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			render(null, today, "Log in to read your Combat Achievement progress.");
			return;
		}

		GearProfile effectiveGear = config.useBankGear()
			? gearProfile
			: new GearProfile(4, 4, 4, true);
		CombatAchievement selected = null;
		String reason;
		if (config.useBankGear() && !effectiveGear.isBankScanned())
		{
			reason = "Open your bank once so the picker can evaluate your best available gear.";
		}
		else
		{
			Set<Integer> completed = progressReader.readCompleted(
				library.getTasks().size(), client::getVarpValue);
			PlayerProfile profile = PlayerProfile.builder()
				.currentTier(config.currentTier())
				.goalTier(config.goalTier())
				.completedTaskIds(completed)
				.gearProfile(effectiveGear)
				.combatStats(readCombatStats())
				.build();
			Recommendation recommendation = recommendationEngine.select(
				library.getTasks(), profile, today, client.getAccountHash());
			selected = recommendation.getTask();
			reason = selected == null
				? "No unfinished task matched your tier, stats, and observed gear."
				: "Selected from unfinished tasks at or below " + titleCase(config.goalTier().name())
					+ ", weighted toward your " + titleCase(config.currentTier().name()) + " progression.";
		}
		render(selected, today, reason);
		announceIfNeeded(selected, today);
	}

	private CombatStats readCombatStats()
	{
		return new CombatStats(
			client.getRealSkillLevel(Skill.ATTACK),
			client.getRealSkillLevel(Skill.STRENGTH),
			client.getRealSkillLevel(Skill.DEFENCE),
			client.getRealSkillLevel(Skill.RANGED),
			client.getRealSkillLevel(Skill.MAGIC),
			client.getRealSkillLevel(Skill.PRAYER),
			client.getRealSkillLevel(Skill.HITPOINTS));
	}

	private void render(CombatAchievement task, LocalDate today, String reason)
	{
		GearProfile shownGear = config.useBankGear() ? gearProfile : new GearProfile(4, 4, 4, true);
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.render(task, shownGear, today, reason);
			}
		});
	}

	private void announceIfNeeded(CombatAchievement task, LocalDate today)
	{
		long accountHash = client.getAccountHash();
		boolean alreadyAnnounced = today.equals(lastAnnouncedDate)
			&& accountHash == lastAnnouncedAccountHash;
		if (task != null && config.announceInChat() && !alreadyAnnounced)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
				"Daily CA: " + task.getName() + " (" + titleCase(task.getTier().name()) + ")", null);
			lastAnnouncedDate = today;
			lastAnnouncedAccountHash = accountHash;
		}
	}

	private void persistGear()
	{
		configManager.setRSProfileConfiguration(DailyCaConfig.GROUP, GEAR_MELEE_KEY, gearProfile.getMeleeTier());
		configManager.setRSProfileConfiguration(DailyCaConfig.GROUP, GEAR_RANGED_KEY, gearProfile.getRangedTier());
		configManager.setRSProfileConfiguration(DailyCaConfig.GROUP, GEAR_MAGIC_KEY, gearProfile.getMagicTier());
		configManager.setRSProfileConfiguration(DailyCaConfig.GROUP, GEAR_SCANNED_KEY,
			gearProfile.isBankScanned());
	}

	private void loadPersistedGear()
	{
		Boolean scanned = configManager.getRSProfileConfiguration(
			DailyCaConfig.GROUP, GEAR_SCANNED_KEY, Boolean.class);
		if (!Boolean.TRUE.equals(scanned))
		{
			gearProfile = emptyGearProfile();
			return;
		}
		gearProfile = new GearProfile(
			readProfileTier(GEAR_MELEE_KEY),
			readProfileTier(GEAR_RANGED_KEY),
			readProfileTier(GEAR_MAGIC_KEY),
			true);
	}

	private int readProfileTier(String key)
	{
		Integer value = configManager.getRSProfileConfiguration(DailyCaConfig.GROUP, key, Integer.class);
		return value == null ? 0 : Math.max(0, Math.min(4, value));
	}

	static boolean shouldClearAccountState(GameState state)
	{
		return state == GameState.LOGIN_SCREEN;
	}

	static boolean isNewDate(LocalDate renderedDate, LocalDate currentDate)
	{
		return renderedDate == null || !renderedDate.equals(currentDate);
	}

	private static boolean isInternalGearKey(String key)
	{
		return GEAR_MELEE_KEY.equals(key) || GEAR_RANGED_KEY.equals(key)
			|| GEAR_MAGIC_KEY.equals(key) || GEAR_SCANNED_KEY.equals(key);
	}

	private static GearProfile emptyGearProfile()
	{
		return new GearProfile(0, 0, 0, false);
	}

	private static void replaceItems(Set<Integer> target, ItemContainer container)
	{
		target.clear();
		if (container == null)
		{
			return;
		}
		for (Item item : container.getItems())
		{
			if (item.getId() >= 0 && item.getQuantity() > 0)
			{
				target.add(item.getId());
			}
		}
	}

	private static BufferedImage createIcon()
	{
		BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(new Color(132, 96, 32));
		graphics.fillOval(1, 1, 30, 30);
		graphics.setColor(Color.WHITE);
		graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		graphics.drawString("CA", 7, 21);
		graphics.dispose();
		return image;
	}

	private static String titleCase(String value)
	{
		return value.substring(0, 1) + value.substring(1).toLowerCase();
	}
}
