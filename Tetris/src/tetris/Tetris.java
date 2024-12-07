package tetris;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.util.Date;
import java.util.Random;

public class Tetris extends Application {
    // Константы для размеров игрового поля и плиток	
    private static final int WIDTH = 10;
    private static final int HEIGHT = 20;
    private static final int TILE_SIZE = 35;
    private static final int WIDTH_mini = 5;
    private static final int HEIGHT_mini = 5;
    private static final int TILE_SIZE_mini = 30;
    
    // Определения фигур тетромино (каждая фигура представлена двумерным массивом)
    private static final int[][][] TETROMINOES = {
            // I
            {
                    { 1, 1, 1, 1 },
                    { 0, 0, 0, 0 },
                    { 0, 0, 0, 0 },
                    { 0, 0, 0, 0 }
            },
            // O
            {
                    { 2, 2 },
                    { 2, 2 }
            },
            // T
            {
                    { 0, 3, 0 },
                    { 3, 3, 3 },
                    { 0, 0, 0 }
            },
            // S
            {
                    { 0, 4, 4 },
                    { 4, 4, 0 },
                    { 0, 0, 0 }
            },
            // Z
            {
                    { 5, 5, 0 },
                    { 0, 5, 5 },
                    { 0, 0, 0 }
            },
            // J
            {
                    { 6, 0, 0 },
                    { 6, 6, 6 },
                    { 0, 0, 0 }
            },
            // L
            {
                    { 0, 0, 7 },
                    { 7, 7, 7 },
                    { 0, 0, 0 }
            }
    };
    
    // Основные элементы UI
    private GridPane grid = new GridPane();
    private GridPane grid_mini = new GridPane();
    private Rectangle[][] tiles = new Rectangle[HEIGHT][WIDTH]; // Массив плиток (прямоугольников)
    private Rectangle[][] tiles_mini = new Rectangle[HEIGHT][WIDTH]; 
    private Label scoreLabel = new Label("Score: 0   Speed: 1"); 
    private Button restartButton = new Button("Restart"); 
    private Button pauseButton = new Button("Pause");
    
    // Игровое поле и фигуры
    private int[][] field = new int[HEIGHT][WIDTH]; // Массив для хранения состояния игрового поля
    private int[][] field_mini = new int[HEIGHT_mini][WIDTH_mini];
    private int[][] currentTetromino; 
    private int[][] nextTetromino;
    private int currentX, currentY; 
    private int nextX, nextY;
    private int currentTetrominoType;
    private int nextTetrominoType;
    
    // Игровые переменные
    public int score = 0; 
    public int oldscore = 0; 
    public int scorecount = 100;
    public int timecount = 800;
    public int speed = 1;
    public int countpause = 1;
    private boolean isPause = false;
    private boolean gameOver = false;
    private boolean next = false;
    private Timeline timeline;
    
    @Override
    public void start(Stage primaryStage) {
        initializeGrid(); 
        initializeGridMini();
        BorderPane root = new BorderPane(); 
        BorderPane.setMargin(grid_mini, new Insets(60));
        root.setCenter(grid);
        root.setRight(grid_mini);
        
        scoreLabel.setStyle("-fx-font-size: 20;");
        BorderPane.setAlignment(scoreLabel, Pos.TOP_LEFT);
        BorderPane.setMargin(scoreLabel, new Insets(10));
        root.setTop(scoreLabel);
        
        
        restartButton.setOnAction(e -> restartGame());
        restartButton.setVisible(false);
        VBox bottomBox = new VBox(restartButton);
        bottomBox.setAlignment(Pos.CENTER);
        root.setBottom(bottomBox);
        
        
        Scene scene = new Scene(root, WIDTH * TILE_SIZE + 300, HEIGHT * TILE_SIZE + 50 + 100); // Создание сцены
        scene.setOnKeyPressed(e -> handleKeyPress(e.getCode())); // Обработка нажатий клавиш
        primaryStage.setTitle("Tetris"); // Заголовок окна
        primaryStage.setScene(scene); // Установка сцены
        primaryStage.show(); // Отображение окна
        newTetromino(); // Создание новой фигуры
        nextTetromino();
        timeline = new Timeline(new KeyFrame(Duration.millis(timecount), e -> tick())); 
        timeline.setCycleCount(Animation.INDEFINITE); 
        timeline.play(); 
    }
    
    // Инициализация сетки игрового поля
    private void initializeGrid() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                tiles[y][x] = new Rectangle(TILE_SIZE, TILE_SIZE); // Создание плитки
                tiles[y][x].setFill(Color.WHITE); // Цвет плитки
                tiles[y][x].setStroke(Color.GRAY); // Обводка плитки
                grid.add(tiles[y][x], x, y); // Добавление плитки в сетку
            }
        }
    }
    
    private void initializeGridMini() {
        for (int y = 0; y < HEIGHT_mini; y++) {
            for (int x = 0; x < WIDTH_mini; x++) {
            	tiles_mini[y][x] = new Rectangle(TILE_SIZE_mini, TILE_SIZE_mini); // Создание плитки
            	tiles_mini[y][x].setFill(Color.WHITE); // Цвет плитки
            	tiles_mini[y][x].setStroke(Color.GRAY); // Обводка плитки
                grid_mini.add(tiles_mini[y][x], x, y); // Добавление плитки в сетку
            }
        }
    }
    
    // Игровой цикл
    private void tick() {
    	scoreCounter(score, oldscore);
        if (gameOver)
            return; // Если игра окончена, выход
        // Если нет столкновения при движении вниз, двигаем фигуру
        if (!isCollision(currentX, currentY + 1, currentTetromino)) {
            moveTetromino(0, 1);
        } 
        
        else {
            solidifyTetromino(); // Фиксируем фигуру
            clearLines(); // Удаляем заполненные линии
            newTetromino(); // Создаем новую фигуру
            nextTetromino();
        }
    }
    
    // Создание новой фигуры
    private void newTetromino() {
    	if(!next) {
        Random rand = new Random();
        	currentTetrominoType = rand.nextInt(TETROMINOES.length); // Выбор случайной фигуры
        	currentTetromino = TETROMINOES[currentTetrominoType]; // Установка текущей фигуры
        	next = true;
    	}
    	else {
    		currentTetrominoType = nextTetrominoType; 
        	currentTetromino = TETROMINOES[nextTetrominoType]; 
    	}
        currentX = (WIDTH / 2) - (currentTetromino[0].length / 2); // Начальная позиция X
        currentY = 0; // Начальная позиция Y
        // Проверка на конец игры
        if (isCollision(currentX, currentY, currentTetromino)) {
            gameOver = true;
            timeline.stop();
            displayGameOver(); // Отображение конца игры
            return;
        }
        
        drawTetromino(true); // Отрисовка фигуры
    }
    
    private void nextTetromino() {
    	clearMiniField();
        Random rand = new Random();
        nextTetrominoType = rand.nextInt(TETROMINOES.length); // Выбор случайной фигуры
        nextTetromino = TETROMINOES[nextTetrominoType]; // Установка текущей фигуры
        nextX = (WIDTH_mini / 2) - (nextTetromino[0].length / 2); // Начальная позиция X
        nextY = (HEIGHT_mini / 2) - (nextTetromino[0].length / 2); // Начальная позиция Y
        
        drawNextTetromino(true); // Отрисовка фигуры
    }
    
    
    // Обработка нажатий клавиш
    private void handleKeyPress(javafx.scene.input.KeyCode code) {
        if (gameOver)
            return; // Если игра окончена, игнорируем нажатия
        switch (code) {
        	case SPACE:
        		pauseGame(); // Пауза
        		break;
            case LEFT:
            	if(!isPause) {
            		moveTetromino(-1, 0); // Движение влево
            	}
                break;
            case RIGHT:
            	if(!isPause) {
            		moveTetromino(1, 0); // Движение вправо
            	}
                break;
            case DOWN:
            	if(!isPause) {
            		moveTetromino(0, 1); // Движение вниз
            	}
                break;
            case UP:
            	if(!isPause) {
            		rotateTetromino(); // Поворот фигуры
            	}
                break;
        }
    }
    
    // Движение фигуры
    private void moveTetromino(int dx, int dy) {
        if (!isCollision(currentX + dx, currentY + dy, currentTetromino)) {
            drawTetromino(false); // Стираем фигуру на старой позиции
            currentX += dx; // Обновляем позицию X
            currentY += dy; // Обновляем позицию Y
            drawTetromino(true); // Рисуем фигуру на новой позиции
        }
    }
    
    // Поворот фигуры
    private void rotateTetromino() {
        int[][] rotatedTetromino = rotateMatrix(currentTetromino); // Поворачиваем матрицу фигуры
        if (!isCollision(currentX, currentY, rotatedTetromino)) {
            drawTetromino(false); // Стираем фигуру на старой позиции
            currentTetromino = rotatedTetromino; // Обновляем фигуру
            drawTetromino(true); // Рисуем фигуру на новой позиции
        }
    }
    
    // Проверка на столкновение
    private boolean isCollision(int x, int y, int[][] tetromino) {
        for (int row = 0; row < tetromino.length; row++) {
            for (int col = 0; col < tetromino[row].length; col++) {
                if (tetromino[row][col] != 0) { // Если клетка фигуры не пустая
                    int fieldX = x + col;
                    int fieldY = y + row;
                    // Проверка границ игрового поля и столкновения с другими фигурами
                    if (fieldX < 0 || fieldX >= WIDTH || fieldY >= HEIGHT
                            || (fieldY > -1 && field[fieldY][fieldX] != 0)) {
                        return true; // Столкновение
                    }
                }
            }
        }
        return false; // Нет столкновения
    }
    
    // Поворот матрицы (фигуры) на 90 градусов по часовой стрелке
    private int[][] rotateMatrix(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] rotatedMatrix = new int[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rotatedMatrix[j][rows - 1 - i] = matrix[i][j];
            }
        }
        return rotatedMatrix;
    }
    
    // Фиксация фигуры на игровом поле
    private void solidifyTetromino() {
        for (int row = 0; row < currentTetromino.length; row++) {
            for (int col = 0; col < currentTetromino[row].length; col++) {
                if (currentTetromino[row][col] != 0) {
                    int fieldX = currentX + col;
                    int fieldY = currentY + row;
                    if (fieldY >= 0)
                        field[fieldY][fieldX] = currentTetrominoType + 1; // Записываем тип фигуры на поле
                }
            }
        }
        drawField(); // Перерисовываем поле
    }
    
    // Удаление заполненных линий
    private void clearLines() {
        int linesCleared = 0;
        for (int y = 0; y < HEIGHT; y++) {
            if (isLineFull(y)) { 
                linesCleared++;
                clearLine(y); // Очищаем линию
                drawField(); // Отрисовываем обновленное поле
            }
        }
        score += linesCleared * linesCleared * scorecount; // Начисление очков
        updateScoreLabel(); // Обновление отображения счета
    }
    
    // Проверка, заполнена ли линия
    private boolean isLineFull(int y) {
        for (int x = 0; x < WIDTH; x++) {
            if (field[y][x] == 0) { // Если есть пустая клетка, линия не заполнена
                return false;
            }
        }
        return true; 
    }
    
    // Очистка линии
    private void clearLine(int y) {
        for (int x = 0; x < WIDTH; x++) {
            field[y][x] = 0; // Устанавливаем все клетки линии в 0 (пустые)
        }
        shiftLinesDown(y);
    }
    
    // Сдвиг линий вниз после очистки
    private void shiftLinesDown(int clearedLine) {
        for (int y = clearedLine; y > 0; y--) {
            System.arraycopy(field[y - 1], 0, field[y], 0, WIDTH); // Копируем линию сверху
        }
        for (int x = 0; x < WIDTH; x++) {
            field[0][x] = 0; // Очищаем верхнюю линию
        }
    }
    
    // Отрисовка тетромино
    private void drawTetromino(boolean on) {
        for (int row = 0; row < currentTetromino.length; row++) {
            for (int col = 0; col < currentTetromino[row].length; col++) {
                if (currentTetromino[row][col] != 0) {
                    int fieldX = currentX + col;
                    int fieldY = currentY + row;
                    if (fieldY >= 0 && fieldY < HEIGHT && fieldX >= 0 && fieldX < WIDTH) {
                        tiles[fieldY][fieldX].setFill(on ? getColor(currentTetrominoType) : Color.WHITE); // Устанавливаем цвет
                                                                                                           
                    }
                }
            }
        }
    }
    
    private void drawNextTetromino(boolean on) {
        for (int row = 0; row < nextTetromino.length; row++) {
            for (int col = 0; col < nextTetromino[row].length; col++) {
                if (nextTetromino[row][col] != 0) {
                    int fieldX = nextX + col;
                    int fieldY = nextY + row;
                    if (fieldY >= 0 && fieldY < HEIGHT_mini && fieldX >= 0 && fieldX < WIDTH_mini) {
                        tiles_mini[fieldY][fieldX].setFill(on ? getColor(nextTetrominoType) : Color.BLACK); // Устанавливаем цвет
                                                                                                           
                    }
                }
            }
        }
    }
    
    // Отрисовка игрового поля
    private void drawField() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                tiles[y][x].setFill(field[y][x] != 0 ? getColor(field[y][x] - 1) : Color.WHITE);
            }
        }
    }
    
    private void drawMiniField() {
        for (int y = 0; y < HEIGHT_mini; y++) {
            for (int x = 0; x < WIDTH_mini; x++) {
                tiles_mini[y][x].setFill(field_mini[y][x] != 0 ? getColor(field[y][x] - 1) : Color.WHITE);
            }
        }
    }
    
    // Обновление отображения счета
    private void updateScoreLabel() {
        scoreLabel.setText("Счет: " + score + "  Cкорость: " + speed);
    }
    
    private void scoreCounter(int score, int old) {
    	if((score - old) > 1000) {
    		System.out.println(score - old);
    		if(timecount > 200) {
        		timecount -= 100;
        		speed += 1;
        		timeline.stop();
        		timeline.getKeyFrames().clear();
                timeline = new Timeline(new KeyFrame(Duration.millis(timecount), e -> tick())); // Создание таймера
                timeline.setCycleCount(Animation.INDEFINITE); // Бесконечный цикл таймера
                timeline.play(); // Запуск таймера
        		System.out.println("timecount:");
        		System.out.println(timecount);
    		}
    		oldscore = score;
    		scorecount += 10;
    		System.out.println("scorecount:");
    		System.out.println(scorecount);
    		
    	}
    }
    
    // Отображение конца игры
    private void displayGameOver() {
    	FileWriter writer;
    	Date currentDate = new Date();
		try {
			writer = new FileWriter("Score.txt", true);
			System.out.println("Date: " + currentDate + "   Score: " + score);
	    	writer.write("Date: " + currentDate + "   Score: " + score + "\n");
	    	writer.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error");
		}
		
        scoreLabel.setText("Игра окончена! Счет: " + score);
        restartButton.setVisible(true); 
        pauseButton.setVisible(true);
    }

    //Пaуза
    private void pauseGame(){
    	countpause += 1;
    	if(countpause % 2 == 0) {
            timeline.stop(); 
            isPause = true;
    	}
    	else {
            isPause = false;
    		timeline.play();
    	}
    	
    }
    
    // Перезапуск игры
    private void restartGame() {
        oldscore = 0; 
        scorecount = 100;
        speed = 1;
        countpause = 1;
        isPause = false;
        next = false;
        gameOver = false;
        score = 0;
        timecount = 800;
        updateScoreLabel();
        pauseButton.setVisible(true);
        restartButton.setVisible(false);
        clearField(); 
        newTetromino(); 
        timeline.play(); 
    }
    
    // Очистка игрового поля
    private void clearField() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                field[y][x] = 0; // Обнуляем все клетки поля
            }
        }
        drawField(); // Перерисовываем поле
    }
    
    private void clearMiniField() {
        for (int y = 0; y < HEIGHT_mini; y++) {
            for (int x = 0; x < WIDTH_mini; x++) {
                field_mini[y][x] = 0; // Обнуляем все клетки поля
            }
        }
        drawMiniField(); // Перерисовываем поле
    }
    
    // Метод для получения цвета тетромино по его типу
    private Color getColor(int tetrominoType) {
        switch (tetrominoType) {
            case 0: // I
                return Color.CYAN;
            case 1: // O
                return Color.YELLOW;
            case 2: // T
                return Color.GREEN;
            case 3: // S
                return Color.PURPLE;
            case 4: // Z
                return Color.BLUE;
            case 5: // J
                return Color.RED;
            case 6: // L
                return Color.ORANGE;
            default:
                return Color.WHITE;
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}