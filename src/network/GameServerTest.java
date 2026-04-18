package network;

import model.GameModel;
import model.GameSettings;
import trivia.Question;
import trivia.QuestionBank;
import trivia.QuestionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Lightweight integration tests for {@link GameServer} and
 * {@link GameClient}. No external framework — prints PASS/FAIL like the
 * existing {@code TurnManagerTest}. Binds to an ephemeral port so parallel
 * runs don't collide.
 */
public class GameServerTest {

    private static int failures = 0;

    private static void assertTrue(String label, boolean cond) {
        if (cond) {
            System.out.println("  PASS: " + label);
        } else {
            System.out.println("  FAIL: " + label);
            failures++;
        }
    }

    private static GameSettings defaultSettings() {
        return new GameSettings(
                10,
                false,
                "Player 1", // p1 name
                "Player 2", // p2 name
                "Player 3", // p3 name
                "Player 4", // p4 name
                false, // randomMode
                "General",
                "Normal",
                1);
    }

    /** Minimal bank with one MCQ so the server has questions to broadcast. */
    private static QuestionBank stubBank() {
        // Built ahead of time because QuestionBank's super-ctor calls
        // getAllQuestionsAsList() before any subclass field initialisers run.
        final Question q = makeQuestion();
        return new QuestionBank(null) {
            @Override
            public Question getQuestion(String cat, String diff) {
                return q;
            }

            @Override
            public Set<String> getCategories() {
                return Set.of("Tech");
            }

            @Override
            public Set<String> getDifficulties(String c) {
                return Set.of("Easy");
            }

            @Override
            public List<Question> getAllQuestionsAsList() {
                return q == null ? List.of() : List.of(q);
            }
        };
    }

    private static Question makeQuestion() {
        Question q = new Question();
        q.setType(QuestionType.MULTIPLE_CHOICE);
        q.setCategory("Tech");
        q.setDifficulty("Easy");
        q.setPrompt("2 + 2 = ?");
        q.setChoices(new ArrayList<>(List.of("3", "4", "5")));
        q.setAnswer("B");
        return q;
    }

    private static void testHandshakeAndPhaseBroadcast() throws Exception {
        System.out.println("\n[testHandshakeAndPhaseBroadcast]");

        GameModel model = new GameModel(stubBank());
        GameServer server = new GameServer(0, defaultSettings(), model);
        server.start();
        int port = server.getBoundPort();

        List<NetworkMessage> a = new ArrayList<>();
        List<NetworkMessage> b = new ArrayList<>();
        CountDownLatch phaseLatch = new CountDownLatch(2);

        GameClient c1 = new GameClient();
        c1.setListener(msg -> {
            a.add(msg);
            if (msg.type == NetworkMessage.Type.PHASE)
                phaseLatch.countDown();
        });
        GameClient c2 = new GameClient();
        c2.setListener(msg -> {
            b.add(msg);
            if (msg.type == NetworkMessage.Type.PHASE)
                phaseLatch.countDown();
        });

        c1.connect("127.0.0.1", port, "Alice");
        c2.connect("127.0.0.1", port, "Bob");

        boolean gotPhase = phaseLatch.await(2, TimeUnit.SECONDS);
        assertTrue("both clients saw a PHASE broadcast", gotPhase);
        assertTrue("client1 received a WELCOME",
                a.stream().anyMatch(m -> m.type == NetworkMessage.Type.WELCOME));
        assertTrue("client2 received a WELCOME",
                b.stream().anyMatch(m -> m.type == NetworkMessage.Type.WELCOME));
        assertTrue("server is in HOT_SEAT_PASS after both joins",
                model.getPhase().name().equals("HOT_SEAT_PASS"));

        c1.close();
        c2.close();
        server.stop();
    }

    private static void testReadyBroadcastsQuestion() throws Exception {
        System.out.println("\n[testReadyBroadcastsQuestion]");

        GameModel model = new GameModel(stubBank());
        GameServer server = new GameServer(0, defaultSettings(), model);
        server.start();
        int port = server.getBoundPort();

        CountDownLatch questionLatch = new CountDownLatch(2);
        GameClient c1 = new GameClient();
        GameClient c2 = new GameClient();
        c1.setListener(msg -> {
            if (msg.type == NetworkMessage.Type.QUESTION)
                questionLatch.countDown();
        });
        c2.setListener(msg -> {
            if (msg.type == NetworkMessage.Type.QUESTION)
                questionLatch.countDown();
        });

        c1.connect("127.0.0.1", port, "Alice");
        c2.connect("127.0.0.1", port, "Bob");
        Thread.sleep(200); // give the lobby a moment

        // Only the current player's READY advances the phase.
        int current = model.getCurrentPlayerIndex();
        (current == 0 ? c1 : c2).sendReady();

        boolean gotQuestion = questionLatch.await(2, TimeUnit.SECONDS);
        assertTrue("both clients received a QUESTION after READY", gotQuestion);

        c1.close();
        c2.close();
        server.stop();
    }

    private static void testAnswerBroadcastsResult() throws Exception {
        System.out.println("\n[testAnswerBroadcastsResult]");

        GameModel model = new GameModel(stubBank());
        GameServer server = new GameServer(0, defaultSettings(), model);
        server.start();
        int port = server.getBoundPort();

        CountDownLatch resultLatch = new CountDownLatch(2);
        boolean[] correctSeen = { false };
        GameClient c1 = new GameClient();
        GameClient c2 = new GameClient();
        c1.setListener(msg -> {
            if (msg.type == NetworkMessage.Type.RESULT) {
                if (Boolean.TRUE.equals(msg.correct))
                    correctSeen[0] = true;
                resultLatch.countDown();
            }
        });
        c2.setListener(msg -> {
            if (msg.type == NetworkMessage.Type.RESULT)
                resultLatch.countDown();
        });

        c1.connect("127.0.0.1", port, "Alice");
        c2.connect("127.0.0.1", port, "Bob");
        Thread.sleep(200);

        int current = model.getCurrentPlayerIndex();
        (current == 0 ? c1 : c2).sendReady();
        Thread.sleep(200);
        (current == 0 ? c1 : c2).sendAnswer("B", 1500);

        boolean gotResult = resultLatch.await(2, TimeUnit.SECONDS);
        assertTrue("both clients received a RESULT after ANSWER", gotResult);
        assertTrue("RESULT marked answer correct", correctSeen[0]);

        c1.close();
        c2.close();
        server.stop();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("============================================================");
        System.out.println("  GameServer / GameClient Integration Tests");
        System.out.println("============================================================");

        testHandshakeAndPhaseBroadcast();
        testReadyBroadcastsQuestion();
        testAnswerBroadcastsResult();

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
