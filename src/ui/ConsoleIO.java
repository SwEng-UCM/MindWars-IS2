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
            @SuppressWarnings("resource") // Scanner must remain open for program lifetime
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

    /**
     * Reads a line with a live countdown timer displayed above the input.
     * The countdown shows remaining seconds in real-time.
     * Throws TimeoutException if no input is provided in time.
     */
    public String readLineWithTimeoutAndCountdown(String prompt, long timeoutMs) throws TimeoutException {
        // Flag to signal when input is received
        final boolean[] inputReceived = {false};
        final long[] lastDisplayedSecond = {-1};
        
        // Display initial countdown line
        long initialSeconds = timeoutMs / 1000;
        System.out.println("");
        System.out.println("  ⏱ Time remaining: " + initialSeconds + "s");
        System.out.println("");
        System.out.print("  Answer: ");
        System.out.flush();
        
        // Start countdown thread that updates the timer display
        Thread countdownThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            lastDisplayedSecond[0] = initialSeconds;
            
            while (!inputReceived[0]) {
                long elapsed = System.currentTimeMillis() - startTime;
                long remaining = timeoutMs - elapsed;
                
                if (remaining <= 0) {
                    break;
                }
                
                long secondsRemaining = remaining / 1000;
                
                // Only update display when the second changes
                if (secondsRemaining != lastDisplayedSecond[0] && secondsRemaining >= 0) {
                    lastDisplayedSecond[0] = secondsRemaining;
                    
                    // Save cursor position, move up 2 lines, update timer, restore position
                    System.out.print("\0337");  // Save cursor position
                    System.out.print("\033[2A"); // Move up two lines
                    System.out.print("\r  ⏱ Time remaining: " + secondsRemaining + "s      ");
                    System.out.print("\0338");  // Restore cursor position
                    System.out.flush();
                }
                
                try {
                    Thread.sleep(100); // Check every 100ms
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        countdownThread.setDaemon(true);
        countdownThread.start();
        
        try {
            String result = inputQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            inputReceived[0] = true;
            
            if (result == null) {
                System.out.println(); // Move to next line after timeout
                throw new TimeoutException();
            }
            
            System.out.println(); // Move to next line after input
            return result.trim();
        } catch (InterruptedException e) {
            inputReceived[0] = true;
            System.out.println();
            throw new TimeoutException();
        } finally {
            inputReceived[0] = true;
            countdownThread.interrupt();
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