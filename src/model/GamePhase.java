package model;

/**
 * Phases of a MindWars game session. Used by the view to decide which screen
 * to render and by the controller to gate state transitions.
 */
public enum GamePhase {
    SETUP, // Game setup wizard (map size, names, mode, category, difficulty)
    HOT_SEAT_PASS, // "Pass the device to X" screen between players
    QUESTION, // Current player is answering a question
    TERRITORY_CLAIM, // A player is clicking cells to claim after a round
    INVASION_PASS, // Transition screen announcing Phase 2
    INVASION_SELECT, // Attacker picking a cell to attack from / target to attack
    INVASION_BATTLE, // Attacker/defender answering battle question
    GAME_OVER
}
