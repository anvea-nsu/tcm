package parser;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ModernParserIDE extends Application {

    private TextArea codeArea;
    private VBox lineNumberContainer;
    private TextArea terminalArea;
    private double fontSize = 14;
    private double zoomLevel = 1.0;

    @Override
    public void start(Stage primaryStage) {
        // Главный контейнер
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #212121;"); // Светлый фон

        // Верхняя часть
        VBox topContainer = new VBox(createModernMenuBar(), createModernToolBar());
        root.setTop(topContainer);

        // Центральная часть
        root.setCenter(createModernCodeEditor());

        // Нижняя часть
        root.setBottom(createModernTerminal());

        Scene scene = new Scene(root, 1200, 800);
        scene.setFill(Color.web("#f8f9fa"));

        primaryStage.setTitle("Modern Parser IDE");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createModernMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #212121; -fx-border-color: #e1e5e9; -fx-border-width: 0 0 1 0;");

        // File Menu
        Menu fileMenu = new Menu("File");
        fileMenu.setStyle("-fx-text-fill: #2c3e50;");
        MenuItem newFile = new MenuItem("New File      ⌘N");
        MenuItem open = new MenuItem("Open...       ⌘O");
        MenuItem save = new MenuItem("Save          ⌘S");
        MenuItem close = new MenuItem("Close         ⌘W");
        fileMenu.getItems().addAll(newFile, open, save, new SeparatorMenuItem(), close);

        // Text Menu
        Menu textMenu = new Menu("Text");
        textMenu.setStyle("-fx-text-fill: #2c3e50;");
        MenuItem documentation = new MenuItem("Documentation");
        MenuItem grammar = new MenuItem("Grammar");
        textMenu.getItems().addAll(documentation, grammar);

        // View Menu
        Menu viewMenu = new Menu("View");
        viewMenu.setStyle("-fx-text-fill: #2c3e50;");
        MenuItem zoomIn = new MenuItem("Zoom In      ⌘+");
        MenuItem zoomOut = new MenuItem("Zoom Out     ⌘-");
        MenuItem increaseFont = new MenuItem("Increase Font");
        MenuItem decreaseFont = new MenuItem("Decrease Font");
        viewMenu.getItems().addAll(zoomIn, zoomOut, new SeparatorMenuItem(), increaseFont, decreaseFont);

        // Run Menu
        Menu runMenu = new Menu("Run");
        runMenu.setStyle("-fx-text-fill: #2c3e50;");
        MenuItem runParser = new MenuItem("Run Parser     ⌘R");
        runMenu.getItems().add(runParser);

        // Обработчики
        zoomIn.setOnAction(e -> zoom(0.1));
        zoomOut.setOnAction(e -> zoom(-0.1));
        increaseFont.setOnAction(e -> changeFontSize(1));
        decreaseFont.setOnAction(e -> changeFontSize(-1));
        runParser.setOnAction(e -> runParser());

        menuBar.getMenus().addAll(fileMenu, textMenu, viewMenu, runMenu);
        return menuBar;
    }

    private ToolBar createModernToolBar() {
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: #212121; -fx-padding: 8; -fx-border-color: #e1e5e9; -fx-border-width: 0 0 1 0;");

        // Project section
        MenuButton projectMenu = new MenuButton("Modern Parser Project");
        projectMenu.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        MenuItem newProject = new MenuItem("New Project");
        MenuItem openProject = new MenuItem("Open Project");
        projectMenu.getItems().addAll(newProject, openProject);

        // Action buttons
        Button newBtn = createToolbarButton("New", "📄");
        Button openBtn = createToolbarButton("Open", "📁");
        Button saveBtn = createToolbarButton("Save", "💾");

        // Zoom controls
        Label zoomLabel = new Label("Zoom:");
        zoomLabel.setStyle("-fx-text-fill: #5a6c7d;");

        Slider zoomSlider = new Slider(0.5, 2.0, 1.0);
        zoomSlider.setPrefWidth(120);
        zoomSlider.setStyle("-fx-control-inner-background: #212121;");
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoomLevel = newVal.doubleValue();
            updateFontSize();
        });

        // Run button
        Button runBtn = new Button("▶ Run");
        runBtn.setStyle("-fx-background-color: #212121; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 6; -fx-padding: 8 16;");
        runBtn.setOnAction(e -> runParser());

        toolbar.getItems().addAll(
                projectMenu, new Separator(),
                newBtn, openBtn, saveBtn, new Separator(),
                zoomLabel, zoomSlider, new Separator(),
                runBtn
        );

        return toolbar;
    }

    private Button createToolbarButton(String text, String emoji) {
        Button btn = new Button(emoji + " " + text);
        btn.setStyle("-fx-background-color: #212121; -fx-text-fill: #2c3e50; -fx-background-radius: 4; -fx-padding: 6 12;");
        return btn;
    }

    private HBox createModernCodeEditor() {
        HBox editorContainer = new HBox();
        editorContainer.setStyle("-fx-background-color: #212121;");

        // Контейнер для номеров строк
        lineNumberContainer = new VBox();
        lineNumberContainer.setStyle("-fx-background-color: #212121; -fx-border-color: #e1e5e9; -fx-border-width: 0 1 0 0;");
        lineNumberContainer.setPrefWidth(60);
        lineNumberContainer.setAlignment(Pos.TOP_RIGHT);
        lineNumberContainer.setPadding(new Insets(10, 15, 10, 10));

        // Область редактирования кода - СВЕТЛАЯ!
        codeArea = new TextArea();
        codeArea.setStyle("-fx-control-inner-background: #212121; -fx-text-fill: #2c3e50; " +
                "-fx-font-family: 'SF Mono', 'Monaco', 'Menlo', monospace; " +
                "-fx-font-size: 14; -fx-border-width: 0; -fx-focus-color: transparent; " +
                "-fx-background-color: #212121;");
        codeArea.setPrefHeight(Double.MAX_VALUE);

        // Синхронизация номеров строк
        codeArea.textProperty().addListener((obs, oldVal, newVal) -> updateModernLineNumbers());

        // Используем ScrollPane для лучшего контроля
        ScrollPane scrollPane = new ScrollPane(codeArea);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        scrollPane.setFitToWidth(true);

        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        editorContainer.getChildren().addAll(lineNumberContainer, scrollPane);
        updateModernLineNumbers();

        return editorContainer;
    }

    private void updateModernLineNumbers() {
        lineNumberContainer.getChildren().clear();
        String text = codeArea.getText();
        if (text.isEmpty()) {
            addLineNumber(1);
            return;
        }

        int lineCount = text.split("\n", -1).length;
        for (int i = 1; i <= lineCount; i++) {
            addLineNumber(i);
        }
    }

    private void addLineNumber(int number) {
        Label lineLabel = new Label(String.valueOf(number));
        lineLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-family: 'SF Mono', 'Menlo', monospace; -fx-font-size: 13;");
        lineLabel.setPadding(new Insets(2, 0, 2, 0));
        lineNumberContainer.getChildren().add(lineLabel);
    }

    private VBox createModernTerminal() {
        VBox terminalContainer = new VBox();
        terminalContainer.setStyle("-fx-background-color: #212121; -fx-border-color: #e1e5e9; -fx-border-width: 1 0 0 0;");

        // Заголовок терминала
        HBox terminalHeader = new HBox();
        terminalHeader.setStyle("-fx-background-color: #212121; -fx-padding: 12 15; -fx-border-color: #e1e5e9; -fx-border-width: 0 0 1 0;");
        terminalHeader.setAlignment(Pos.CENTER_LEFT);

        Label terminalLabel = new Label("Terminal");
        terminalLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-background-color: #212121; -fx-text-fill: #2c3e50; -fx-background-radius: 4;");
        clearBtn.setOnAction(e -> terminalArea.clear());

        HBox.setHgrow(terminalLabel, Priority.ALWAYS);
        terminalHeader.getChildren().addAll(terminalLabel, clearBtn);

        // Область терминала - СВЕТЛАЯ!
        terminalArea = new TextArea();
        terminalArea.setStyle("-fx-control-inner-background: #212121; -fx-text-fill: #2c3e50; " +
                "-fx-font-family: 'SF Mono', 'Monaco', 'Menlo', monospace; " +
                "-fx-font-size: 13; -fx-border-width: 0;");
        terminalArea.setPrefHeight(200);
        terminalArea.setEditable(false);

        terminalContainer.getChildren().addAll(terminalHeader, terminalArea);
        VBox.setVgrow(terminalArea, Priority.ALWAYS);

        return terminalContainer;
    }

    private void zoom(double delta) {
        zoomLevel += delta;
        zoomLevel = Math.max(0.5, Math.min(2.0, zoomLevel));
        updateFontSize();
    }

    private void changeFontSize(int delta) {
        fontSize += delta;
        fontSize = Math.max(8, Math.min(24, fontSize));
        updateFontSize();
    }

    private void updateFontSize() {
        String currentStyle = codeArea.getStyle();
        // Удаляем старый размер шрифта и добавляем новый
        String newStyle = currentStyle.replaceAll("-fx-font-size: \\d+;", "") +
                String.format("-fx-font-size: %.0f;", fontSize * zoomLevel);
        codeArea.setStyle(newStyle);
        updateModernLineNumbers();
    }

    private void runParser() {
        terminalArea.setText("> Running parser...\n");

        // Имитация работы парсера
        new Thread(() -> {
            try {
                Thread.sleep(800);
                javafx.application.Platform.runLater(() -> {
                    terminalArea.appendText("> Parser completed successfully!\n");
                    terminalArea.appendText("> No errors found.\n");
                    terminalArea.appendText("> Ready for next analysis.\n\n");
                });
            } catch (InterruptedException e) {
                javafx.application.Platform.runLater(() ->
                        terminalArea.appendText("> Error: Parser interrupted\n"));
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}