package parser;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ParserIDE extends Application {

    private TextArea codeArea;
    private TextArea lineNumbers;
    private TextArea terminalArea;
    private double fontSize = 14;
    private double zoomLevel = 1.0;

    @Override
    public void start(Stage primaryStage) {
        // Главный контейнер
        BorderPane root = new BorderPane();

        // Верхняя панель с меню и элементами управления
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(createMenuBar(), createToolBar());
        root.setTop(topContainer);

        // Центральная часть с редактором кода
        root.setCenter(createCodeEditor());

        // Нижняя часть с терминалом
        root.setBottom(createTerminal());

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Language Parser IDE");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar() {
        // Меню File
        Menu fileMenu = new Menu("File");
        MenuItem open = new MenuItem("Open");
        MenuItem save = new MenuItem("Save");
        MenuItem close = new MenuItem("Close");
        fileMenu.getItems().addAll(open, save, close);

        // Меню Text
        Menu textMenu = new Menu("Text");
        MenuItem documentation = new MenuItem("Documentation");
        MenuItem grammar = new MenuItem("Grammar");
        textMenu.getItems().addAll(documentation, grammar);

        // Меню View
        Menu viewMenu = new Menu("View");
        MenuItem zoomIn = new MenuItem("Zoom In");
        MenuItem zoomOut = new MenuItem("Zoom Out");
        MenuItem increaseFont = new MenuItem("Increase Font Size");
        MenuItem decreaseFont = new MenuItem("Decrease Font Size");
        viewMenu.getItems().addAll(zoomIn, zoomOut, increaseFont, decreaseFont);

        // Меню Run
        Menu runMenu = new Menu("Run");

        // Обработчики для View меню
        zoomIn.setOnAction(e -> zoom(0.1));
        zoomOut.setOnAction(e -> zoom(-0.1));
        increaseFont.setOnAction(e -> changeFontSize(1));
        decreaseFont.setOnAction(e -> changeFontSize(-1));

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, textMenu, viewMenu, runMenu);
        return menuBar;
    }

    private ToolBar createToolBar() {
        // Кнопка проекта с меню
        MenuButton projectMenu = new MenuButton("Project Name");
        MenuItem newFile = new MenuItem("New File");
        MenuItem openFile = new MenuItem("Open File");
        projectMenu.getItems().addAll(newFile, openFile);

        // Ползунок масштаба
        Slider zoomSlider = new Slider(0.5, 2.0, 1.0);
        zoomSlider.setOrientation(Orientation.HORIZONTAL);
        zoomSlider.setPrefWidth(150);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoomLevel = newVal.doubleValue();
            updateFontSize();
        });

        // Кнопка запуска парсера
        Button runParserBtn = new Button("Run Parser");
        runParserBtn.setOnAction(e -> runParser());

        // Панель инструментов
        ToolBar toolbar = new ToolBar();
        toolbar.getItems().addAll(projectMenu, new Separator(), zoomSlider, runParserBtn);
        return toolbar;
    }

    private HBox createCodeEditor() {
        HBox editorContainer = new HBox();

        // Панель номеров строк
        lineNumbers = new TextArea();
        lineNumbers.setEditable(false);
        lineNumbers.setStyle("-fx-font-family: monospace; -fx-text-fill: gray;");
        lineNumbers.setPrefWidth(50);
        lineNumbers.setPrefHeight(Double.MAX_VALUE);

        // Область редактирования кода
        codeArea = new TextArea();
        codeArea.setStyle("-fx-font-family: monospace;");
        codeArea.setPrefHeight(Double.MAX_VALUE);

        // Синхронизация прокрутки
        codeArea.textProperty().addListener((obs, oldVal, newVal) -> updateLineNumbers());
        codeArea.scrollTopProperty().addListener((obs, oldVal, newVal) ->
                lineNumbers.setScrollTop(newVal.doubleValue()));

        editorContainer.getChildren().addAll(lineNumbers, codeArea);
        HBox.setHgrow(codeArea, Priority.ALWAYS);
        updateLineNumbers();

        return editorContainer;
    }

    private VBox createTerminal() {
        VBox terminalContainer = new VBox();
        terminalContainer.setPadding(new Insets(5));

        Label terminalLabel = new Label("Terminal");
        terminalArea = new TextArea();
        terminalArea.setStyle("-fx-font-family: monospace;");
        terminalArea.setPrefHeight(150);
        terminalArea.setEditable(false);

        terminalContainer.getChildren().addAll(terminalLabel, terminalArea);
        VBox.setVgrow(terminalArea, Priority.ALWAYS);

        return terminalContainer;
    }

    private void updateLineNumbers() {
        String[] lines = codeArea.getText().split("\n");
        StringBuilder numbers = new StringBuilder();
        for (int i = 1; i <= lines.length; i++) {
            numbers.append(i).append("\n");
        }
        lineNumbers.setText(numbers.toString());
    }

    private void zoom(double delta) {
        zoomLevel += delta;
        zoomLevel = Math.max(0.5, Math.min(2.0, zoomLevel));
        updateFontSize();
    }

    private void changeFontSize(int delta) {
        fontSize += delta;
        fontSize = Math.max(8, Math.min(72, fontSize));
        updateFontSize();
    }

    private void updateFontSize() {
        String style = String.format("-fx-font-size: %.0fpt;", fontSize * zoomLevel);
        codeArea.setStyle("-fx-font-family: monospace; " + style);
        lineNumbers.setStyle("-fx-font-family: monospace; -fx-text-fill: gray; " + style);
        terminalArea.setStyle("-fx-font-family: monospace; " + style);
    }

    private void runParser() {
        // Заглушка для запуска парсера
        terminalArea.setText("Running parser...\n");
        // Здесь будет логика парсера
    }

    public static void main(String[] args) {
        launch(args);
    }
}