package ui;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConsoleIO {

    private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

    public ConsoleIO() {
        // Single background thread reads all System.in input into a queue.
        // This allows readLineWithTimeout() to poll with a deadline without
        // leaving a blocked scanner.nextLine() that would eat future input.
        Thread readerThread = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                try {
                    inputQueue.put(sc.nextLine());
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public void println(String s) {
        System.out.println(s);
    }

    public String readLine(String prompt) {
        System.out.println(prompt);
        System.out.print("> ");
        try {
            return inputQueue.take().trim();
        } catch (InterruptedException e) {
            return "";
        }
    }

    /**
     * Reads a line within the given timeout in milliseconds.
     * Throws TimeoutException if no input is provided in time.
     */
    public String readLineWithTimeout(String prompt, long timeoutMs) throws TimeoutException {
        System.out.println(prompt);
        System.out.print("> ");
        try {
            String result = inputQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            if (result == null) {
                throw new TimeoutException();
            }
            return result.trim();
        } catch (InterruptedException e) {
            throw new TimeoutException();
        }
    }

    public String readNonEmptyString(String prompt) {
        String input = "";
        while (input.isEmpty()) {
            input = readLine(prompt);
            if (input.isEmpty()) {
                println("Error: Input cannot be empty. Please try again.");
            }
        }
        return input;
    }

    // helper function to safely read a number in a range (useful for choosing how
    // many rounds the players want to play)
    public int readIntInRange(String prompt, int minInclusive, int maxInclusive) {
        int result;
        while (true) {
            String input = readLine(prompt);
            try {
                result = Integer.parseInt(input);
                if (result >= minInclusive && result <= maxInclusive) {
                    break;
                } else {
                    println("Please enter a number between " + minInclusive + " and " + maxInclusive + ".");
                }
            } catch (NumberFormatException e) {
                println("Invalid input. Please enter a valid number.");
            }
        }
        return result;
    }

    // helper function to let a player choose from a list of options
    public String selectFromList(String prompt, List<String> options) {
        println(prompt);
        for (int i = 0; i < options.size(); i++) {
            println("  " + (i + 1) + ") " + options.get(i));
        }
        int choice = readIntInRange("  Enter your choice (1-" + options.size() + "):", 1, options.size());
        return options.get(choice - 1);
    }
}