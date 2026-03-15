# MindWars

![Logo](assets/logo.png)

 "Where brains conquer" 




### Description
A 2-player hot seat trivia game where players answer questions to earn points and conquer territory on a shared map.


## Build & Run

```bash
javac -d out -cp "lib/*" src/**/*.java
java -cp "out:lib/*" Main
```

Requires Java 17+ and GSON (included in `lib/`).

## Project Structure

```text
src/
├─ Main.java                         # Entry point
├─ CheckQuestions.java               # Utility to validate questions.json
│
├─ game/                             # Core game logic
│  ├─ Game.java                      # Main orchestrator (rounds, scoring, territory)
│  ├─ GameState.java                 # Mutable state (players, current turn)
│  ├─ TurnManager.java               # Turn order logic
│  ├─ WinnerCalculator.java          # Final winner (score + territory tiebreaker)
│  ├─ NumericWinnerCalculator.java   # Estimation round winner (closest + fastest)
│  └─ MapGrid.java                   # Territory grid with fog of war and bonus cells
│
├─ player/                           # Player data model
│  └─ Player.java                    # Name, score, timer, streak, symbol
│
├─ trivia/                           # Question management
│  ├─ QuestionType.java              # Enum (MCQ, True/False, Numeric, Open-Ended, Ordering)
│  ├─ Question.java                  # Question model with multi-type support
│  ├─ QuestionBank.java              # Loads questions from JSON by category & difficulty
│  └─ AnswerValidator.java           # Input validation and answer checking
│
└─ ui/                               # User interface
   ├─ ConsoleIO.java                 # Console I/O with timeout and countdown
   └─ SoundManager.java              # Async WAV playback (one-shot + looping)
```
