package TetrisGA;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.Scanner;
import javax.swing.JFrame;


/**
 * The {@code Tetris} class is responsible for handling much of the game logic and
 * reading user input.
 * @author Brendan Jones
 *
 */
public class Tetris extends JFrame {
    /**
     *
     *
     *  BoardPanel.heightWeight = manualChromosome[0];
     BoardPanel.linesWeight = manualChromosome[1];
     BoardPanel.holesWeight = manualChromosome[2];
     BoardPanel.bumbinesWeight = manualChromosome[3];
     BoardPanel.blockadesWeight = manualChromosome[4];
     *
     *
     */

    private double[] manualChromosome={-2.841884818353364 ,90.33398858013996 ,-86.29884523976246, -17.97583422646018, -3.496915438232847 ,0};


    private boolean geneticMode=false;
    private int bestRotation;
    private int bestXpos;
    private double bestPoints;

    private int ghostRow;
    boolean testMode = false;

    private int gamesCounter = geneticOperations.numberOfGames;
    private int chromosomeCounter=geneticOperations.populationSize;


    /**
     * The Serial Version UID.
     */
    private static final long serialVersionUID = -4722429764792514382L;


    /**
     * The number of milliseconds per frame.
     */
    private static final long FRAME_TIME = 500L / 30L;

    /**
     * The number of pieces that exist.
     */
    private static final int TYPE_COUNT = TileType.values().length;

    /**
     * The BoardPanel instance.
     */
    public BoardPanel board;

    /**
     * The SidePanel instance.
     */
    private SidePanel side;

    /**
     * Whether or not the game is paused.
     */
    private boolean isPaused;

    /**
     * Whether or not we've played a game yet. This is set to true
     * initially and then set to false when the game starts.
     */
    private boolean isNewGame;

    /**
     * Whether or not the game is over.
     */
    private boolean isGameOver;

    /**
     * The current score.
     */
    private int score;

    /**
     * The random number generator. This is used to
     * spit out pieces randomly.
     */
    private Random random;

    /**
     * The clock that handles the update logic.
     */
    private static Clock logicTimer;

    /**
     * The current type of tile.
     */
    private TileType currentType;

    /**
     * The next type of tile.
     */
    private TileType nextType;

    /**
     * The current column of our tile.
     */
    private int currentCol;

    /**
     * The current row of our tile.
     */
    private int currentRow;

    /**
     * The current rotation of our tile.
     */
    private int currentRotation;

    /**
     * Ensures that a certain amount of time passes after a piece is
     * spawned before we can drop it.
     */
    private int dropCooldown;

    /**
     * The speed of the game.
     */
    private static float gameSpeed;

    /**
     * Creates a new Tetris instance. Sets up the window's properties,
     * and adds a controller listener.
     */
    public Tetris() {
		/*
		 * Set the basic properties of the window.
		 */

        super("Tetris");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);


		/*
		 * Initialize the BoardPanel and SidePanel instances.
		 */

        board = new BoardPanel(this);
        side = new SidePanel(this);


		/*
		 * Add the BoardPanel and SidePanel instances to the window.
		 */
        add(board, BorderLayout.CENTER);
        add(side, BorderLayout.EAST);

		/*
		 * Adds a custom anonymous KeyListener to the frame.
		 */
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                switch(e.getKeyCode()) {

				/*
				 * Drop - When pressed, we check to see that the game is not
				 * paused and that there is no drop cooldown, then set the
				 * logic timer to run at a speed of 25 cycles per second.
				 */


                    case KeyEvent.VK_S:
                        if(!isPaused && dropCooldown == 0) {
                            logicTimer.setCyclesPerSecond(250.0f);
                            boolean notOnBottom=true;
                            while(notOnBottom)
                            {
                                if(board.isValidAndEmpty(currentType, currentCol, currentRow + 1, currentRotation)) {
                                    //Increment the current row if it's safe to do so.
                                    currentRow++;
                                } else {
                                notOnBottom=false;
                                }
                            }

                        }
                        break;

				/*
				 * Move Left - When pressed, we check to see that the game is
				 * not paused and that the position to the left of the current
				 * position is valid. If so, we decrement the current column by 1.
				 */
                    case KeyEvent.VK_A:
                        if(!isPaused && board.isValidAndEmpty(currentType, currentCol - 1, currentRow, currentRotation)) {
                            currentCol--;


                        }
                        break;

				/*
				 * Move Right - When pressed, we check to see that the game is
				 * not paused and that the position to the right of the current
				 * position is valid. If so, we increment the current column by 1.
				 */
                    case KeyEvent.VK_D:
                        if(!isPaused && board.isValidAndEmpty(currentType, currentCol + 1, currentRow, currentRotation)) {
                            currentCol++;
                        }
                        break;

				/*
				 * Rotate Anticlockwise - When pressed, check to see that the game is not paused
				 * and then attempt to rotate the piece anticlockwise. Because of the size and
				 * complexity of the rotation code, as well as it's similarity to clockwise
				 * rotation, the code for rotating the piece is handled in another method.
				 */
                    case KeyEvent.VK_Q:
                        if(!isPaused) {
                            rotatePiece((currentRotation == 0) ? 3 : currentRotation - 1);
                        }
                        break;

				/*
			     * Rotate Clockwise - When pressed, check to see that the game is not paused
				 * and then attempt to rotate the piece clockwise. Because of the size and
				 * complexity of the rotation code, as well as it's similarity to anticlockwise
				 * rotation, the code for rotating the piece is handled in another method.
				 */
                    case KeyEvent.VK_E:
                        if(!isPaused) {
                            rotatePiece((currentRotation == 3) ? 0 : currentRotation + 1);
                        }
                        break;

				/*
				 * Pause Game - When pressed, check to see that we're currently playing a game.
				 * If so, toggle the pause variable and update the logic timer to reflect this
				 * change, otherwise the game will execute a huge number of updates and essentially
				 * cause an instant game over when we unpause if we stay paused for more than a
				 * minute or so.
				 */
                    case KeyEvent.VK_P:
                        if(!isGameOver && !isNewGame) {
                            isPaused = !isPaused;
                            logicTimer.setPaused(isPaused);
                        }
                        break;

				/*
				 * Start Game - When pressed, check to see that we're in either a game over or new
				 * game state. If so, reset the game.
				 */
                    case KeyEvent.VK_ENTER:
                        if(isGameOver || isNewGame) {
                            resetGame(0);
                        }
                        break;

                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

                switch(e.getKeyCode()) {

				/*
				 * Drop - When released, we set the speed of the logic timer
				 * back to whatever the current game speed is and clear out
				 * any cycles that might still be elapsed.
				 */
                    case KeyEvent.VK_S:
                        logicTimer.setCyclesPerSecond(gameSpeed);
                        logicTimer.reset();
                        break;
                }

            }

        });

		/*
		 * Here we resize the frame to hold the BoardPanel and SidePanel instances,
		 * center the window on the screen, and show it to the user.
		 */
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Starts the game running. Initializes everything and enters the game loop.
     */
    private  void startGame() {
		/*
		 * Initialize our random number generator, logic timer, and new game variables.
		 */

		if(geneticMode){geneticOperations.generateFirstPopulation();}else {
            BoardPanel.heightWeight = manualChromosome[0];
            BoardPanel.linesWeight = manualChromosome[1];
            BoardPanel.holesWeight = manualChromosome[2];
            BoardPanel.bumbinesWeight = manualChromosome[3];
            BoardPanel.blockadesWeight = manualChromosome[4];
        }
        this.random = new Random();
        this.isNewGame = true;
        this.gameSpeed = 999f;

		/*
		 * Setup the timer to keep the game from running before the user presses enter
		 * to start it.
		 */
        this.logicTimer = new Clock(gameSpeed);
        logicTimer.setPaused(true);

        while(true) {



            //Get the time that the frame started.
            long start = System.nanoTime();

            //Update the logic timer.
            logicTimer.update();

			/*
			 * If a cycle has elapsed on the timer, we can update the game and
			 * move our current piece down.
			 */
            if(logicTimer.hasElapsedCycle()) {
                updateGame();
            }

            //Decrement the drop cool down if necessary.
            if(dropCooldown > 0) {
                dropCooldown--;
            }


            //Display the window to the user.
            renderGame();

			/*
			 * Sleep to cap the framerate.
			 */
            long delta = (System.nanoTime() - start) / 1L;
            if(delta < FRAME_TIME) {
                try {
                    Thread.sleep(FRAME_TIME - delta);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Updates the game and handles the bulk of it's logic.
     */
    private void updateGame() {
		/*
		 * Check to see if the piece's position can move down to the next row.
		 */

        if(board.isValidAndEmpty(currentType, currentCol, currentRow + 1, currentRotation)) {
            //Increment the current row if it's safe to do so.
           //currentRow++;
        } else {
			/*
			 * We've either reached the bottom of the board, or landed on another piece, so
			 * we need to add the piece to the board.
			 */
            board.addPiece(currentType, currentCol, currentRow, currentRotation);

			/*
			 * Check to see if adding the new piece resulted in any cleared lines. If so,
			 * increase the player's score. (Up to 4 lines can be cleared in a single go;
			 * [1 = 100pts, 2 = 200pts, 3 = 400pts, 4 = 800pts]).
			 */
            int cleared = board.checkLines();
            if(cleared > 0) {
                score += 50 << cleared;
            }

			/*
			 * Increase the speed slightly for the next piece and update the game's timer
			 * to reflect the increase.
			 */
            logicTimer.setCyclesPerSecond(gameSpeed);
            logicTimer.reset();

			/*
			 * Set the drop cooldown so the next piece doesn't automatically come flying
			 * in from the heavens immediately after this piece hits if we've not reacted
			 * yet. (~0.5 second buffer).
			 */
            dropCooldown = 0;

			/*
			 * Update the difficulty level. This has no effect on the game, and is only
			 * used in the "Level" string in the SidePanel.
			 */

			/*
			 * Spawn a new piece to control.
			 */
            spawnPiece();
        }
    }

    /**
     * Forces the BoardPanel and SidePanel to repaint.
     */
    private void renderGame() {
        board.repaint();
        side.repaint();

    }

    public static void setGameSpeed(float gs) {
        gameSpeed = gs;
    }

    public static float getGameSpeed() {
        return gameSpeed;
    }

    /**
     * Resets the game variables to their default values at the start
     * of a new game.
     */
    private void resetGame(int geneNumber) {



        //this.gameSpeed = 3.0f;
        this.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
        this.isNewGame = false;
        this.isGameOver = false;
        board.clear();
        logicTimer.reset();
        logicTimer.setCyclesPerSecond(gameSpeed);



        if(geneticMode) {
            geneticOperations.chromosome[geneticOperations.currentChromosome][5] += score;
            BoardPanel.heightWeight = geneticOperations.chromosome[geneNumber][0];
            BoardPanel.linesWeight = geneticOperations.chromosome[geneNumber][1];
            BoardPanel.holesWeight = geneticOperations.chromosome[geneNumber][2];
            BoardPanel.bumbinesWeight = geneticOperations.chromosome[geneNumber][3];
            BoardPanel.blockadesWeight = geneticOperations.chromosome[geneNumber][4];
        } else {
            BoardPanel.heightWeight = manualChromosome[0];
            BoardPanel.linesWeight = manualChromosome[1];
            BoardPanel.holesWeight = manualChromosome[2];
            BoardPanel.bumbinesWeight = manualChromosome[3];
            BoardPanel.blockadesWeight = manualChromosome[4];
        }
        this.score = 0;
            spawnPiece();
    }

    /**
     * Spawns a new piece and resets our piece's variables to their default
     * values.
     */
    private void spawnPiece() {
		/*
		 * Poll the last piece and reset our position and rotation to
		 * their default variables, then pick the next piece to use.
		 */
        this.currentType = nextType;
/////       ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // sprawdzanie czy klocek moze byc normalnie wygenerowany
        // jak nie to konczy gre. jak tak to konczy generowanie klocka i odpala bruta
        for(int c=0;c<10;c++){
            if(BoardPanel.tablica[4][c]!=0) {

                for(int i = 0; i < 10; i++){
                    for(int j = 0; j < 21; j++) {
                    BoardPanel.tablica[j][i]=0;
                    }}

                this.isGameOver = true;
                logicTimer.setPaused(true);

                //System.out.println("score: "+ getScore() + "genes " + BoardPanel.linesWeight );
               // startGame();
                if(geneticMode) {
                    if (chromosomeCounter == 1) {
                        gamesCounter = geneticOperations.numberOfGames;
                        geneticOperations.getNewPopulation();
                        gamesCounter=geneticOperations.numberOfGames;
                        chromosomeCounter=geneticOperations.populationSize;
                        geneticOperations.currentChromosome=0;
                    } else if (gamesCounter == 0) {
                        geneticOperations.chromosome[geneticOperations.currentChromosome][5] /= geneticOperations.numberOfGames;
                        System.out.println( "Score: " + String.format("%.0f", geneticOperations.chromosome[geneticOperations.currentChromosome][5]));
                        geneticOperations.currentChromosome++;
                        gamesCounter = geneticOperations.numberOfGames;
                        chromosomeCounter--;
                    }
                    resetGame(geneticOperations.currentChromosome);
                    gamesCounter--;
                }else {
                    System.out.println("Score: " + getScore());
                    resetGame(0);
                }
            }
        }
        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        this.currentCol = currentType.getSpawnColumn();
        this.currentRow = currentType.getSpawnRow();
        this.currentRotation = 0;
        this.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
		/*
		 * If the spawn point is invalid, we need to pause the game and flag that we've lost
		 * because it means that the pieces on the board have gotten too high.
         */
        bestPoints=-999999999;
        bestRotation=0;
        bestXpos=0;
        bruteForce();

        if(!isPaused && dropCooldown == 0) {
            logicTimer.setCyclesPerSecond(1.0f);
            boolean notOnBottom=true;
            while(notOnBottom)
            {
                if(board.isValidAndEmpty(currentType, currentCol, currentRow + 1, currentRotation)) {
                    //Increment the current row if it's safe to do so.
                    currentRow++;
                } else {
                    notOnBottom=false;
                }
            }
        }
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Attempts to move piece to every possible column with every possible rotation.
     */
private boolean canMoveRight(){

    if(!isPaused && board.isValidAndEmpty(currentType, currentCol + 1, currentRow, currentRotation)) {

        return true;
    }else {return false;
    }
}

    private boolean canMoveDown(){

        if(!isPaused && board.isValidAndEmpty(currentType, currentCol , ghostRow, currentRotation)) {

            return true;
        }else {return false;
        }
    }

    private boolean canMoveLeft(){

        if(!isPaused && board.isValidAndEmpty(currentType, currentCol-1 , currentRow, currentRotation)) {

            return true;
        }else {return false;
        }
    }


private void bruteForce() {

    if(testMode == true) {
        for (int i=0; i<100;i++){
            if(testMode == true) System.out.println();}
    }

    for (int Rotacje = 0; Rotacje < 4; Rotacje++) {
        if(testMode == true)  System.out.println("rotated" + currentRotation);
        currentRotation = Rotacje;
        while(canMoveLeft()){currentCol-=1;}
        currentCol-=1;
        while (canMoveRight()) {
            if(testMode == true)  System.out.println("==================================================" );
            if(testMode == true)  System.out.println("==================================================" );
            if(testMode == true)  System.out.println("==================================================" );

            currentCol++;

            if(testMode == true) System.out.println("moved right" + currentCol + " rota " + currentRotation);

            for (int x = 0; x < BoardPanel.COL_COUNT; x++) {

                for (int y = BoardPanel.HIDDEN_ROW_COUNT; y < BoardPanel.ROW_COUNT; y++) {
                    TileType tile = BoardPanel.getTile(x, y);
                    if (tile != null) {

                        //dodanie do tablicy normalnych klockow
                        BoardPanel.tablica[y][x] = 1;
                        //System.out.print(tablica[x][y] + " ");

                    } else {
                        //wpisanie do tablicy zer jezeli nie ma klocka
                        BoardPanel.tablica[y][x] = 0;


                    }
                }
            }
            if(testMode == true) System.out.println("dopisalem klocki " + currentRow );
            if(testMode == true)  System.out.println("dimenszyn  " + BoardPanel.ghostX);

            ghostRow=0;
            while(canMoveDown()){
                ghostRow+=1;

            }
            if(testMode == true) System.out.println("ROW  " + ghostRow);
            for(int col = 0; col < currentType.getDimension(); col++) {
                for(int row = 0; row < currentType.getDimension(); row++) {


                    if( currentType.isTile(col, row, currentRotation)) {

// dopisanie do tablicy ghost klockow
                        //                 row / col
                        BoardPanel.tablica[ghostRow -1 + row][currentCol+col] = 2;
                    }
                }
            }

                for (int y = BoardPanel.HIDDEN_ROW_COUNT; y < BoardPanel.ROW_COUNT; y++) {
                    for (int x = 0; x < BoardPanel.COL_COUNT; x++) {

                        if(testMode == true)    System.out.print(BoardPanel.tablica[y][x] + " ");

                    }
                    if(testMode == true) System.out.println();
                }

            if(testMode == true)   {
                System.out.println("punkty dla tego ruchu : "+ BoardPanel.calculatePoints(BoardPanel.tablica) );
                System.out.println("==================================================================================" );
                System.out.println("old points "+bestPoints);
            }

                if(BoardPanel.calculatePoints(BoardPanel.tablica)>bestPoints){
                   bestPoints=BoardPanel.calculatePoints(BoardPanel.tablica);
                   bestXpos=currentCol;
                   bestRotation=currentRotation;


                }
            if(testMode == true)System.out.println("new points "+bestPoints);
            }
        }
        currentCol=bestXpos;
        currentRotation=bestRotation;
 if(testMode == true)       System.out.println("finished, best x="+ bestXpos + "best rota=" + bestRotation);


}

////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Attempts to set the rotation of the current piece to newRotation.
     * @param newRotation The rotation of the new peice.
     */
    private void rotatePiece(int newRotation) {
		/*
		 * Sometimes pieces will need to be moved when rotated to avoid clipping
		 * out of the board (the I piece is a good example of this). Here we store
		 * a temporary row and column in case we need to move the tile as well.
		 */
        int newColumn = currentCol;
        int newRow = currentRow;

		/*
		 * Get the insets for each of the sides. These are used to determine how
		 * many empty rows or columns there are on a given side.
		 */
        int left = currentType.getLeftInset(newRotation);
        int right = currentType.getRightInset(newRotation);
        int top = currentType.getTopInset(newRotation);
        int bottom = currentType.getBottomInset(newRotation);

		/*
		 * If the current piece is too far to the left or right, move the piece away from the edges
		 * so that the piece doesn't clip out of the map and automatically become invalid.
		 */
        if(currentCol < -left) {
            newColumn -= currentCol - left;
        } else if(currentCol + currentType.getDimension() - right >= BoardPanel.COL_COUNT) {
            newColumn -= (currentCol + currentType.getDimension() - right) - BoardPanel.COL_COUNT + 1;
        }

		/*
		 * If the current piece is too far to the top or bottom, move the piece away from the edges
		 * so that the piece doesn't clip out of the map and automatically become invalid.
		 */
        if(currentRow < -top) {
            newRow -= currentRow - top;
        } else if(currentRow + currentType.getDimension() - bottom >= BoardPanel.ROW_COUNT) {
            newRow -= (currentRow + currentType.getDimension() - bottom) - BoardPanel.ROW_COUNT + 1;
        }

		/*
		 * Check to see if the new position is acceptable. If it is, update the rotation and
		 * position of the piece.
		 */
        if(board.isValidAndEmpty(currentType, newColumn, newRow, newRotation)) {
            currentRotation = newRotation;
            currentRow = newRow;
            currentCol = newColumn;
        }
    }

    /**
     * Checks to see whether or not the game is paused.
     * @return Whether or not the game is paused.
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Checks to see whether or not the game is over.
     * @return Whether or not the game is over.
     */
    public boolean isGameOver() {
        return isGameOver;
    }

    /**
     * Checks to see whether or not we're on a new game.
     * @return Whether or not this is a new game.
     */
    public boolean isNewGame() {
        return isNewGame;
    }

    /**
     * Gets the current score.
     * @return The score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets the current type of piece we're using.
     * @return The piece type.
     */
    public TileType getPieceType() {
        return currentType;
    }

    /**
     * Gets the next type of piece we're using.
     * @return The next piece.
     */
    public TileType getNextPieceType() {
        return nextType;
    }

    /**
     * Gets the column of the current piece.
     * @return The column.
     */
    public int getPieceCol() {
        return currentCol;
    }

    /**
     * Gets the row of the current piece.
     * @return The row.
     */
    public int getPieceRow() {
        return currentRow;
    }

    /**
     * Gets the rotation of the current piece.
     * @return The rotation.
     */
    public int getPieceRotation() {
        return currentRotation;
    }

    /**
     * Entry-point of the game. Responsible for creating and starting a new
     * game instance.
     * @param args Unused.
     */
    public static void main(String[] args)  throws Exception {
        Tetris tetris = new Tetris();
        tetris.startGame();

    }
}