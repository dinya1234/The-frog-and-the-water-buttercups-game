package com.example.ui;

import com.example.game.GameSession;
import com.example.game.RandomPondFactory;
import com.example.io.PondParser;
import com.example.model.JumpPath;
import com.example.model.MaxJumpAttempt;
import com.example.model.Pond;
import com.example.model.PondAnalysis;
import com.example.solver.FrogAnalyzer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.Random;

public final class FrogGameFrame extends JFrame {

    private static final int MAX_GAME_PADS = 20;

    private static final Color WINDOW_BG = new Color(6, 27, 42);
    private static final Color PANEL_BG = new Color(9, 39, 57);
    private static final Color INPUT_BG = new Color(235, 255, 227);
    private static final Color TEXT = new Color(103, 243, 33);
    private static final Color MUTED = new Color(164, 211, 226);
    private static final Color ACCENT = new Color(38, 145, 103);
    private static final Color SECONDARY = new Color(29, 84, 112);
    private static final Color GOLD = new Color(198, 143, 38);
    private static final Color SUCCESS = new Color(137, 235, 167);
    private static final Color WARNING = new Color(255, 164, 126);
    private static final Color INFO = new Color(103, 243, 33);

    private final Random random = new Random();
    private final PondCanvas canvas;

    private JTextField pondField;
    private JTextArea infoArea;
    private JLabel statusLabel;
    private JButton applyButton;
    private JButton randomButton;
    private JButton resetButton;
    private JButton autoButton;
    private JButton helpButton;
    private JCheckBox pathCheck;

    private PondAnalysis analysis;
    private GameSession session;
    private boolean animating;
    private boolean autoPlaying;

    public FrogGameFrame() {
        super("Лягушка по кувшинкам — игра");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 640));
        setSize(1240, 760);
        setIconImage(GameIcon.create());
        canvas = new PondCanvas(this::onPadClicked);
        buildUi();
        loadPond(new Pond("Основной пример", 2, 3, 1, 1, 4),
                "Кликните по кувшинке в пределах прыжка. Цель — последняя кувшинка.");
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WINDOW_BG);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(canvas, BorderLayout.CENTER);
        root.add(buildSidebar(), BorderLayout.EAST);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(4, 23, 37));
        header.setBorder(new EmptyBorder(15, 22, 14, 22));

        JLabel title = new JLabel("Лягушка по кувшинкам");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 27));

        JLabel subtitle = new JLabel(
                "Клик — прыжок. Дойдите до последней кувшинки и сравните себя с оптимальным путём.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.CENTER);
        header.add(text, BorderLayout.WEST);

        JLabel algorithm = new JLabel("<html><div style='text-align:right'>жадный O(n)<br>два указателя<br>DP-эталон</div></html>");
        algorithm.setForeground(new Color(113, 177, 197));
        algorithm.setFont(new Font("SansSerif", Font.PLAIN, 11));
        algorithm.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(algorithm, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout(0, 14));
        side.setBackground(PANEL_BG);
        side.setBorder(new EmptyBorder(15, 15, 15, 15));
        side.setPreferredSize(new Dimension(380, 0));
        side.add(buildInputPanel(), BorderLayout.NORTH);
        side.add(buildInfoPanel(), BorderLayout.CENTER);
        side.add(buildActions(), BorderLayout.SOUTH);
        return side;
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(sectionLabel("Ряд кувшинок"), BorderLayout.NORTH);

        pondField = new JTextField("2, 3, 1, 1, 4");
        pondField.setFont(new Font("Monospaced", Font.BOLD, 15));
        pondField.setForeground(new Color(7, 42, 62));
        pondField.setBackground(INPUT_BG);
        pondField.setCaretColor(new Color(7, 42, 62));
        pondField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(82, 143, 164), 1),
                new EmptyBorder(8, 9, 8, 9)));
        pondField.addActionListener(event -> applyPondFromField());

        applyButton = createButton("Применить", ACCENT, 112);
        applyButton.addActionListener(event -> applyPondFromField());

        JPanel fieldRow = new JPanel(new BorderLayout(8, 0));
        fieldRow.setOpaque(false);
        fieldRow.add(pondField, BorderLayout.CENTER);
        fieldRow.add(applyButton, BorderLayout.EAST);
        panel.add(fieldRow, BorderLayout.CENTER);

        randomButton = createButton("Случайный пруд", SECONDARY, 0);
        randomButton.addActionListener(event -> loadPond(
                RandomPondFactory.random(random),
                "Случайный пруд готов: есть нули и несколько вариантов пути."));
        panel.add(randomButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(48, 94, 112), 1),
                " Ответы и состояние ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                MUTED));

        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setOpaque(false);
        infoArea.setForeground(TEXT);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBorder(new EmptyBorder(7, 7, 7, 9));

        JScrollPane scroll = new JScrollPane(infoArea);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildActions() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setOpaque(false);

        resetButton = createButton("Сброс", SECONDARY, 0);
        resetButton.addActionListener(event -> resetGame());

        autoButton = createButton("Автопрыжок по оптимуму", GOLD, 0);
        autoButton.addActionListener(event -> startAutoPlay());

        pathCheck = new JCheckBox("Показать оптимальный путь");
        pathCheck.setOpaque(false);
        pathCheck.setForeground(TEXT);
        pathCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pathCheck.setFocusPainted(false);
        pathCheck.setBorder(new EmptyBorder(6, 4, 6, 4));
        pathCheck.addActionListener(event -> canvas.setShowOptimalPath(
                pathCheck.isSelected() && analysis != null && analysis.path().isPresent()));

        helpButton = createButton("Правила игры", SECONDARY, 0);
        helpButton.addActionListener(event -> showRules());

        panel.add(resetButton);
        panel.add(autoButton);
        panel.add(pathCheck);
        panel.add(helpButton);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(4, 22, 35));
        bar.setBorder(new EmptyBorder(8, 18, 8, 18));

        statusLabel = new JLabel("Загрузка...");
        statusLabel.setForeground(INFO);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bar.add(statusLabel, BorderLayout.CENTER);
        return bar;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }

    private JButton createButton(String text, Color background, int preferredWidth) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(new Color(103, 243, 33));
        button.setBackground(background);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.brighter(), 1),
                new EmptyBorder(7, 11, 7, 11)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (preferredWidth > 0) {
            button.setPreferredSize(new Dimension(preferredWidth, 36));
        }
        return button;
    }

    private void loadPond(Pond pond, String message) {
        analysis = FrogAnalyzer.analyze(pond);
        session = new GameSession(analysis);
        animating = false;
        autoPlaying = false;
        canvas.stopAnimation();
        canvas.setPond(pond, analysis.path());
        canvas.setCurrentIndex(0);
        canvas.setWon(session.won());
        canvas.setShowOptimalPath(pathCheck.isSelected() && analysis.path().isPresent());
        pondField.setText(Arrays.toString(pond.jumps()));
        updateInfo();
        updateControls();
        updateStatus(message, INFO);
    }

    private void applyPondFromField() {
        try {
            Pond pond = PondParser.parse("Свой пруд", pondField.getText());
            if (pond.size() > MAX_GAME_PADS) {
                throw new IllegalArgumentException(
                        "Для игры используйте не больше " + MAX_GAME_PADS
                                + " кувшинок: иначе подписи становятся нечитаемыми. "
                                + "Для больших прудов есть режим --console.");
            }
            loadPond(pond, "Пруд загружен. Кликните по подсвеченной кувшинке в пределах прыжка.");
        } catch (IllegalArgumentException e) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Некорректный пруд", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetGame() {
        if (animating || session == null) {
            return;
        }
        autoPlaying = false;
        session.reset();
        canvas.stopAnimation();
        canvas.setCurrentIndex(0);
        canvas.setWon(session.won());
        updateInfo();
        updateControls();
        updateStatus(defaultHint(), INFO);
    }

    private void updateInfo() {
        if (analysis == null || session == null || infoArea == null) {
            return;
        }
        Pond pond = analysis.pond();
        StringBuilder text = new StringBuilder();

        text.append("Пруд: ").append(Arrays.toString(pond.jumps())).append("\n\n");
        text.append("Достижима: ").append(analysis.reachable() ? "да" : "нет").append('\n');
        if (analysis.reachable()) {
            text.append("Минимум: ").append(jumpsWord(analysis.minJumps()))
                    .append("  (жадный O(n))\n");
            text.append("DP:      ").append(jumpsWord(analysis.minJumpsDp()))
                    .append("  (эталон O(n²))\n");
            text.append("Путь:    ")
                    .append(analysis.path().map(JumpPath::format).orElse("—"))
                    .append('\n');
        } else {
            text.append("Минимум: недостижимо\n");
            text.append("DP:      недостижимо\n");
            text.append("Путь:    —\n");
        }

        MaxJumpAttempt maxJump = analysis.maxJumpAttempt();
        text.append("Max-жадный: ");
        if (maxJump.reachedTarget()) {
            text.append(jumpsWord(maxJump.jumps()));
            if (analysis.isCounterexampleForMaxJump()) {
                text.append("  ← не оптимум");
            }
        } else {
            text.append("застрял");
        }
        text.append("\n\n");

        if (session.won()) {
            text.append("Вы: цель достигнута\n");
            text.append("Прыжков: ").append(session.jumps()).append('\n');
            if (analysis.reachable()) {
                if (session.jumps() == analysis.minJumps()) {
                    text.append("Это оптимум!\n");
                } else {
                    text.append("Оптимум: ").append(analysis.minJumps()).append('\n');
                }
            }
        } else {
            text.append("Вы: кувшинка ").append(session.currentIndex())
                    .append(" (jump=").append(pond.jump(session.currentIndex())).append(")\n");
            text.append("Прыжков: ").append(session.jumps()).append('\n');
            int farthest = session.farthestFromCurrent();
            if (farthest > session.currentIndex()) {
                text.append("Ход: ").append(session.currentIndex() + 1)
                        .append("..").append(farthest).append('\n');
            } else {
                text.append("Ход: нет\n");
            }

            if (analysis.reachable()) {
                OptionalInt next = session.nextOptimalStep();
                if (next.isPresent()) {
                    text.append("Подсказка: оптимально на ").append(next.getAsInt()).append('\n');
                } else if (session.onOptimalPath()) {
                    text.append("Вы на оптимальном пути\n");
                } else {
                    text.append("Вы отклонились от оптимума\n");
                }
            }
        }

        if (analysis.isCounterexampleForMaxJump()) {
            text.append("\nКонтрпример: «прыгай на максимум»\nделает ")
                    .append(maxJump.jumps()).append(", а оптимум — ")
                    .append(analysis.minJumps()).append(".\n");
        }

        infoArea.setText(text.toString());
        infoArea.setCaretPosition(0);
    }

    private void updateControls() {
        boolean busy = animating;
        boolean hasPath = analysis != null && analysis.path().isPresent();
        applyButton.setEnabled(!busy);
        randomButton.setEnabled(!busy);
        resetButton.setEnabled(!busy && session != null);
        autoButton.setEnabled(!busy && hasPath && session != null && !session.won());
        pathCheck.setEnabled(!busy && hasPath);
        helpButton.setEnabled(!busy);
    }

    private void updateStatus(String message, Color color) {
        if (statusLabel == null) {
            return;
        }
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void onPadClicked(int index) {
        if (animating || session == null) {
            return;
        }
        if (session.won()) {
            updateStatus("Цель уже достигнута. Нажмите «Сброс», чтобы сыграть ещё раз.", INFO);
            return;
        }
        startJump(index, null);
    }

    private void startJump(int target, Runnable after) {
        if (animating || session == null) {
            return;
        }
        GameSession.MoveResult check = session.checkMove(target);
        if (!check.allowed()) {
            autoPlaying = false;
            canvas.flashError(target);
            updateStatus(check.message(), WARNING);
            updateControls();
            return;
        }

        int from = session.currentIndex();
        animating = true;
        updateControls();
        updateStatus("Прыжок " + (session.jumps() + 1) + "...", INFO);
        canvas.animateJump(from, target, () -> {
            session.move(target);
            animating = false;
            canvas.setCurrentIndex(target);
            canvas.setWon(session.won());
            updateInfo();
            updateControls();

            if (session.won()) {
                autoPlaying = false;
                updateStatus(winMessage(), SUCCESS);
            } else if (!session.canMove()) {
                updateStatus("Здесь прыгать некуда — путь закончился. Нажмите «Сброс».", WARNING);
            } else {
                updateStatus(defaultHint(), INFO);
            }

            if (after != null) {
                after.run();
            }
        });
    }

    private void startAutoPlay() {
        if (analysis == null || analysis.path().isEmpty() || animating || session == null) {
            return;
        }
        if (session.won()) {
            updateStatus("Сначала нажмите «Сброс», чтобы посмотреть автопрыжок.", INFO);
            return;
        }

        if (!session.onOptimalPath()) {
            session.reset();
            canvas.stopAnimation();
            canvas.setCurrentIndex(0);
            canvas.setWon(false);
            updateInfo();
        }

        autoPlaying = true;
        updateStatus("Автопрыжок по оптимальному пути...", INFO);
        playNextOptimal();
    }

    private void playNextOptimal() {
        if (!autoPlaying || session == null) {
            return;
        }
        if (session.won()) {
            autoPlaying = false;
            updateControls();
            return;
        }
        OptionalInt next = session.nextOptimalStep();
        if (next.isEmpty()) {
            autoPlaying = false;
            updateStatus("Вы отклонились от оптимального пути. «Сброс» вернёт на старт.", WARNING);
            updateControls();
            return;
        }
        startJump(next.getAsInt(), this::playNextOptimal);
    }

    private String defaultHint() {
        if (session == null) {
            return "";
        }
        if (session.won()) {
            return "Цель достигнута. «Сброс» — попробовать ещё раз.";
        }
        if (!session.canMove()) {
            return "Отсюда прыгать некуда. Нажмите «Сброс» или «Автопрыжок».";
        }
        return "Кликните по кувшинке " + (session.currentIndex() + 1)
                + ".." + session.farthestFromCurrent() + ".";
    }

    private String winMessage() {
        int jumps = session.jumps();
        if (analysis.reachable() && jumps == analysis.minJumps()) {
            return "Победа за " + jumpsWord(jumps) + " — это оптимум!";
        }
        return "Победа за " + jumpsWord(jumps)
                + ". Оптимум — " + analysis.minJumps()
                + "; попробуйте «Автопрыжок».";
    }

    private void showRules() {
        JOptionPane.showMessageDialog(this, """
                Как играть
                • Лягушка стартует на кувшинке 0.
                • Число на кувшинке — максимальная длина прыжка с неё.
                • Кликайте по кувшинке не дальше этого числа; прыгать можно только вперёд.
                • Цель — последняя кувшинка. Постарайтесь дойти за минимум прыжков.
                • «Автопрыжок» проходит оптимальный путь, «Сброс» возвращает на старт.
                • «Показать оптимальный путь» рисует его пунктиром над прудом.

                Справа видно ответ жадного алгоритма, DP-эталон и контрпример
                для стратегии «прыгай каждый раз на максимум».
                """, "Правила игры", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String jumpsWord(int count) {
        int mod100 = count % 100;
        int mod10 = count % 10;
        if (mod100 >= 11 && mod100 <= 14) {
            return count + " прыжков";
        }
        return switch (mod10) {
            case 1 -> count + " прыжок";
            case 2, 3, 4 -> count + " прыжка";
            default -> count + " прыжков";
        };
    }
}
