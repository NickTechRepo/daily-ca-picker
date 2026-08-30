# Daily CA Picker

A RuneLite Plugin Hub plugin that selects one unfinished Combat Achievement per day while considering:

- your configured current Combat Achievement tier;
- the tier you are progressing toward;
- your live combat skill levels;
- the strongest relevant gear observed in your bank and equipment; and
- Combat Achievements already marked complete by the game.

For unchanged completion state, configuration, stats, and observed gear, the same account receives one stable task for the day. The task is recalculated when those inputs change.

## Setup

1. Enable **Daily CA Picker**.
2. Choose **Current CA tier** and **Goal CA tier** in RuneLite settings.
3. Equipped gear is observed immediately. Open your bank once so RuneLite supplies a complete bank snapshot; the plugin stores only three numeric capability scores: melee, ranged, and magic.
4. Open the sidebar panel and unfurl the OSRS-style scroll with **Roll Today's CA**. The reveal is visual only: your account still receives one stable assignment for that calendar day.
5. Use **Wiki guide** after the reveal to open the task's OSRS Wiki page.

Bank-aware filtering activates only after a real bank snapshot has been received (or a prior profile-scoped scan exists). The panel shows the current scan state and never claims that an unopened bank was inspected.

## Privacy

- The plugin makes no background HTTP/API requests and has no external service.
- Clicking **Wiki guide** explicitly opens an allowlisted HTTPS OSRS Wiki page in your browser; normal browser request data is then sent to the Wiki.
- Bank contents, item IDs, quantities, account hashes, and character names are not persisted.
- Only three numeric capability scores plus a scanned/not-scanned flag are stored under RuneLite's account-profile configuration. RuneLite may synchronize profile configuration according to the user's RuneLite account settings.
- The task catalog is bundled with the plugin and is not downloaded at runtime.

## Selection behavior

Tasks above the configured goal tier, completed tasks, and tasks that fail the local stat/gear feasibility checks are excluded. Selection is weighted toward the current tier while retaining lower tiers in rotation. Speed tasks receive stricter stat and gear requirements.

The feasibility model is intentionally conservative and heuristic; RuneScape strategies and gear metas change. Opening the bank refreshes the stored capability scores.

## Scope

This is intentionally a single-purpose daily prompt rather than a Combat Achievement browser, full planner, or live encounter helper. Existing plugins such as Combat Achievement Helper provide broader recommendation and tracking workflows; Daily CA Picker's distinct behavior is one stable, low-friction assignment per account and calendar day. This narrow scope avoids cross-plugin dependencies while leaving detailed planning to those tools.

## Development

Requires Java 11.

```bash
./gradlew test
./gradlew build
./gradlew run
```

## License

Source code is BSD 2-Clause licensed. See `LICENSE` and `THIRD_PARTY_NOTICES.md` for catalog attribution.
