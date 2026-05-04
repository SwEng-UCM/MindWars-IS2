---
version: 1.0
level: moderate
processes:
  coding: pair
  debugging: copilot
  planning: none
  documentation: assist
components:
  bot/: hint
  command/: pair
  controller/: pair
  game/: pair
  model/: pair
  network/: pair
  persistence/: pair
  trivia/: pair
  util/: copilot
  view/: pair
  Main.java: assist
  CheckQuestions.java: assist
---

## Notes

This project was developed with the assistance of AI tools:
- ChatGPT
- Claud
- Copilote

AI was used primarily to:
- Suggest and improve implementations of certain classes and methods
- Assist in debugging and resolving errors
- Provide explanations for programming concepts and design patterns
- Help structure parts of the application

Core logic and architecture decisions were designed and implemented by the author.

AI involvement varies across the project:
- Moderate use in controller, networking, command handling, and bot logic
- Lower use in core game logic, models, and utility classes

All AI-generated content has been reviewed, tested, and adapted. The author remains fully responsible for the final implementation.
