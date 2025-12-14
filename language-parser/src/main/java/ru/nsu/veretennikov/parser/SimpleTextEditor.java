package ru.nsu.veretennikov.parser;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class SimpleTextEditor extends JFrame {
    private JTextArea textArea;
    private JTextArea terminalCustom;
    private JTextArea terminalAntlr;
    private JSlider fontSizeSlider;
    private JSlider terminalFontSizeSlider;
    private LineNumberPanel lineNumberPanel;
    private JSplitPane mainSplitPane;
    private JSplitPane terminalSplitPane;

    public SimpleTextEditor() {
        createMenuBar();
        createMainContent();
        createSlider();

        SwingUtilities.invokeLater(() -> {
            mainSplitPane.setDividerLocation(0.5);
            terminalSplitPane.setDividerLocation(0.5);
        });

        setTitle("Simple Text Editor - Console.ReadLine Parser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem closeItem = new JMenuItem("Close");

        openItem.addActionListener(e -> openFile());
        saveItem.addActionListener(e -> saveFile());
        closeItem.addActionListener(e -> {
            textArea.setText("");
            terminalCustom.setText("");
            terminalAntlr.setText("");
        });

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(closeItem);

        // Text Menu
        JMenu textMenu = new JMenu("Text");
        JMenuItem docItem = new JMenuItem("Documentation");
        JMenuItem grammarItem = new JMenuItem("Grammar");

        docItem.addActionListener(e -> openDocumentation());
        grammarItem.addActionListener(e -> openGrammar());

        textMenu.add(docItem);
        textMenu.add(grammarItem);

        // Run Menu
        JMenu runMenu = new JMenu("Run");
        JMenuItem runParserItem = new JMenuItem("Run Both Parsers");
        JMenuItem runCustomItem = new JMenuItem("Run Custom Parser");
        JMenuItem runAntlrItem = new JMenuItem("Run ANTLR Parser");
        JMenuItem showTokensItem = new JMenuItem("Show Tokens");

        runParserItem.addActionListener(e -> runBothParsers());
        runCustomItem.addActionListener(e -> runCustomParser());
        runAntlrItem.addActionListener(e -> runAntlrParser());
        showTokensItem.addActionListener(e -> showTokens());

        runMenu.add(runParserItem);
        runMenu.add(runCustomItem);
        runMenu.add(runAntlrItem);
        runMenu.addSeparator();
        runMenu.add(showTokensItem);

        menuBar.add(fileMenu);
        menuBar.add(textMenu);
        menuBar.add(runMenu);

        setJMenuBar(menuBar);
    }

    private void createMainContent() {
        // Верхняя часть: редактор кода
        JPanel editorPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setText("// Enter your code here\n// Example:\nConsole.ReadLine();\nConsole.ReadLine(variable);\n");

        lineNumberPanel = new LineNumberPanel(textArea);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setRowHeaderView(lineNumberPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        editorPanel.add(scrollPane, BorderLayout.CENTER);

        // Нижняя часть: два терминала
        JPanel terminalsPanel = createTerminalsPanel();

        // Главный разделитель: редактор сверху, терминалы снизу
        mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, terminalsPanel);
        mainSplitPane.setResizeWeight(0.5);
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setContinuousLayout(true);

        add(mainSplitPane, BorderLayout.CENTER);
    }

    private JPanel createTerminalsPanel() {
        // Создаем два терминала рядом друг с другом
        JPanel customPanel = createSingleTerminalPanel("Custom Parser", terminalCustom = new JTextArea());
        JPanel antlrPanel = createSingleTerminalPanel("ANTLR Parser", terminalAntlr = new JTextArea());

        terminalCustom.append("Ready to analyze code...\n");
        terminalCustom.append("=====================================\n");

        terminalAntlr.append("ANTLR parser ready...\n");
        terminalAntlr.append("=====================================\n");

        // Разделитель между двумя терминалами
        terminalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, customPanel, antlrPanel);
        terminalSplitPane.setResizeWeight(0.5);
        terminalSplitPane.setOneTouchExpandable(true);
        terminalSplitPane.setContinuousLayout(true);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(terminalSplitPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createSingleTerminalPanel(String title, JTextArea terminal) {
        JPanel panel = new JPanel(new BorderLayout());

        // Заголовок
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(" " + title);
        label.setFont(new Font("Dialog", Font.BOLD, 12));
        headerPanel.add(label, BorderLayout.WEST);
        headerPanel.setBackground(new Color(230, 230, 230));

        // Терминал
        terminal.setEditable(false);
        terminal.setBackground(new Color(240, 240, 240));
        terminal.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(terminal);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void createSlider() {
        JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sliderPanel.add(new JLabel("Editor Font Size: "));

        fontSizeSlider = new JSlider(8, 72, 12);
        fontSizeSlider.setMajorTickSpacing(10);
        fontSizeSlider.setPaintTicks(true);
        fontSizeSlider.setPreferredSize(new Dimension(200, 40));
        fontSizeSlider.addChangeListener(e -> {
            if (!fontSizeSlider.getValueIsAdjusting()) {
                updateFontSize();
            }
        });

        sliderPanel.add(fontSizeSlider);

        // Слайдер для терминалов
        sliderPanel.add(new JLabel("  Terminal Font: "));
        terminalFontSizeSlider = new JSlider(8, 24, 11);
        terminalFontSizeSlider.setPreferredSize(new Dimension(100, 40));
        terminalFontSizeSlider.addChangeListener(e -> {
            if (!terminalFontSizeSlider.getValueIsAdjusting()) {
                updateTerminalFontSize();
            }
        });
        sliderPanel.add(terminalFontSizeSlider);

        add(sliderPanel, BorderLayout.NORTH);
    }

    private void updateFontSize() {
        int size = fontSizeSlider.getValue();
        Font font = new Font("Monospaced", Font.PLAIN, size);
        textArea.setFont(font);
        lineNumberPanel.setFont(font);
    }

    private void updateTerminalFontSize() {
        int size = terminalFontSizeSlider.getValue();
        Font font = new Font("Monospaced", Font.PLAIN, size);
        terminalCustom.setFont(font);
        terminalAntlr.setFont(font);
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                textArea.read(reader, null);
                terminalCustom.append("> Opened: " + fileChooser.getSelectedFile().getName() + "\n");
                terminalAntlr.append("> Opened: " + fileChooser.getSelectedFile().getName() + "\n");
            } catch (IOException ex) {
                showError("Error opening file: " + ex.getMessage());
            }
        }
    }

    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                textArea.write(writer);
                terminalCustom.append("> Saved: " + fileChooser.getSelectedFile().getName() + "\n");
                terminalAntlr.append("> Saved: " + fileChooser.getSelectedFile().getName() + "\n");
            } catch (IOException ex) {
                showError("Error saving file: " + ex.getMessage());
            }
        }
    }

    private void openDocumentation() {
        openDocFile("documentation.docx");
    }

    private void openGrammar() {
        openDocFile("grammar.docx");
    }

    private void openDocFile(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                String projectRoot = System.getProperty("user.dir");
                file = new File(projectRoot, filename);
                if (!file.exists()) {
                    showError("File " + filename + " not found in project root.\nLooked in: " + file.getAbsolutePath());
                    return;
                }
            }
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(file);
                    terminalCustom.append("> Opened: " + file.getAbsolutePath() + "\n");
                } else {
                    showError("Opening files is not supported on this system.");
                }
            } else {
                showError("Desktop API is not supported on this system.");
            }
        } catch (IOException ex) {
            showError("Error opening file " + filename + ": " + ex.getMessage());
        }
    }

    private void runBothParsers() {
        runCustomParser();
        runAntlrParser();
    }

    private void runCustomParser() {
        terminalCustom.setText("");
        terminalCustom.append("=== Custom Parser ===\n");
        terminalCustom.append("=====================================\n\n");

        String code = textArea.getText();
        if (code.trim().isEmpty()) {
            terminalCustom.append("> Empty program is ACCEPTED ✓\n");
            return;
        }

        CustomParser.ParseResult result = CustomParser.parseCode(code);
        if (result.isSuccess()) {
            terminalCustom.append(result.getTrace());
            terminalCustom.append("\n=====================================\n");
            terminalCustom.append("✓ SUCCESS: Code is syntactically correct!\n");
            if (result.getTokens() != null) {
                long count = result.getTokens().stream()
                        .filter(t -> t.getType() == Token.TokenType.CONSOLE_READLINE)
                        .count();
                terminalCustom.append(String.format("Found %d Console.ReadLine statement(s)\n", count));
            }
        } else {
            terminalCustom.append(result.getTrace());
            terminalCustom.append("\n=====================================\n");
            terminalCustom.append("✗ FAILED: Code contains syntax errors!\n\n");
            List<String> errors = result.getErrors();
            terminalCustom.append(String.format("Total errors: %d\n\n", errors.size()));
            for (int i = 0; i < errors.size(); i++) {
                terminalCustom.append(String.format("%d. %s\n", i + 1, errors.get(i)));
            }
        }
    }

    private void runAntlrParser() {
        terminalAntlr.setText("");
        terminalAntlr.append("=== ANTLR Parser ===\n");
        terminalAntlr.append("=====================================\n\n");

        String code = textArea.getText();
        if (code.trim().isEmpty()) {
            terminalAntlr.append("> Empty program is ACCEPTED ✓\n");
            return;
        }

        try {
            CharStream input = CharStreams.fromString(code);
            ConsoleReadLineLexer lexer = new ConsoleReadLineLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ConsoleReadLineParser antlrParser = new ConsoleReadLineParser(tokens);

            // Кастомный error listener
            antlrParser.removeErrorListeners();
            AntlrErrorListener errorListener = new AntlrErrorListener();
            antlrParser.addErrorListener(errorListener);

            // Парсинг
            ConsoleReadLineParser.ProgramContext tree = antlrParser.program();

            if (errorListener.hasErrors()) {
                terminalAntlr.append("✗ FAILED: Syntax errors found!\n\n");
                for (String error : errorListener.getErrors()) {
                    terminalAntlr.append(error + "\n");
                }
            } else {
                terminalAntlr.append("✓ SUCCESS: Code is syntactically correct!\n\n");

                // Подсчет операторов
                int stmtCount = tree.statement().size();
                terminalAntlr.append(String.format("Found %d Console.ReadLine statement(s)\n", stmtCount));

                // Показываем дерево разбора
                terminalAntlr.append("\nParse Tree:\n");
                terminalAntlr.append(tree.toStringTree(antlrParser) + "\n");
            }

        } catch (Exception e) {
            terminalAntlr.append("✗ ERROR: " + e.getMessage() + "\n");
        }
    }

    private void showTokens() {
        terminalCustom.setText("");
        terminalCustom.append("=== Token Analysis ===\n");
        terminalCustom.append("=====================================\n\n");

        String code = textArea.getText();
        if (code.trim().isEmpty()) {
            terminalCustom.append("> Code area is empty\n");
            return;
        }

        try {
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();
            terminalCustom.append(String.format("Found %d token(s):\n\n", tokens.size()));

            int count = 1;
            for (Token token : tokens) {
                if (token.getType() != Token.TokenType.EOF) {
                    terminalCustom.append(String.format("%2d. %-20s | Value: %-20s | Line: %d, Col: %d\n",
                            count++,
                            token.getType(),
                            token.getValue().isEmpty() ? "(empty)" : token.getValue(),
                            token.getLine(),
                            token.getColumn()
                    ));
                }
            }
            terminalCustom.append("\n=====================================\n");
            terminalCustom.append("✓ Tokenization completed\n");
        } catch (LexerException e) {
            terminalCustom.append("> ERROR: " + e.getMessage() + "\n");
            showError(e.getMessage());
        }
    }

    private void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Кастомный error listener для ANTLR
    static class AntlrErrorListener extends BaseErrorListener {
        private final java.util.List<String> errors = new java.util.ArrayList<>();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            errors.add(String.format("Line %d:%d - %s", line, charPositionInLine, msg));
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public java.util.List<String> getErrors() {
            return errors;
        }
    }

    // Line number panel
    class LineNumberPanel extends JPanel {
        private JTextArea textArea;
        private Font font;

        public LineNumberPanel(JTextArea textArea) {
            this.textArea = textArea;
            this.font = new Font("Monospaced", Font.PLAIN, 12);
            setBackground(Color.LIGHT_GRAY);
            setPreferredSize(new Dimension(40, textArea.getHeight()));

            textArea.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { repaint(); }
                public void removeUpdate(DocumentEvent e) { repaint(); }
                public void changedUpdate(DocumentEvent e) { repaint(); }
            });

            textArea.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    repaint();
                }
            });
        }

        public void setFont(Font font) {
            this.font = font;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setFont(font);
            g.setColor(Color.BLACK);

            FontMetrics fm = g.getFontMetrics();
            int fontHeight = fm.getHeight();
            int baseLine = fm.getAscent();

            Rectangle clip = g.getClipBounds();
            int startOffset = textArea.viewToModel2D(new Point(0, clip.y));
            int endOffset = textArea.viewToModel2D(new Point(0, clip.y + clip.height));

            try {
                int startLine = textArea.getLineOfOffset(startOffset);
                int endLine = textArea.getLineOfOffset(endOffset);
                int maxLineNum = endLine + 1;
                int maxWidth = fm.stringWidth(String.valueOf(maxLineNum)) + 10;
                setPreferredSize(new Dimension(maxWidth, textArea.getHeight()));

                for (int i = startLine; i <= endLine; i++) {
                    try {
                        Rectangle2D rect = textArea.modelToView2D(textArea.getLineStartOffset(i));
                        if (rect != null) {
                            String lineNumber = String.valueOf(i + 1);
                            int x = getWidth() - fm.stringWidth(lineNumber) - 5;
                            int y = (int)rect.getY() + baseLine;
                            g.drawString(lineNumber, x, y);
                        }
                    } catch (BadLocationException ex) {
                    }
                }
            } catch (BadLocationException ex) {
                g.drawString("1", 5, baseLine);
            }
            revalidate();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimpleTextEditor().setVisible(true);
        });
    }
}