package ru.nsu.veretennikov.parser;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.List;

public class SimpleTextEditor extends JFrame {
    private JTextArea textArea;
    private JTextArea terminal;
    private JSlider fontSizeSlider;
    private JSlider terminalFontSizeSlider;
    private LineNumberPanel lineNumberPanel;
    private JSplitPane splitPane;

    public SimpleTextEditor() {
        createMenuBar();
        createMainContent(); // Теперь создает и редактор, и терминал
        createSlider();

        // Устанавливаем начальную позицию разделителя (70% для редактора, 30% для терминала)
        SwingUtilities.invokeLater(() -> {
            splitPane.setDividerLocation(0.7);
        });

        setTitle("Simple Text Editor - Console.ReadLine Parser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
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
            terminal.setText("");
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
        JMenuItem runParserItem = new JMenuItem("Run Parser");
        JMenuItem showTokensItem = new JMenuItem("Show Tokens");

        runParserItem.addActionListener(e -> runParser());
        showTokensItem.addActionListener(e -> showTokens());

        runMenu.add(runParserItem);
        runMenu.add(showTokensItem);

        menuBar.add(fileMenu);
        menuBar.add(textMenu);
        menuBar.add(runMenu);

        setJMenuBar(menuBar);
    }

    private void createMainContent() {
        JPanel editorPanel = new JPanel(new BorderLayout());

        // Text area
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setText("// Enter your code here\n// Example:\nConsole.ReadLine();\nConsole.ReadLine(variable);\n");

        // Line numbers panel
        lineNumberPanel = new LineNumberPanel(textArea);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setRowHeaderView(lineNumberPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        editorPanel.add(scrollPane, BorderLayout.CENTER);

        // Создаем панель терминала
        JPanel terminalPanel = createTerminalPanel();

        // Создаем разделитель (SplitPane) между редактором и терминалом
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, terminalPanel);
        splitPane.setResizeWeight(0.7); // 70% для редактора, 30% для терминала
        splitPane.setOneTouchExpandable(true); // Кнопки для быстрого сворачивания/разворачивания
        splitPane.setContinuousLayout(true); // Плавное изменение размера

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createTerminalPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Заголовок терминала с контролами
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel terminalLabel = new JLabel(" Terminal Output");
        terminalLabel.setFont(new Font("Dialog", Font.BOLD, 12));

        // Слайдер для размера шрифта терминала
        JPanel fontControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        fontControlPanel.add(new JLabel("Terminal Font: "));

        terminalFontSizeSlider = new JSlider(8, 24, 11);
        terminalFontSizeSlider.setPreferredSize(new Dimension(100, 20));
        terminalFontSizeSlider.addChangeListener(e -> {
            if (!terminalFontSizeSlider.getValueIsAdjusting()) {
                updateTerminalFontSize();
            }
        });

        fontControlPanel.add(terminalFontSizeSlider);

        headerPanel.add(terminalLabel, BorderLayout.WEST);
        headerPanel.add(fontControlPanel, BorderLayout.EAST);
        headerPanel.setBackground(new Color(230, 230, 230));

        // Терминал
        terminal = new JTextArea(5, 20);
        terminal.setEditable(false);
        terminal.setBackground(new Color(240, 240, 240));
        terminal.setFont(new Font("Monospaced", Font.PLAIN, 11));
        terminal.setText("Parser Terminal\n");
        terminal.append("Ready to analyze code...\n");
        terminal.append("=====================================\n");

        JScrollPane terminalScroll = new JScrollPane(terminal);
        terminalScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(terminalScroll, BorderLayout.CENTER);

        return panel;
    }

    private void updateTerminalFontSize() {
        int size = terminalFontSizeSlider.getValue();
        Font font = new Font("Monospaced", Font.PLAIN, size);
        terminal.setFont(font);
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
        add(sliderPanel, BorderLayout.NORTH);
    }

    private void updateFontSize() {
        int size = fontSizeSlider.getValue();
        Font font = new Font("Monospaced", Font.PLAIN, size);
        textArea.setFont(font);
        lineNumberPanel.setFont(font);
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                textArea.read(reader, null);
                terminal.append("> Opened file: " + fileChooser.getSelectedFile().getName() + "\n");
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
                terminal.append("> Saved file: " + fileChooser.getSelectedFile().getName() + "\n");
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
                    showError("File " + filename + " not found in project root.\n" +
                            "Looked in: " + file.getAbsolutePath());
                    return;
                }
            }

            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(file);
                    terminal.append("> Opened file: " + file.getAbsolutePath() + "\n");
                } else {
                    showError("Opening files is not supported on this system.");
                }
            } else {
                showError("Desktop API is not supported on this system.");
            }
        } catch (IOException ex) {
            showError("Error opening file " + filename + ": " + ex.getMessage());
        } catch (Exception ex) {
            showError("Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Запускает парсер для анализа кода в текстовой области
     */
    private void runParser() {
        terminal.setText(""); // Очищаем терминал
        terminal.append("=== Running Parser ===\n");
        terminal.append("=====================================\n\n");

        String code = textArea.getText();

        if (code.trim().isEmpty()) {
            terminal.append("> Code area is empty\n");
            terminal.append("> Empty program is ACCEPTED ✓\n");
            return;
        }

        // Запускаем парсер
        Parser.ParseResult result = Parser.parseCode(code);

        if (result.isSuccess()) {
            terminal.append(result.getTrace());
            terminal.append("\n=====================================\n");
            terminal.append("✓ SUCCESS: Code is syntactically correct!\n");

            // Показываем количество операторов
            if (result.getTokens() != null) {
                long count = result.getTokens().stream()
                        .filter(t -> t.getType() == Token.TokenType.CONSOLE_READLINE)
                        .count();
                terminal.append(String.format("Found %d Console.ReadLine statement(s)\n", count));
            }

            showMessage("Parse Success", "Code is syntactically correct!");
        } else {
            terminal.append(result.getTrace());
            terminal.append("\n=====================================\n");
            terminal.append("✗ FAILED: Code contains syntax errors!\n\n");

            List<String> errors = result.getErrors();
            terminal.append(String.format("Total errors found: %d\n\n", errors.size()));

            for (int i = 0; i < errors.size(); i++) {
                terminal.append(String.format("%d. %s\n", i + 1, errors.get(i)));
            }

            // Показываем диалог с ошибками
            StringBuilder errorDialog = new StringBuilder();
            errorDialog.append(String.format("Found %d error(s):\n\n", errors.size()));
            for (int i = 0; i < Math.min(5, errors.size()); i++) {
                errorDialog.append(errors.get(i)).append("\n");
            }
            if (errors.size() > 5) {
                errorDialog.append(String.format("\n... and %d more error(s). See terminal for details.", errors.size() - 5));
            }

            showError(errorDialog.toString());
        }
    }

    /**
     * Показывает все токены из кода
     */
    private void showTokens() {
        terminal.setText("");
        terminal.append("=== Token Analysis ===\n");
        terminal.append("=====================================\n\n");

        String code = textArea.getText();

        if (code.trim().isEmpty()) {
            terminal.append("> Code area is empty\n");
            return;
        }

        try {
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();

            terminal.append(String.format("Found %d token(s):\n\n", tokens.size()));

            int count = 1;
            for (Token token : tokens) {
                if (token.getType() != Token.TokenType.EOF) {
                    terminal.append(String.format("%2d. %-20s | Value: %-20s | Line: %d, Col: %d\n",
                            count++,
                            token.getType(),
                            token.getValue().isEmpty() ? "(empty)" : token.getValue(),
                            token.getLine(),
                            token.getColumn()
                    ));
                }
            }

            terminal.append("\n=====================================\n");
            terminal.append("✓ Tokenization completed\n");

        } catch (LexerException e) {
            terminal.append("> ERROR: " + e.getMessage() + "\n");
            showError(e.getMessage());
        }
    }

    private void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Custom line number panel
    class LineNumberPanel extends JPanel {
        private JTextArea textArea;
        private Font font;

        public LineNumberPanel(JTextArea textArea) {
            this.textArea = textArea;
            this.font = new Font("Monospaced", Font.PLAIN, 12);
            setBackground(Color.LIGHT_GRAY);
            setPreferredSize(new Dimension(40, textArea.getHeight()));

            // Update line numbers when text changes
            textArea.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { repaint(); }
                public void removeUpdate(DocumentEvent e) { repaint(); }
                public void changedUpdate(DocumentEvent e) { repaint(); }
            });

            // Update line numbers when component is resized
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

            // Get visible text area bounds
            Rectangle clip = g.getClipBounds();
            int startOffset = textArea.viewToModel2D(new Point(0, clip.y));
            int endOffset = textArea.viewToModel2D(new Point(0, clip.y + clip.height));

            try {
                // Get line numbers for visible area
                int startLine = textArea.getLineOfOffset(startOffset);
                int endLine = textArea.getLineOfOffset(endOffset);

                // Calculate width needed for line numbers
                int maxLineNum = endLine + 1;
                int maxWidth = fm.stringWidth(String.valueOf(maxLineNum)) + 10;
                setPreferredSize(new Dimension(maxWidth, textArea.getHeight()));

                // Draw line numbers
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
                        // Skip this line if there's an error
                    }
                }
            } catch (BadLocationException ex) {
                // If there's an error, just draw line 1
                g.drawString("1", 5, baseLine);
            }

            // Revalidate to update the scroll pane if width changed
            revalidate();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimpleTextEditor().setVisible(true);
        });
    }
}