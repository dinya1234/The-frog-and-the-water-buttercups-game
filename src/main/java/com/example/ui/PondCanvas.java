package com.example.ui;

import com.example.model.JumpPath;
import com.example.model.Pond;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntConsumer;

public final class PondCanvas extends JPanel {

    private static final Color WATER_TOP = new Color(23, 108, 142);
    private static final Color WATER_BOTTOM = new Color(5, 38, 61);
    private static final Color PAD_LIGHT = new Color(92, 186, 105);
    private static final Color PAD_DARK = new Color(35, 112, 64);
    private static final Color PAD_BORDER = new Color(19, 72, 43);
    private static final Color GOLD_LIGHT = new Color(255, 219, 105);
    private static final Color GOLD_DARK = new Color(214, 151, 39);
    private static final Color TEXT = new Color(239, 252, 255);
    private static final Color MUTED = new Color(181, 224, 238);
    private static final Color REACHABLE = new Color(114, 228, 255);
    private static final Color ERROR = new Color(255, 96, 96);

    private static final int ANIMATION_FRAME_MS = 16;

    private final IntConsumer padClickHandler;

    private Pond pond;
    private Optional<JumpPath> optimalPath = Optional.empty();
    private int currentIndex;
    private boolean won;
    private boolean showOptimalPath;
    private int hoveredIndex = -1;
    private int errorIndex = -1;

    private boolean animating;
    private int animationFrom;
    private int animationTo;
    private long animationStartNanos;
    private long animationDurationNanos;
    private Timer animationTimer;
    private Runnable animationDone;
    private Timer errorTimer;

    public PondCanvas(IntConsumer padClickHandler) {
        this.padClickHandler = Objects.requireNonNull(padClickHandler, "Обработчик клика не может быть null");
        setOpaque(true);
        setBackground(WATER_BOTTOM);
        setPreferredSize(new Dimension(820, 560));
        setMinimumSize(new Dimension(420, 320));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (animating || pond == null) {
                    return;
                }
                int index = padAt(event.getPoint());
                if (index >= 0) {
                    padClickHandler.accept(index);
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                hoveredIndex = -1;
                setCursor(Cursor.getDefaultCursor());
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                int index = padAt(event.getPoint());
                if (index != hoveredIndex) {
                    hoveredIndex = index;
                    setCursor(index >= 0
                            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                            : Cursor.getDefaultCursor());
                    repaint();
                }
            }
        });
    }

    public void setPond(Pond pond, Optional<JumpPath> optimalPath) {
        stopAnimation();
        this.pond = Objects.requireNonNull(pond);
        this.optimalPath = Objects.requireNonNull(optimalPath);
        this.currentIndex = 0;
        this.won = pond.isSingle();
        this.hoveredIndex = -1;
        this.errorIndex = -1;
        repaint();
    }

    public void setCurrentIndex(int currentIndex) {
        if (pond == null) {
            this.currentIndex = 0;
            return;
        }
        this.currentIndex = Math.max(0, Math.min(pond.lastIndex(), currentIndex));
        repaint();
    }

    public void setWon(boolean won) {
        this.won = won;
        repaint();
    }

    public void setShowOptimalPath(boolean showOptimalPath) {
        this.showOptimalPath = showOptimalPath;
        repaint();
    }

    public void flashError(int index) {
        errorIndex = index;
        if (errorTimer != null) {
            errorTimer.stop();
        }
        errorTimer = new Timer(520, event -> {
            errorIndex = -1;
            errorTimer = null;
            repaint();
        });
        errorTimer.setRepeats(false);
        errorTimer.start();
        repaint();
    }

    public boolean isAnimating() {
        return animating;
    }

    public void animateJump(int fromIndex, int toIndex, Runnable after) {
        stopAnimation();
        if (pond == null) {
            if (after != null) {
                after.run();
            }
            return;
        }

        animationFrom = Math.max(0, Math.min(pond.lastIndex(), fromIndex));
        animationTo = Math.max(0, Math.min(pond.lastIndex(), toIndex));
        animationDone = after;
        animationStartNanos = System.nanoTime();

        Layout layout = computeLayout();
        double distance = layout.centers().get(animationFrom)
                .distance(layout.centers().get(animationTo));
        double seconds = 0.42 + Math.min(0.38, distance / 1400.0);
        animationDurationNanos = (long) (seconds * 1_000_000_000L);

        animating = true;
        animationTimer = new Timer(ANIMATION_FRAME_MS, event -> tickAnimation());
        animationTimer.start();
        repaint();
    }

    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
        animating = false;
        animationDone = null;
    }

    private void tickAnimation() {
        if (!animating) {
            return;
        }
        if (animationProgress() < 1.0) {
            repaint();
            return;
        }

        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
        animating = false;
        currentIndex = animationTo;
        Runnable callback = animationDone;
        animationDone = null;
        repaint();
        if (callback != null) {
            callback.run();
        }
    }

    private double animationProgress() {
        if (!animating) {
            return 1.0;
        }
        long elapsed = System.nanoTime() - animationStartNanos;
        return Math.max(0.0, Math.min(1.0, elapsed / (double) animationDurationNanos));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            drawWater(g);
            if (pond == null) {
                return;
            }

            Layout layout = computeLayout();
            if (showOptimalPath) {
                drawOptimalPath(g, layout);
            }
            drawPads(g, layout);
            drawFrog(g, layout);
            drawHud(g);
        } finally {
            g.dispose();
        }
    }

    private void drawWater(Graphics2D g) {
        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());
        g.setPaint(new GradientPaint(0, 0, WATER_TOP, 0, height, WATER_BOTTOM));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(255, 255, 255, 13));
        for (int i = 0; i < 18; i++) {
            int x = Math.floorMod(i * 173 + 31, width + 100) - 50;
            int y = Math.floorMod(i * 97 + 43, height + 40) - 20;
            int rippleWidth = 42 + (i * 19) % 96;
            g.fillOval(x, y, rippleWidth, 8 + i % 4);
        }

        g.setColor(new Color(191, 240, 255, 16));
        g.fillOval(width / 2 - 160, -80, 320, 170);

        g.setStroke(new BasicStroke(3f));
        g.setColor(new Color(36, 118, 79, 155));
        for (int i = 0; i < 6; i++) {
            int x = 22 + i * 11;
            int top = height - 70 - (i * 23) % 55;
            g.drawLine(x, height, x + i % 2 * 4, top);
        }
        for (int i = 0; i < 5; i++) {
            int x = width - 24 - i * 10;
            int top = height - 55 - (i * 31) % 70;
            g.drawLine(x, height, x - i % 2 * 5, top);
        }
    }

    private void drawOptimalPath(Graphics2D g, Layout layout) {
        if (optimalPath.isEmpty()) {
            return;
        }
        List<Integer> indices = optimalPath.get().indices();
        if (indices.size() < 2) {
            return;
        }

        Path2D path = new Path2D.Double();
        for (int i = 0; i < indices.size(); i++) {
            Point2D point = layout.centers().get(indices.get(i));
            if (i == 0) {
                path.moveTo(point.getX(), point.getY());
            } else {
                path.lineTo(point.getX(), point.getY());
            }
        }

        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10f, new float[]{12f, 10f}, 0f));
        g.setColor(new Color(255, 221, 112, 145));
        g.draw(path);
    }

    private void drawPads(Graphics2D g, Layout layout) {
        int last = pond.lastIndex();
        int reachableTo = Math.min(last, (int) Math.min(Integer.MAX_VALUE,
                (long) currentIndex + pond.jump(currentIndex)));
        List<Integer> pathIndices = showOptimalPath && optimalPath.isPresent()
                ? optimalPath.get().indices()
                : List.of();

        for (int index = 0; index < pond.size(); index++) {
            Point2D center = layout.centers().get(index);
            double x = center.getX();
            double y = center.getY();
            int radius = layout.radius();
            boolean target = index == last;
            boolean current = index == currentIndex;
            boolean reachable = !won && index > currentIndex && index <= reachableTo;
            boolean onPath = pathIndices.contains(index);

            if (current) {
                glow(g, x, y, radius + 17, new Color(127, 255, 170, 62));
            } else if (reachable) {
                glow(g, x, y, radius + 12, new Color(114, 228, 255, 50));
            }
            if (onPath && !current) {
                ring(g, x, y, radius + 6, new Color(255, 218, 100, 210), 2.5f);
            }
            if (index == errorIndex) {
                ring(g, x, y, radius + 8, ERROR, 3.5f);
            }

            g.setColor(new Color(1, 20, 33, 110));
            g.fill(new Ellipse2D.Double(x - radius, y - radius * 0.58 + 7,
                    radius * 2.0, radius * 1.16));

            g.setPaint(new GradientPaint(
                    (float) x, (float) (y - radius * 0.58),
                    target ? GOLD_LIGHT : PAD_LIGHT,
                    (float) x, (float) (y + radius * 0.58),
                    target ? GOLD_DARK : PAD_DARK));
            Ellipse2D pad = new Ellipse2D.Double(
                    x - radius, y - radius * 0.58, radius * 2.0, radius * 1.16);
            g.fill(pad);

            g.setStroke(new BasicStroke(2f));
            g.setColor(PAD_BORDER);
            g.draw(pad);

            g.setColor(new Color(255, 255, 255, 58));
            g.setStroke(new BasicStroke(1.6f));
            g.drawArc((int) (x - radius * 0.72), (int) (y - radius * 0.50),
                    (int) (radius * 1.44), (int) (radius * 0.82), 18, 145);

            if (reachable) {
                ring(g, x, y, radius + 2, REACHABLE, 2.4f);
            }

            String jumpText = Integer.toString(pond.jump(index));
            g.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, (int) (radius * 0.92))));
            FontMetrics metrics = g.getFontMetrics();
            int textX = (int) Math.round(x - metrics.stringWidth(jumpText) / 2.0);
            int textY = (int) Math.round(y + metrics.getAscent() / 2.0 - 3);
            g.setColor(new Color(6, 45, 30, 105));
            g.drawString(jumpText, textX + 1, textY + 1);
            g.setColor(target ? new Color(69, 51, 8) : TEXT);
            g.drawString(jumpText, textX, textY);

            g.setFont(new Font("SansSerif", Font.PLAIN, Math.max(10, radius / 2)));
            String indexText = Integer.toString(index);
            FontMetrics indexMetrics = g.getFontMetrics();
            int indexX = (int) Math.round(x - indexMetrics.stringWidth(indexText) / 2.0);
            int indexY = (int) Math.round(y + radius * 0.58 + indexMetrics.getAscent() + 9);
            g.setColor(new Color(3, 24, 37, 150));
            g.drawString(indexText, indexX + 1, indexY + 1);
            g.setColor(MUTED);
            g.drawString(indexText, indexX, indexY);
        }
    }

    private void drawFrog(Graphics2D g, Layout layout) {
        if (pond == null || layout.centers().isEmpty()) {
            return;
        }

        double x;
        double y;
        double arcLift = 0.0;
        if (animating) {
            double time = animationProgress();
            double eased = time * time * (3.0 - 2.0 * time);
            Point2D from = layout.centers().get(animationFrom);
            Point2D to = layout.centers().get(animationTo);
            x = from.getX() + (to.getX() - from.getX()) * eased;
            y = from.getY() + (to.getY() - from.getY()) * eased;
            double distance = from.distance(to);
            arcLift = Math.min(120.0, 38.0 + distance * 0.32) * Math.sin(Math.PI * time);
        } else {
            Point2D point = layout.centers().get(Math.max(0, Math.min(pond.lastIndex(), currentIndex)));
            x = point.getX();
            y = point.getY();
        }

        double radius = layout.radius() * 0.82;
        double frogY = y - radius * 0.68 - arcLift;

        if (!animating) {
            g.setColor(new Color(0, 26, 20, 90));
            g.fill(new Ellipse2D.Double(x - radius * 0.78, y + radius * 0.18,
                    radius * 1.56, radius * 0.48));
        }

        g.setColor(new Color(44, 145, 72));
        g.fill(new Ellipse2D.Double(x - radius * 1.12, frogY + radius * 0.30,
                radius * 0.62, radius * 0.46));
        g.fill(new Ellipse2D.Double(x + radius * 0.50, frogY + radius * 0.30,
                radius * 0.62, radius * 0.46));

        g.setPaint(new GradientPaint(
                (float) x, (float) (frogY - radius * 0.9),
                new Color(111, 216, 116),
                (float) x, (float) (frogY + radius * 0.9),
                new Color(35, 145, 72)));
        g.fill(new Ellipse2D.Double(x - radius, frogY - radius * 0.88,
                radius * 2.0, radius * 1.72));
        g.setColor(new Color(24, 102, 53));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Ellipse2D.Double(x - radius, frogY - radius * 0.88,
                radius * 2.0, radius * 1.72));

        g.setColor(new Color(175, 242, 168));
        g.fill(new Ellipse2D.Double(x - radius * 0.52, frogY - radius * 0.02,
                radius * 1.04, radius * 0.82));

        double eyeRadius = radius * 0.30;
        g.setColor(new Color(243, 255, 238));
        g.fill(new Ellipse2D.Double(x - radius * 0.62, frogY - radius * 0.92,
                eyeRadius * 2.0, eyeRadius * 2.0));
        g.fill(new Ellipse2D.Double(x + radius * 0.08, frogY - radius * 0.92,
                eyeRadius * 2.0, eyeRadius * 2.0));
        g.setColor(new Color(12, 44, 29));
        g.fill(new Ellipse2D.Double(x - radius * 0.45, frogY - radius * 0.78,
                eyeRadius * 0.78, eyeRadius * 0.92));
        g.fill(new Ellipse2D.Double(x + radius * 0.25, frogY - radius * 0.78,
                eyeRadius * 0.78, eyeRadius * 0.92));

        g.setColor(new Color(24, 102, 53));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc((int) (x - radius * 0.36), (int) (frogY + radius * 0.16),
                (int) (radius * 0.72), (int) (radius * 0.46), 205, 130);

        if (won) {
            drawCrown(g, x, frogY - radius * 1.28, radius);
        }
    }

    private void drawCrown(Graphics2D g, double x, double y, double radius) {
        Path2D crown = new Path2D.Double();
        crown.moveTo(x - radius * 0.55, y);
        crown.lineTo(x - radius * 0.62, y - radius * 0.42);
        crown.lineTo(x - radius * 0.25, y - radius * 0.16);
        crown.lineTo(x, y - radius * 0.52);
        crown.lineTo(x + radius * 0.25, y - radius * 0.16);
        crown.lineTo(x + radius * 0.62, y - radius * 0.42);
        crown.lineTo(x + radius * 0.55, y);
        crown.closePath();
        g.setColor(GOLD_LIGHT);
        g.fill(crown);
        g.setColor(GOLD_DARK);
        g.setStroke(new BasicStroke(1.5f));
        g.draw(crown);
    }

    private void drawHud(Graphics2D g) {
        String text;
        if (won) {
            text = "ЦЕЛЬ ДОСТИГНУТА!";
        } else if (animating) {
            text = "Прыжок...";
        } else {
            int reachableTo = Math.min(pond.lastIndex(),
                    (int) Math.min(Integer.MAX_VALUE, (long) currentIndex + pond.jump(currentIndex)));
            text = reachableTo > currentIndex
                    ? "Кувшинка " + currentIndex + "  •  можно прыгнуть на " + (currentIndex + 1)
                    + ".." + reachableTo
                    : "Отсюда прыгать некуда";
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(text) + 26;
        g.setColor(new Color(3, 28, 43, 155));
        g.fillRoundRect(14, 13, width, 34, 15, 15);
        g.setColor(won ? GOLD_LIGHT : new Color(228, 249, 255));
        g.drawString(text, 27, 36);

        if (pond != null && !won) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(new Color(217, 244, 252, 175));
            String hint = "Цель — кувшинка " + pond.lastIndex();
            int hintWidth = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, Math.max(14, getWidth() - hintWidth - 20), 31);
        }
    }

    private void glow(Graphics2D g, double x, double y, int radius, Color color) {
        g.setColor(color);
        g.fill(new Ellipse2D.Double(x - radius, y - radius * 0.72,
                radius * 2.0, radius * 1.44));
    }

    private void ring(Graphics2D g, double x, double y, int radius, Color color, float width) {
        g.setColor(color);
        g.setStroke(new BasicStroke(width));
        g.draw(new Ellipse2D.Double(x - radius, y - radius * 0.62,
                radius * 2.0, radius * 1.24));
    }

    private Layout computeLayout() {
        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());
        if (pond == null) {
            return new Layout(List.of(), 20);
        }

        int n = pond.size();
        double margin = Math.min(82.0, width * 0.065);
        double available = Math.max(1.0, width - 2.0 * margin);
        double spacing = n == 1 ? 0.0 : Math.min(112.0, available / (n - 1));
        double startX = width / 2.0 - spacing * (n - 1) / 2.0;
        double baseY = height * 0.62;
        double arc = n == 1 ? 0.0 : Math.min(52.0, height * 0.085);

        int radius = (int) Math.round(Math.max(16.0, Math.min(34.0, spacing * 0.38)));
        radius = Math.max(12, Math.min(radius, (int) (height * 0.13)));

        List<Point2D> centers = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            double x = startX + spacing * i;
            double y = n == 1 ? baseY : baseY - Math.sin(Math.PI * i / (n - 1)) * arc;
            if (i == 0 && n > 1) {
                y = baseY - Math.sin(Math.PI * 0.04) * arc;
            }
            centers.add(new Point2D.Double(x, y));
        }
        return new Layout(List.copyOf(centers), radius);
    }

    private int padAt(Point point) {
        if (pond == null) {
            return -1;
        }
        Layout current = computeLayout();
        double horizontal = current.radius() + 8.0;
        double vertical = current.radius() * 0.62 + 8.0;
        int best = -1;
        double bestScore = Double.MAX_VALUE;
        for (int i = 0; i < current.centers().size(); i++) {
            Point2D center = current.centers().get(i);
            double dx = (point.getX() - center.getX()) / horizontal;
            double dy = (point.getY() - center.getY()) / vertical;
            double score = dx * dx + dy * dy;
            if (score <= 1.0 && score < bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }

    private record Layout(List<Point2D> centers, int radius) {
    }
}
