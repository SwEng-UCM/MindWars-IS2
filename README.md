# Agile Inception



## <center> <b>  MindWars </b> </center>

![Logo](assets/logo.png)

<center> <i> "Where brains conquer" </i> </center>

---

### <u> Members </u>

### Aloyse Arnaud
- **Responsibility:** takes care of the people  
- **Preferred name:** Aloyse  
- **Role NSWE:** South / East  
- **Role MBTI:** ENFJ-A (Protagonist) 
- **Personal strengths:** good communication, logical and structured, asks for help, ability to work in teams  
- **Personal areas of growth:** Java language, independence, stress management 
- **Fun fact:** Passion in life is music and concerts
- **GitHub:** `aloyse33`  

### Anastasia Irina Chelaru
- **Responsibility:** takes care of the process  
- **Preferred name:** Anastasia  
- **Role NSWE:** East  
- **Role MBTI:** ENTJ-A  
- **Personal strengths:** logical mind, well organized, ambitious, seeking opportunities  
- **Personal areas of growth:** learning web, getting out of my comfort zone
- **Fun fact:** Loves traveling and working at the same time.  
- **GitHub:** `anastasiachelaru`  

### Stefana-Raisa Dimofte
- **Responsibility:** takes care of the people  
- **Preferred name:** Raisa  
- **Role NSWE:** South  
- **Role MBTI:** INFJ-T  
- **Personal strengths:** insightful problem-solving, thoughtful communication, reliable follow-through  
- **Personal areas of growth:** managing idealistic expectations, becoming more comfortable seeking support, learning Java  
- **Fun fact:** Loves rom-coms and game nights
- **GitHub:** `raisadimofte`  

### Leopold Popper
- **Responsibility:** takes care of the result  
- **Preferred name:** Leo  
- **Role NSWE:** North  
- **Role MBTI:** ENFJ-A (Protagonist)
- **Personal strengths:** thoughtful, always active, gets things done 
- **Personal areas of growth:** collaboration / working with others  
- **Fun fact:** Can speak 6 languages
- **GitHub:** `leopopper`  

### Elena-Thea Ungureanu
- **Responsibility:** takes care of the brainstorming  
- **Preferred name:** Elena  
- **Role NSWE:** West  
- **Role MBTI:** ENFP-T  
- **Personal strengths:** conscientious and responsible, strong logical thinking, ambitious and growth-oriented  
- **Personal areas of growth:** taking on too many things at once, learning Java, may struggle to say “no” and set limits  
- **Fun fact:** Love everything about disney movies, can watch the same ones over and over again and never get bored
- **GitHub:** `elenathea3103`  

---

### <u> Project definition </u>

### One-liner
A multiplayer trivia game where players answer knowledge-based questions to gain points and strategically conquer territories controlled by other players.

### Why is it appealing / interesting / fun?
The project combines educational value with competitive and strategic gameplay. Extending traditional trivia into multiplayer encourages engagement and social interaction. The territory conquest adds meaningful consequences to answers, increasing motivation and immersion.

### Distinctive features
- Multiplayer trivia gameplay with direct competition  
- Territory-based mechanics: correct answers enable players to capture or defend areas  
- Skill-driven progression (knowledge over randomness)  
- Fast-paced gameplay with immediate feedback  
- High replayability due to dynamic player interactions and changing territory control  

### Target audience
Students and young adults (approx. **16–30**) who enjoy trivia, general knowledge games, and competitive multiplayer experiences. Also suitable for casual players seeking short sessions, and players motivated by social + strategic competition.

---

### <u> How we will work </u>

### Principles / group identity
We are a group of international students. We want a collaborative and inclusive environment where each member can contribute according to their Java level, learn, and improve.  
We follow these principles:
- Everyone has a role and commits to fulfilling it  
- We learn from each other  
- Everyone belongs in the group, including beginners in Java  

### Workflow
- Break the project into small tasks (questions, game logic, territories, simple interface)  
- Adapt tasks to each member’s level (beginner / intermediate)  
- Hold regular meetings to review progress  
- Use GitHub to share code and track issues  
- Provide help and explanations for members who are beginners in Java  
- Encourage questions without fear of mistakes  

### Strengthen the group
- Encourage mutual support (pair programming, explanations)  
- Value progress, even small achievements  
- Maintain a positive and motivating atmosphere  
- Celebrate milestones (completed features)

---


### <u> Brainstorming </u>

#### Game format ideas
- Classic Duel Trivia — Players answer questions in turns; highest points wins  
- Category Battle — Each round has a category (History, Movies, Science, etc.)  
- Risk Trivia — Before answering, player chooses how many points to risk  
- Territory Trivia — Each correct answer lets you claim a zone; most zones wins  
- Knockout Rounds — After some rounds, lowest score risks elimination  
- Streak Challenge — Build the longest correct-answer streak  
- Level-Based Trivia — Questions get harder each round  
- Speed Pressure Mode — Player must answer within limited attempts (fake speed)  

#### Turn mechanics
- Strict alternation (P1 → P2 → P1 → P2…)  
- Each round = both players answer one question  
- Player chooses category on their turn  
- Random category per round  
- “Steal Turn” if opponent answers wrong  
- Special round every 5 rounds (double points)  
- Fixed number of rounds (e.g. 10 rounds) or target score ends game  

#### Question types (console-friendly)
- Multiple choice (A/B/C/D)  
- True/False  
- Guess the number (closest wins)  
- Fill in the word (short answer)  
- “Which one doesn’t belong?”  
- Ordered answers (oldest → newest, biggest → smallest)  
- Riddle-style questions  

#### Scoring ideas
- Correct answer = +10 points  
- Wrong answer = 0  
- Risk system (bet 5 / 10 / 20 points)  
- Streak bonus (+5 per streak level)  
- Steal points if you answer opponent’s failed question  
- Double points in special rounds  
- Lose points if wrong in high-risk rounds  

#### Combined ideas
- Combo 1 — Risk Category Duel: choose category + points to risk  
- Combo 2 — Streak Battle: turns + streak bonus + steal if opponent breaks streak  
- Combo 3 — Territory Trivia Lite: each correct answer gives a zone; special rounds give 2 zones  
- Combo 4 — Final Gamble Showdown: normal game → last round both secretly bet points  

#### MVP (v1.0)
- Player name input  
- Turn system  
- Ask question  
- Validate answer  
- Score system  
- Round counter  
- Winner display  