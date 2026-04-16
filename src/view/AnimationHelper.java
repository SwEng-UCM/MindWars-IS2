package view;

import javax.swing.*;
import java.awt.*;

/**
 * Small Swing animation helpers used across the gameplay screens (#90).
 * Everything here uses the EDT — each animation spins up a short-lived
 * {@link Timer} and stops itself once complete. No external dependencies.
 */
public final class AnimationHelper {

    private AnimationHelper() {
    }

    /**
     * Briefly enlarges then shrinks the component's font to draw attention
     * to it. Works on any {@link JComponent} that respects {@code setFont}
     * (labels, buttons).
     */
    public static void pulseFont(JComponent comp, Font base, int peakDelta, int steps, int stepMs) {
        final int half = Math.max(1, steps / 2);
        final float baseSize = base.getSize2D();
        Timer t = new Timer(stepMs, null);
        final int[] tick = { 0 };
        t.addActionListener(e -> {
            int k = tick[0]++;
            float factor = k <= half ? (k / (float) half) : (1f - (k - half) / (float) (steps - half));
            float size = baseSize + peakDelta * factor;
            comp.setFont(base.deriveFont(size));
            if (k >= steps) {
                comp.setFont(base);
                ((Timer) e.getSource()).stop();
            }
        });
        t.setInitialDelay(0);
        t.start();
    }

    /**
     * Fades the component's background between two colors in a fixed number
     * of ticks. The final frame pins the background to {@code to}. Useful
     * for correct/incorrect feedback flashes and cell conquest animations.
     */
    public static void flashBackground(JComponent comp, Color from, Color to, int steps, int stepMs) {
        final int total = Math.max(1, steps);
        final int[] tick = { 0 };
        comp.setOpaque(true);
        Timer t = new Timer(stepMs, null);
        t.addActionListener(e -> {
            int k = tick[0]++;
            float f = Math.min(1f, k / (float) total);
            comp.setBackground(blend(from, to, f));
            comp.repaint();
            if (k >= total) {
                comp.setBackground(to);
                ((Timer) e.getSource()).stop();
            }
        });
        t.setInitialDelay(0);
        t.start();
    }

    /**
     * Oscillates the border thickness on the component so the eye catches
     * the target of an attack. Used in invasion mode to highlight the cell
     * being attacked.
     */
    public static void pulseBorder(JComponent comp, Color color, int cycles, int stepMs) {
        final int[] tick = { 0 };
        final int total = cycles * 2;
        Timer t = new Timer(stepMs, null);
        t.addActionListener(e -> {
            int k = tick[0]++;
            int thickness = (k % 2 == 0) ? 4 : 1;
            comp.setBorder(BorderFactory.createLineBorder(color, thickness));
            if (k >= total) {
                ((Timer) e.getSource()).stop();
            }
        });
        t.setInitialDelay(0);
        t.start();
    }

    private static Color blend(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bb = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bb);
    }
}
