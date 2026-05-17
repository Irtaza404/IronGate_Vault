package com.irongate.service;

import java.util.PriorityQueue;
import java.util.function.Consumer;

/**
 * Password Cracking Simulator — DSA: Priority Queue (min-heap by complexity).
 *
 * Simulates a brute-force / dictionary attack and streams progress
 * back via a callback so the JavaFX UI can update in real time.
 *
 * This is a SIMULATION for educational/demonstration purposes only.
 * No actual cracking of stored hashes occurs.
 */
public class PasswordCrackerService {

    private volatile boolean running = false;

    public record CrackResult(
        int    attempts,
        long   estimatedMs,
        String strength,
        String message
    ) {}

    /**
     * Analyse password strength and simulate a brute-force attack.
     * @param password    The sample password to analyse.
     * @param onProgress  Called on each simulated step (attempt count).
     * @return            Final result summary.
     */
    public CrackResult analyse(String password, Consumer<Integer> onProgress) {
        running = true;

        // Build a priority queue of attack strategies (lower cost = higher priority)
        PriorityQueue<AttackStrategy> pq = new PriorityQueue<>();
        pq.add(new AttackStrategy("Dictionary", 1, commonWords(password)));
        pq.add(new AttackStrategy("BruteForce-Digits", 2, digitComplexity(password)));
        pq.add(new AttackStrategy("BruteForce-Lower", 3, lowerComplexity(password)));
        pq.add(new AttackStrategy("BruteForce-Mixed", 4, mixedComplexity(password)));
        pq.add(new AttackStrategy("BruteForce-Full",  5, fullComplexity(password)));

        int totalAttempts = 0;
        String strength   = "Strong";
        String winStrategy = "Brute Force (Full)";

        while (!pq.isEmpty() && running) {
            AttackStrategy strategy = pq.poll();
            totalAttempts += strategy.attempts;
            if (onProgress != null) onProgress.accept(totalAttempts);

            // Simulate processing delay
            try { Thread.sleep(80); } catch (InterruptedException ignored) {}

            // If this strategy could crack it in a reasonable attempt count, stop here
            if (strategy.attempts < 10_000) {
                winStrategy = strategy.name;
                break;
            }
        }

        // Assess strength
        int score = scorePassword(password);
        if (score <= 1)       strength = "Very Weak";
        else if (score == 2)  strength = "Weak";
        else if (score == 3)  strength = "Moderate";
        else if (score == 4)  strength = "Strong";
        else                  strength = "Very Strong";

        // Estimate real-world crack time at 1 billion attempts/second (GPU)
        long estimatedMs = (long)(Math.pow(totalAttempts, 1.3) / 1_000_000.0);

        running = false;
        return new CrackResult(
            totalAttempts,
            estimatedMs,
            strength,
            "Strategy that would succeed: " + winStrategy
        );
    }

    public void stop() { running = false; }

    // ── Complexity estimators ────────────────────────────────────

    private int commonWords(String pw) {
        // Simulated dictionary of 10 000 common passwords
        String[] common = {"password","123456","qwerty","abc123","letmein","welcome","admin","pass"};
        for (String w : common) if (pw.equalsIgnoreCase(w)) return 1;
        return 100_000; // not in common list
    }

    private int digitComplexity(String pw) {
        if (!pw.matches(".*[^0-9].*")) return (int) Math.pow(10, pw.length());
        return Integer.MAX_VALUE;
    }

    private int lowerComplexity(String pw) {
        if (!pw.matches(".*[^a-z].*")) return (int) Math.pow(26, Math.min(pw.length(), 7));
        return Integer.MAX_VALUE;
    }

    private int mixedComplexity(String pw) {
        if (!pw.matches(".*[^a-zA-Z0-9].*")) return (int) Math.pow(62, Math.min(pw.length(), 6));
        return Integer.MAX_VALUE;
    }

    private int fullComplexity(String pw) {
        int charsetSize = 95; // printable ASCII
        return (int) Math.min(Math.pow(charsetSize, Math.min(pw.length(), 5)), Integer.MAX_VALUE);
    }

    private int scorePassword(String pw) {
        int score = 0;
        if (pw.length() >= 8)                       score++;
        if (pw.length() >= 12)                      score++;
        if (pw.matches(".*[A-Z].*"))                score++;
        if (pw.matches(".*[0-9].*"))                score++;
        if (pw.matches(".*[^a-zA-Z0-9].*"))         score++;
        return score;
    }

    // ── Inner DSA record ─────────────────────────────────────────

    private static class AttackStrategy implements Comparable<AttackStrategy> {
        final String name;
        final int    priority;
        final int    attempts;

        AttackStrategy(String name, int priority, int attempts) {
            this.name     = name;
            this.priority = priority;
            this.attempts = attempts;
        }

        @Override
        public int compareTo(AttackStrategy o) {
            // Lower attempts = cheaper attack = higher priority in queue
            int cmp = Integer.compare(this.attempts, o.attempts);
            return cmp != 0 ? cmp : Integer.compare(this.priority, o.priority);
        }
    }
}
