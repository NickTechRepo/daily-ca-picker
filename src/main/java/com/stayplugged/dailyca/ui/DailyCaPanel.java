package com.stayplugged.dailyca.ui;

import com.stayplugged.dailyca.model.CombatAchievement;
import com.stayplugged.dailyca.model.GearProfile;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public final class DailyCaPanel extends PluginPanel
{
	private static final Color PARCHMENT_LIGHT = new Color(232, 207, 145);
	private static final Color PARCHMENT_DARK = new Color(183, 139, 67);
	private static final Color INK = new Color(58, 37, 18);
	private static final Color MUTED_INK = new Color(100, 68, 32);
	private static final Color GOLD = new Color(229, 184, 72);
	private static final Color SEAL_RED = new Color(117, 30, 25);
	private static final String WAITING_TEXT = "The Combat Ledger awaits...";

	private final JLabel dateLabel = centeredLabel("", GOLD, Font.BOLD, 12);
	private final JLabel scrollTitle = centeredLabel("DAILY COMBAT ACHIEVEMENT", INK, Font.BOLD, 13);
	private final JLabel statusLabel = centeredLabel(WAITING_TEXT, MUTED_INK, Font.ITALIC, 12);
	private final JLabel nameLabel = centeredLabel("", INK, Font.BOLD, 15);
	private final JLabel tierLabel = centeredLabel("", MUTED_INK, Font.BOLD, 11);
	private final JLabel bossLabel = centeredLabel("", INK, Font.BOLD, 12);
	private final JLabel descriptionLabel = new JLabel("");
	private final JLabel reasonLabel = new JLabel("");
	private final JLabel bankLabel = centeredLabel("Open your bank once to scan gear.", ColorScheme.LIGHT_GRAY_COLOR,
		Font.PLAIN, 10);
	private final JPanel taskContent = transparentVerticalPanel();
	private final JButton rollButton = new JButton("ROLL TODAY'S CA");
	private final JButton wikiButton = new JButton("Wiki guide");
	private final Consumer<String> openUrl;
	private final RevealListener onReveal;

	private Timer revealTimer;
	private CombatAchievement renderedTask;
	private LocalDate renderedDate;
	private long renderedSessionGeneration;
	private String wikiUrl = "";
	private boolean revealed;
	private int revealFrame;

	public DailyCaPanel(Consumer<String> openUrl)
	{
		this(openUrl, (ignoredTask, ignoredDate, ignoredGeneration) -> { });
	}

	public DailyCaPanel(Consumer<String> openUrl, BiConsumer<CombatAchievement, LocalDate> onReveal)
	{
		this(openUrl, (task, date, ignoredGeneration) -> onReveal.accept(task, date));
	}

	public DailyCaPanel(Consumer<String> openUrl, RevealListener onReveal)
	{
		this.openUrl = openUrl;
		this.onReveal = onReveal;
		setLayout(new BorderLayout(0, 8));
		setBorder(new EmptyBorder(10, 8, 10, 8));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel heading = new JPanel(new BorderLayout());
		heading.setOpaque(false);
		JLabel headingLabel = centeredLabel("✦  ADVENTURER'S DECREE  ✦", GOLD, Font.BOLD, 12);
		heading.add(headingLabel, BorderLayout.NORTH);
		heading.add(dateLabel, BorderLayout.SOUTH);
		add(heading, BorderLayout.NORTH);

		ParchmentPanel parchment = new ParchmentPanel();
		parchment.setLayout(new BoxLayout(parchment, BoxLayout.Y_AXIS));
		parchment.setBorder(new EmptyBorder(25, 17, 27, 17));
		parchment.add(scrollTitle);
		parchment.add(Box.createVerticalStrut(7));
		parchment.add(new OrnamentalDivider());
		parchment.add(Box.createVerticalStrut(10));
		parchment.add(statusLabel);
		parchment.add(Box.createVerticalStrut(4));

		descriptionLabel.setForeground(INK);
		descriptionLabel.setAlignmentX(CENTER_ALIGNMENT);
		reasonLabel.setForeground(MUTED_INK);
		reasonLabel.setFont(reasonLabel.getFont().deriveFont(Font.ITALIC, 10f));
		reasonLabel.setAlignmentX(CENTER_ALIGNMENT);
		taskContent.add(nameLabel);
		taskContent.add(Box.createVerticalStrut(4));
		taskContent.add(tierLabel);
		taskContent.add(Box.createVerticalStrut(4));
		taskContent.add(bossLabel);
		taskContent.add(Box.createVerticalStrut(9));
		taskContent.add(descriptionLabel);
		taskContent.add(Box.createVerticalStrut(8));
		taskContent.add(new OrnamentalDivider());
		taskContent.add(Box.createVerticalStrut(8));
		taskContent.add(reasonLabel);
		parchment.add(taskContent);

		JPanel center = new JPanel(new BorderLayout(0, 6));
		center.setOpaque(false);
		center.add(parchment, BorderLayout.CENTER);
		center.add(bankLabel, BorderLayout.SOUTH);
		add(center, BorderLayout.CENTER);

		styleRollButton();
		rollButton.addActionListener(this::startReveal);
		wikiButton.setEnabled(false);
		wikiButton.addActionListener(event ->
		{
			if (revealed && !wikiUrl.isEmpty())
			{
				openUrl.accept(wikiUrl);
			}
		});

		JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 5));
		buttons.setOpaque(false);
		buttons.add(rollButton);
		buttons.add(wikiButton);
		add(buttons, BorderLayout.SOUTH);
		showWaitingState();
	}

	public void render(CombatAchievement task, GearProfile gear, LocalDate date, String reason)
	{
		render(task, gear, date, reason, 0L);
	}

	public void render(CombatAchievement task, GearProfile gear, LocalDate date, String reason,
		long sessionGeneration)
	{
		boolean assignmentChanged = !Objects.equals(renderedDate, date)
			|| taskId(renderedTask) != taskId(task)
			|| renderedSessionGeneration != sessionGeneration;
		renderedTask = task;
		renderedDate = date;
		renderedSessionGeneration = sessionGeneration;
		dateLabel.setText(date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));

		if (task == null)
		{
			cancelRevealTimer();
			nameLabel.setText("Daily CA unavailable");
			tierLabel.setText("");
			bossLabel.setText("");
			descriptionLabel.setText(html(reason == null || reason.isEmpty()
				? "No suitable unfinished task found."
				: reason, 168));
			reasonLabel.setText("");
			wikiUrl = "";
			setRevealed(true);
			statusLabel.setText("The ledger is silent.");
			rollButton.setText("NO CHALLENGE AVAILABLE");
			rollButton.setEnabled(false);
		}
		else
		{
			nameLabel.setText(plain(task.getName()));
			tierLabel.setText(plain("◆ " + titleCase(task.getTier().name()) + " · " + task.getType() + " ◆"));
			bossLabel.setText(plain(task.getMonster()));
			descriptionLabel.setText(html(task.getDescription(), 168));
			reasonLabel.setText(html(reason, 168));
			wikiUrl = isAllowedWikiUrl(task.getWikiUrl()) ? task.getWikiUrl() : "";
			if (assignmentChanged)
			{
				showWaitingState();
			}
			else if (revealed)
			{
				wikiButton.setEnabled(!wikiUrl.isEmpty());
			}
		}

		bankLabel.setText(gear.isBankScanned()
			? "Bank scanned · Melee " + gear.getMeleeTier() + " / Ranged " + gear.getRangedTier()
				+ " / Magic " + gear.getMagicTier()
			: "Open your bank once to scan gear.");
		revalidate();
		repaint();
	}

	private void startReveal(ActionEvent event)
	{
		if (renderedTask == null || revealed || revealTimer != null)
		{
			return;
		}

		revealFrame = 0;
		rollButton.setEnabled(false);
		rollButton.setText("CONSULTING THE LEDGER...");
		statusLabel.setText("The fates are choosing");
		revealTimer = new Timer(145, ignored ->
		{
			revealFrame++;
			statusLabel.setText("The fates are choosing" + dots(revealFrame));
			if (revealFrame >= 8)
			{
				finishReveal();
			}
		});
		revealTimer.setInitialDelay(0);
		revealTimer.start();
	}

	private void finishReveal()
	{
		cancelRevealTimer();
		if (revealed || renderedTask == null)
		{
			return;
		}
		setRevealed(true);
		statusLabel.setText("Your challenge has been decreed!");
		rollButton.setText("TODAY'S CA REVEALED");
		rollButton.setEnabled(false);
		wikiButton.setEnabled(!wikiUrl.isEmpty());
		onReveal.onReveal(renderedTask, renderedDate, renderedSessionGeneration);
		revalidate();
		repaint();
	}

	private void showWaitingState()
	{
		cancelRevealTimer();
		setRevealed(false);
		statusLabel.setText(WAITING_TEXT);
		rollButton.setText("ROLL TODAY'S CA");
		rollButton.setEnabled(renderedTask != null);
		wikiButton.setEnabled(false);
	}

	private void setRevealed(boolean value)
	{
		revealed = value;
		taskContent.setVisible(value);
	}

	private void styleRollButton()
	{
		rollButton.setBackground(SEAL_RED);
		rollButton.setForeground(new Color(255, 222, 133));
		rollButton.setFont(rollButton.getFont().deriveFont(Font.BOLD, 12f));
		rollButton.setFocusPainted(false);
		rollButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		rollButton.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(213, 161, 60), 2),
			new EmptyBorder(7, 5, 7, 5)));
	}

	public String getDisplayedTextForTest()
	{
		String taskText = revealed
			? String.join(" ", nameLabel.getText(), tierLabel.getText(), bossLabel.getText(),
				descriptionLabel.getText(), reasonLabel.getText())
			: "";
		return String.join(" ", dateLabel.getText(), scrollTitle.getText(), statusLabel.getText(),
			taskText, bankLabel.getText(), rollButton.getText());
	}

	boolean isRevealedForTest()
	{
		return revealed;
	}

	void revealImmediatelyForTest()
	{
		if (renderedTask != null)
		{
			finishReveal();
		}
	}

	void startRevealForTest()
	{
		startReveal(null);
	}

	boolean isRevealTimerRunningForTest()
	{
		return revealTimer != null && revealTimer.isRunning();
	}

	public void resetTransientState()
	{
		cancelRevealTimer();
		renderedTask = null;
		renderedDate = null;
		renderedSessionGeneration = 0L;
		wikiUrl = "";
		showWaitingState();
	}

	public void dispose()
	{
		cancelRevealTimer();
	}

	private void cancelRevealTimer()
	{
		if (revealTimer != null)
		{
			revealTimer.stop();
			revealTimer = null;
		}
	}

	static boolean isAllowedWikiUrl(String value)
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

	private static JLabel centeredLabel(String text, Color color, int style, int size)
	{
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setForeground(color);
		label.setFont(label.getFont().deriveFont(style, (float) size));
		label.setAlignmentX(CENTER_ALIGNMENT);
		return label;
	}

	private static JPanel transparentVerticalPanel()
	{
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		return panel;
	}

	private static String dots(int frame)
	{
		StringBuilder value = new StringBuilder();
		for (int i = 0; i < frame % 4; i++)
		{
			value.append('.');
		}
		return value.toString();
	}

	private static int taskId(CombatAchievement task)
	{
		return task == null ? -1 : task.getId();
	}

	private static String html(String value, int width)
	{
		String safe = value == null ? "" : value;
		return "<html><body style='width: " + width + "px; text-align: center'>"
			+ escapeHtml(safe) + "</body></html>";
	}

	private static String plain(String value)
	{
		return value.regionMatches(true, 0, "<html", 0, 5) ? "\u200B" + value : value;
	}

	private static String escapeHtml(String value)
	{
		return value.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
			.replace("'", "&#39;");
	}

	private static String titleCase(String value)
	{
		return value.substring(0, 1) + value.substring(1).toLowerCase();
	}

	private static final class ParchmentPanel extends JPanel
	{
		private ParchmentPanel()
		{
			setOpaque(false);
			setMinimumSize(new Dimension(190, 235));
			setPreferredSize(new Dimension(190, 235));
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int width = getWidth();
			int height = getHeight();

			g.setColor(new Color(0, 0, 0, 85));
			g.fillRoundRect(5, 8, width - 10, height - 12, 18, 18);
			g.setPaint(new GradientPaint(0, 0, PARCHMENT_LIGHT, width, height, PARCHMENT_DARK));
			g.fillRoundRect(2, 3, width - 8, height - 10, 18, 18);

			g.setColor(new Color(113, 75, 31, 100));
			for (int y = 20; y < height - 20; y += 18)
			{
				g.drawLine(12, y, width - 18, y);
			}

			g.setStroke(new BasicStroke(2f));
			g.setColor(new Color(92, 57, 24));
			g.drawRoundRect(2, 3, width - 8, height - 10, 18, 18);

			g.setPaint(new GradientPaint(0, 0, new Color(210, 170, 92), width, 0,
				new Color(151, 101, 43)));
			g.fillRoundRect(0, 1, width - 4, 20, 12, 12);
			g.fillRoundRect(0, height - 25, width - 4, 20, 12, 12);
			g.setColor(new Color(83, 49, 20));
			g.drawRoundRect(0, 1, width - 4, 20, 12, 12);
			g.drawRoundRect(0, height - 25, width - 4, 20, 12, 12);
			g.dispose();

			super.paintComponent(graphics);
		}
	}

	private static final class OrnamentalDivider extends JPanel
	{
		private OrnamentalDivider()
		{
			setOpaque(false);
			setMaximumSize(new Dimension(Integer.MAX_VALUE, 9));
			setPreferredSize(new Dimension(160, 9));
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int middle = getWidth() / 2;
			g.setColor(new Color(111, 72, 29));
			g.drawLine(5, 4, middle - 8, 4);
			g.drawLine(middle + 8, 4, getWidth() - 5, 4);
			g.fillRect(middle - 3, 1, 6, 6);
			g.dispose();
		}
	}

	@FunctionalInterface
	public interface RevealListener
	{
		void onReveal(CombatAchievement task, LocalDate assignmentDate, long sessionGeneration);
	}
}
