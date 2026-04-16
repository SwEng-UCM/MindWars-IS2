package view;

import controller.GameController;
import controller.NavigationController;
import model.GameModel;
import model.GamePhase;
import network.NetworkSession;

import javax.swing.*;
import java.awt.*;

/**
 * The application window. Owns a {@link CardLayout} and hosts every screen.
 * Acts as the {@link NavigationController} so views can request a screen
 * change without depending on each other.
 */
public class MainFrame extends JFrame implements NavigationController {

    // Card names
    public static final String CARD_MENU = "menu";
    public static final String CARD_SETUP = "setup";
    public static final String CARD_LOAD = "load";
    public static final String CARD_SETTINGS = "settings";
    public static final String CARD_LEADER = "leaderboard";
    public static final String CARD_RULES = "rules";
    public static final String CARD_HOT_SEAT = "hotseat";
    public static final String CARD_GAME = "game";
    public static final String CARD_CLAIM = "claim";
    public static final String CARD_INV_PASS = "invpass";
    public static final String CARD_INV_SEL = "invsel";
    public static final String CARD_INV_BAT = "invbat";
    public static final String CARD_GAME_OVER = "gameover";
    public static final String CARD_MULTIPLAYER = "multiplayer";
    public static final String CARD_MP_LOBBY = "mplobby";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final GameModel model;
    private final GameController controller;
    private final NetworkSession networkSession = new NetworkSession();

    // Views
    private final MainMenuView menuView;
    private final GameSetupView setupView;
    private final LoadGameView loadView;
    private final SettingsView settingsView;
    private final LeaderboardView leaderboardView;
    private final RulesView rulesView;
    private final HotSeatView hotSeatView;
    private final GameBoardView gameBoardView;
    private final TerritoryClaimView claimView;
    private final HotSeatView invasionPassView;
    private final InvasionSelectView invasionSelectView;
    private final GameBoardView invasionBattleView;
    private final GameOverView gameOverView;
    private final NetworkSetupView networkSetupView;
    private final NetworkLobbyView networkLobbyView;
    private BettingView bettingView;

    public MainFrame(GameModel model) {
        super("MindWars");
        this.model = model;
        this.controller = new GameController(model, this);
        this.bettingView = new BettingView(controller);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(MindWarsTheme.FRAME_WIDTH, MindWarsTheme.FRAME_HEIGHT);
        setMinimumSize(new Dimension(500, 700));
        setLocationRelativeTo(null);

        menuView = new MainMenuView(this);
        setupView = new GameSetupView(controller);
        loadView = new LoadGameView(this);
        settingsView = new SettingsView(this);
        leaderboardView = new LeaderboardView(this);
        rulesView = new RulesView(this);
        hotSeatView = new HotSeatView(controller, false);
        gameBoardView = new GameBoardView(controller, false);
        claimView = new TerritoryClaimView(controller);
        invasionPassView = new HotSeatView(controller, true);
        invasionSelectView = new InvasionSelectView(controller);
        invasionBattleView = new GameBoardView(controller, true);
        gameOverView = new GameOverView(controller);
        networkSetupView = new NetworkSetupView(this, model, networkSession);
        networkLobbyView = new NetworkLobbyView(this, networkSession);

        root.add(menuView, CARD_MENU);
        root.add(setupView, CARD_SETUP);
        root.add(loadView, CARD_LOAD);
        root.add(settingsView, CARD_SETTINGS);
        root.add(leaderboardView, CARD_LEADER);
        root.add(rulesView, CARD_RULES);
        root.add(hotSeatView, CARD_HOT_SEAT);
        root.add(gameBoardView, CARD_GAME);
        root.add(claimView, CARD_CLAIM);
        root.add(invasionPassView, CARD_INV_PASS);
        root.add(invasionSelectView, CARD_INV_SEL);
        root.add(invasionBattleView, CARD_INV_BAT);
        root.add(gameOverView, CARD_GAME_OVER);
        root.add(bettingView, "betting");
        root.add(networkSetupView, CARD_MULTIPLAYER);
        root.add(networkLobbyView, CARD_MP_LOBBY);
        setContentPane(root);

        // Observe the model: whenever the phase changes, switch cards.
        model.addPropertyChangeListener(evt -> {
            if (GameModel.PROP_PHASE.equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(this::syncPhase);
            }
        });

        showMainMenu();
    }

    /** Switches cards to match the current model phase. */
    private void syncPhase() {
        GamePhase phase = model.getPhase();
        switch (phase) {
            case SETUP -> cards.show(root, CARD_SETUP);
            case HOT_SEAT_PASS -> {
                hotSeatView.refresh();
                cards.show(root, CARD_HOT_SEAT);
            }
            case QUESTION -> {
                gameBoardView.refresh();
                cards.show(root, CARD_GAME);
                gameBoardView.startTimer();
            }
            case TERRITORY_CLAIM -> {
                claimView.refresh();
                cards.show(root, CARD_CLAIM);
            }
            case INVASION_PASS -> {
                invasionPassView.refresh();
                cards.show(root, CARD_INV_PASS);
            }
            case INVASION_SELECT -> {
                invasionSelectView.refresh();
                cards.show(root, CARD_INV_SEL);
            }
            case INVASION_BATTLE -> {
                invasionBattleView.refresh();
                cards.show(root, CARD_INV_BAT);
                invasionBattleView.startTimer();
            }
            case GAME_OVER -> {
                gameOverView.refresh();
                cards.show(root, CARD_GAME_OVER);
            }
        }
    }

    // ── NavigationController ──
    @Override
    public void showMainMenu() {
        cards.show(root, CARD_MENU);
    }

    @Override
    public void showGameSetup() {
        setupView.reset();
        cards.show(root, CARD_SETUP);
    }

    @Override
    public void showLoadGame() {
        cards.show(root, CARD_LOAD);
    }

    @Override
    public void showSettings() {
        cards.show(root, CARD_SETTINGS);
    }

    @Override
    public void showLeaderboard() {
        leaderboardView.reload();
        cards.show(root, CARD_LEADER);
    }

    @Override
    public void showRules() {
        cards.show(root, CARD_RULES);
    }

    @Override
    public void showGame() {
        syncPhase();
    }

    @Override
    public void showGameOver() {
        gameOverView.refresh();
        cards.show(root, CARD_GAME_OVER);
    }

    @Override
    public void showBetting() {
        if (bettingView != null) {
            bettingView.refresh();
        }

        cards.show(root, "betting");
    }

    @Override
    public void showMultiplayer() {
        cards.show(root, CARD_MULTIPLAYER);
    }

    @Override
    public void showMultiplayerLobby() {
        networkLobbyView.refresh();
        cards.show(root, CARD_MP_LOBBY);
    }
}
