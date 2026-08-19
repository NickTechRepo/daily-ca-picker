package com.stayplugged.dailyca.ui;

import com.stayplugged.dailyca.model.CombatAchievement;
import com.stayplugged.dailyca.model.GearProfile;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public final class DailyCaPanel extends PluginPanel
{
	private final JLabel dateLabel = new JLabel("", SwingConstants.CENTER);
	private final JLabel nameLabel = new JLabel("No task selected", SwingConstants.CENTER);
	private final JLabel tierLabel = new JLabel("", SwingConstants.CENTER);
	private final JLabel bossLabel = new JLabel("", SwingConstants.CENTER);
	private final JLabel descriptionLabel = new JLabel("");
	private final JLabel reasonLabel = new JLabel("");
	private final JLabel bankLabel = new JLabel("Open your bank once to scan gear.");
	private final Consumer<String> openUrl;
	private String wikiUrl = "";

	public DailyCaPanel(Consumer<String> openUrl)
	{
		this.openUrl = openUrl;
		setLayout(new BorderLayout(0, 8));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel content = new JPanel(new GridLayout(0, 1, 0, 6));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		content.add(dateLabel);
		content.add(nameLabel);
		content.add(tierLabel);
		content.add(bossLabel);
		content.add(descriptionLabel);
		content.add(reasonLabel);
		content.add(bankLabel);
		add(content, BorderLayout.NORTH);

		JPanel buttons = new JPanel(new GridLayout(1, 1, 6, 0));
		JButton wiki = new JButton("Wiki guide");
		wiki.addActionListener(event ->
		{
			if (!wikiUrl.isEmpty())
			{
				openUrl.accept(wikiUrl);
			}
		});
		buttons.add(wiki);
		add(buttons, BorderLayout.SOUTH);
	}

	public void render(CombatAchievement task, GearProfile gear, LocalDate date, String reason)
	{
		dateLabel.setText("Daily challenge · " + date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		if (task == null)
		{
			nameLabel.setText("Daily CA unavailable");
			tierLabel.setText("");
			bossLabel.setText("");
			descriptionLabel.setText(html(reason == null || reason.isEmpty()
				? "No suitable unfinished task found."
				: reason));
			reasonLabel.setText("");
			wikiUrl = "";
		}
		else
		{
			nameLabel.setText(plain(task.getName()));
			tierLabel.setText(plain(titleCase(task.getTier().name()) + " · " + task.getType()));
			bossLabel.setText(plain(task.getMonster()));
			descriptionLabel.setText(html(task.getDescription()));
			reasonLabel.setText(html(reason));
			wikiUrl = isAllowedWikiUrl(task.getWikiUrl()) ? task.getWikiUrl() : "";
		}
		bankLabel.setText(gear.isBankScanned()
			? "Bank scanned · Melee " + gear.getMeleeTier() + " / Ranged " + gear.getRangedTier()
				+ " / Magic " + gear.getMagicTier()
			: "Open your bank once to scan gear.");
		revalidate();
		repaint();
	}

	public String getDisplayedTextForTest()
	{
		return String.join(" ", dateLabel.getText(), nameLabel.getText(), tierLabel.getText(),
			bossLabel.getText(), descriptionLabel.getText(), reasonLabel.getText(), bankLabel.getText());
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

	private static String html(String value)
	{
		return "<html><body style='width: 190px'>" + escapeHtml(value) + "</body></html>";
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
}
