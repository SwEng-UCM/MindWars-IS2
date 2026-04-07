package game;

import bot.EasyBot;
import player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link TurnManager}.
 *
 * <p>Run this class directly (it uses no external test framework) so it works
 * with the existing project setup that only has gson on the classpath.
 *
 * <p>Each test method prints PASS / FAIL to stdout. A non-zero exit code is
 * returned if any test fails.
 */
public class TurnManagerTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static int failures = 0;

    private static void assertTrue(String label, boolean condition) {
        if (condition) {
            System.out.println("  PASS: " + label);
        } else {
            System.out.println("  FAIL: " + label);
            failures++;
        }
    }

    private static void assertFalse(String label, boolean condition) {
        assertTrue(label, !condition);
    }

    private static void assertEquals(String label, Object expected, Object actual) {
        boolean ok = (expected == null ? actual == null : expected.equals(actual));
        if (ok) {
            System.out.println("  PASS: " + label + " [" + actual + "]");
        } else {
            System.out.println("  FAIL: " + label + " – expected <" + expected + "> but got <" + actual + ">");
            failures++;
        }
    }

    /** Builds a fresh two-human-player game state. */
    private static GameState twoHumanState() {
        GameState gs = new GameState();
        Player p1 = new Player("Alice");
        Player p2 = new Player("Bob");
        gs.addPlayer(p1);
        gs.addPlayer(p2);
        return gs;
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /** getCurrentPlayer returns index-0 player after construction/reset. */
    private static void testGetCurrentPlayerInitial() {
        System.out.println("\n[testGetCurrentPlayerInitial]");
        GameState gs = twoHumanState();
        TurnManager tm = new TurnManager(gs);

        assertEquals("current player is Alice", "Alice", tm.getCurrentPlayer().getName());
    }

    /** advanceTurn cycles correctly across all players and wraps around. */
    private static void testAdvanceTurnWraps() {
        System.out.println("\n[testAdvanceTurnWraps]");
        GameState gs = twoHumanState();
        TurnManager tm = new TurnManager(gs);

        assertEquals("initial – Alice", "Alice", tm.getCurrentPlayer().getName());

        tm.advanceTurn();
        assertEquals("after 1 advance – Bob", "Bob", tm.getCurrentPlayer().getName());

        tm.advanceTurn(); // wraps back to 0
        assertEquals("after wrap – Alice", "Alice", tm.getCurrentPlayer().getName());
    }

    /** advanceTurn increments round number on wrap-around. */
    private static void testRoundNumberIncrementOnWrap() {
        System.out.println("\n[testRoundNumberIncrementOnWrap]");
        GameState gs = twoHumanState();
        TurnManager tm = new TurnManager(gs);

        assertEquals("round starts at 1", 1, gs.getRoundNumber());

        tm.advanceTurn(); // still round 1 (only advanced, not wrapped)
        assertEquals("round still 1 after first advance", 1, gs.getRoundNumber());

        tm.advanceTurn(); // wraps → round 2
        assertEquals("round incremented to 2 after wrap", 2, gs.getRoundNumber());
    }

    /** resetTurn puts the index back to 0 without changing round number. */
    private static void testResetTurn() {
        System.out.println("\n[testResetTurn]");
        GameState gs = twoHumanState();
        TurnManager tm = new TurnManager(gs);

        tm.advanceTurn(); // now at Bob
        assertEquals("at Bob before reset", "Bob", tm.getCurrentPlayer().getName());

        tm.resetTurn();
        assertEquals("back to Alice after reset", "Alice", tm.getCurrentPlayer().getName());
    }

    /** isActivePlayer enforces the single active player contract. */
    private static void testIsActivePlayerGuard() {
        System.out.println("\n[testIsActivePlayerGuard]");
        GameState gs = twoHumanState();
        TurnManager tm = new TurnManager(gs);

        Player alice = gs.getPlayers().get(0);
        Player bob   = gs.getPlayers().get(1);

        assertTrue ("Alice is active at start",        tm.isActivePlayer(alice));
        assertFalse("Bob is NOT active at start",      tm.isActivePlayer(bob));
        assertFalse("null is never an active player",  tm.isActivePlayer(null));

        tm.advanceTurn();
        assertFalse("Alice no longer active after advance", tm.isActivePlayer(alice));
        assertTrue ("Bob is now active",                    tm.isActivePlayer(bob));
    }

    /** isCurrentPlayerBot returns false for human players. */
    private static void testIsCurrentPlayerBotForHuman() {
        System.out.println("\n[testIsCurrentPlayerBotForHuman]");
        GameState gs = twoHumanState();
        TurnManager tm = new TurnManager(gs);

        assertFalse("Alice (human) is not a bot", tm.isCurrentPlayerBot());
    }

    /** isCurrentPlayerBot returns true when the active player has a BotStrategy. */
    private static void testIsCurrentPlayerBotForBot() {
        System.out.println("\n[testIsCurrentPlayerBotForBot]");
        GameState gs = new GameState();
        Player human = new Player("Alice");
        Player bot   = new Player("Optimus Prime");
        bot.setStrategy(new EasyBot());

        gs.addPlayer(human);
        gs.addPlayer(bot);

        TurnManager tm = new TurnManager(gs);

        assertFalse("human first – not a bot",  tm.isCurrentPlayerBot());
        tm.advanceTurn();
        assertTrue ("bot second – is a bot",    tm.isCurrentPlayerBot());
    }

    /** isBotAtIndex works independently of the current turn. */
    private static void testIsBotAtIndex() {
        System.out.println("\n[testIsBotAtIndex]");
        GameState gs = new GameState();
        Player human = new Player("Alice");
        Player bot   = new Player("Bot");
        bot.setStrategy(new EasyBot());

        gs.addPlayer(human);
        gs.addPlayer(bot);

        TurnManager tm = new TurnManager(gs);

        assertFalse("index 0 is human", tm.isBotAtIndex(0));
        assertTrue ("index 1 is bot",   tm.isBotAtIndex(1));
    }

    /** TurnChangeListener is notified with correct previous/current players. */
    private static void testListenerNotifiedOnAdvance() {
        System.out.println("\n[testListenerNotifiedOnAdvance]");
        GameState gs = twoHumanState();
        TurnManager tm = new TurnManager(gs);

        List<String> log = new ArrayList<>();
        tm.addTurnChangeListener((prev, curr, round) ->
                log.add(prev.getName() + "->" + curr.getName() + " R" + round));

        tm.advanceTurn();
        tm.advanceTurn(); // wraps

        assertEquals("first callback", "Alice->Bob R1", log.get(0));
        assertEquals("second callback (wrap)", "Bob->Alice R2", log.get(1));
    }

    /** Duplicate listener registration is silently ignored. */
    private static void testDuplicateListenerNotAdded() {
        System.out.println("\n[testDuplicateListenerNotAdded]");
        GameState gs = twoHumanState();
        TurnManager tm = new TurnManager(gs);

        int[] callCount = { 0 };
        TurnManager.TurnChangeListener l = (prev, curr, round) -> callCount[0]++;

        tm.addTurnChangeListener(l);
        tm.addTurnChangeListener(l); // duplicate
        tm.advanceTurn();

        assertEquals("listener called exactly once", 1, callCount[0]);
    }

    /** removeTurnChangeListener stops future callbacks. */
    private static void testRemoveListener() {
        System.out.println("\n[testRemoveListener]");
        GameState gs = twoHumanState();
        TurnManager tm = new TurnManager(gs);

        int[] callCount = { 0 };
        TurnManager.TurnChangeListener l = (prev, curr, round) -> callCount[0]++;
        tm.addTurnChangeListener(l);

        tm.advanceTurn(); // fires
        tm.removeTurnChangeListener(l);
        tm.advanceTurn(); // should NOT fire

        assertEquals("listener called only once before removal", 1, callCount[0]);
    }

    /** getCurrentPlayer throws when there are no players. */
    private static void testThrowsWhenNoPlayers() {
        System.out.println("\n[testThrowsWhenNoPlayers]");
        GameState gs = new GameState(); // empty
        TurnManager tm = new TurnManager(gs);

        boolean threw = false;
        try {
            tm.getCurrentPlayer();
        } catch (IllegalStateException e) {
            threw = true;
        }
        assertTrue("throws IllegalStateException with no players", threw);
    }

    /** getPlayerCount and getRoundNumber delegate correctly. */
    private static void testConvenienceGetters() {
        System.out.println("\n[testConvenienceGetters]");
        GameState gs = twoHumanState();
        TurnManager tm = new TurnManager(gs);

        assertEquals("player count is 2", 2, tm.getPlayerCount());
        assertEquals("initial round is 1", 1, tm.getRoundNumber());
        assertEquals("initial index is 0", 0, tm.getCurrentPlayerIndex());
    }

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("  TurnManager Unit Tests");
        System.out.println("============================================================");

        testGetCurrentPlayerInitial();
        testAdvanceTurnWraps();
        testRoundNumberIncrementOnWrap();
        testResetTurn();
        testIsActivePlayerGuard();
        testIsCurrentPlayerBotForHuman();
        testIsCurrentPlayerBotForBot();
        testIsBotAtIndex();
        testListenerNotifiedOnAdvance();
        testDuplicateListenerNotAdded();
        testRemoveListener();
        testThrowsWhenNoPlayers();
        testConvenienceGetters();

        System.out.println("\n============================================================");
        if (failures == 0) {
            System.out.println("  ALL TESTS PASSED");
        } else {
            System.out.println("  " + failures + " TEST(S) FAILED");
        }
        System.out.println("============================================================");

        System.exit(failures);
    }
}
