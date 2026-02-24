package ui;

import java.util.Scanner;

public class ConsoleIO {

    private final Scanner scanner;

    public ConsoleIO() {
        this.scanner = new Scanner(System.in);
    }

    public void println(String s) {
        System.out.println(s);
    }

    public String readLine(String prompt) {
        System.out.println(prompt);
        System.out.print("> ");
        return scanner.nextLine().trim();
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
    public String selectFromList(String prompt, java.util.List<String> options) {
        println(prompt);
        for (int i = 0; i < options.size(); i++) {
            println("  " + (i + 1) + ") " + options.get(i));
        }
        int choice = readIntInRange("  Enter your choice (1-" + options.size() + "):", 1, options.size());
        return options.get(choice - 1);
    }
}