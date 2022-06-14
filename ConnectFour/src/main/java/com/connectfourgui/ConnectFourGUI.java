package com.connectfourgui;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * Main Game class that contains the logic
 *
 * @author telemachus
 */
public class ConnectFourGUI extends GameApplication {
    /**
     * Used to denote the pieces in the terminal board.
     */
    protected static final int PLAYER = 1, AI = 2, EMPTY = 0;
    /**
     * Controls the size of the tiles when drawing the board
     */
    protected static final int TILE_SIZE = 125;
    /**
     * Controls the speed of the ball when keyboard keys are used
     */
    protected static final int SPEED = 5;
    /**
     * The number of rows and columns in the game board
     */
    protected static final int MAX_ROWS = 6, MAX_COLS = 7;
    /**
     * Controls the width of the window
     */
    protected static final int MAX_WIDTH = MAX_COLS * TILE_SIZE;
    /**
     * Controls the height of the window
     */
    protected static final int MAX_HEIGHT = (MAX_ROWS + 1) * TILE_SIZE;
    /**
     * Offset where it is three
     */
    protected static final int OFFSET = 3;
    /**
     * Keeps the count of the nodes expanded in the minimax algorithm
     */
    protected static long nodesExpanded;
    protected static long totalNodesExpanded = 0;
    protected static double totalTime = 0;
    protected static int totalTurns = 0;
    /**
     * keeps track of the col where the piece will be dropped
     */
    protected int col;
    /**
     * row keeps track of the row where the piece will be dropped
     */
    protected int row;
    /**
     * playerScore Keeps track of the player score
     */
    protected static int playerScore = 0;
    /**
     * aiScore keeps track of the AI score
     */
    protected static int aiScore = 0;
    /**
     * Cell length that is going to be used when the board is going to be divided into cells
     */
    protected static final int CELL_LENGTH = 4;
    /**
     * Initiates the  board
     */
    protected int[][] terminalBoard = createBoard();
    /**
     * used to connect the main menu if alphaBeta is true then the minimax algorithm will implement with turning; else without pruning
     */
    public static boolean alphaBeta = false;
    /**
     * Search tree
     */
    public static TreeNode boardTree;
    protected static int depth = 6;
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Creates the initial board with the specified dimensions
     *
     * @return A 2d array matrix
     */
    public static int[][] createBoard() {
        return new int[MAX_ROWS][MAX_COLS];
    }

    /**
     * prints the board.
     *
     * @param board The board that will be printed
     */
    public static void printBoard(int[][] board) {
        for (int i = MAX_ROWS - 1; i > -1; i--) {
            for (int j = 0; j < MAX_COLS; j++) {
                System.out.printf("%d\t", board[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Drops a piece into a specified place
     *
     * @param board the board in which the piece will be dropped
     * @param piece the piece that will be dropped
     * @param row   Board row
     * @param col   Board column
     */
    //------------------------------------------------------------------------------------------------------------------
    public static void dropPiece(int[][] board, int row, int col, int piece) {
        board[row][col] = piece;
    }

    /**
     * Gets the rows that don't have a piece so that it can be dropped into that row.
     *
     * @param board The board that will be checked
     * @param col   column of the board.
     * @return a row that doesn't have a piece in it.
     */
    public static int getOpenRow(int[][] board, int col) {
        int row;
        for (row = 0; row < MAX_ROWS; row++) {
            if (board[row][col] == 0) return row;
        }
        return row;
    }

    /**
     * Checks if the column is full.
     *
     * @param board the board that will be checked
     * @param col   the board column
     */
    public static boolean isValidLocation(int[][] board, int col) {
        return board[MAX_ROWS - 1][col] == 0;
    }

    /**
     * Gets the valid columns that a piece can be dropped into
     *
     * @param board the board to be checked
     * @return a list that contains the valid columns
     */
    public static ArrayList<Integer> getValidLocations(int[][] board) {
        ArrayList<Integer> validLocations = new ArrayList<>();
        for (int c = 0; c < MAX_COLS; c++) {
            if (isValidLocation(board, c)) validLocations.add(c);
        }
        return validLocations;
    }
    //------------------------------------------------------------------------------------------------------------------


    /**
     * Checks for the classical end game where the game ends if either the enemy or the player has 4 pieces in a row.
     *
     * @param board The board that will be checked.
     * @param piece the piece that the board will be checked for.
     * @return true if there are 4 pieces in a row either diagonally, or horizontally or vertically;else false.
     */
    public static boolean GameOver(int[][] board, int piece) {
        // Horizontal
        for (int c = 0; c < MAX_COLS - OFFSET; c++) {
            for (int r = 0; r < MAX_ROWS; r++) {
                if (board[r][c] == piece && board[r][c + 1] == piece && board[r][c + 2] == piece && board[r][c + 3] == piece) {
                    return true;
                }
            }
        }
        // vertical check
        for (int c = 0; c < MAX_COLS; c++) {
            for (int r = 0; r < MAX_ROWS - OFFSET; r++) {
                if (board[r][c] == piece && board[r + 1][c] == piece && board[r + 2][c] == piece && board[r + 3][c] == piece) {
                    return true;
                }
            }
        }
        // Positive Diagonal check
        for (int c = 0; c < MAX_COLS - OFFSET; c++) {
            for (int r = 0; r < MAX_ROWS - OFFSET; r++) {
                if (board[r][c] == piece && board[r + 1][c + 1] == piece && board[r + 2][c + 2] == piece && board[r + 3][c + 3] == piece) {
                    return true;
                }
            }
        }
        // negative Diagonal check
        for (int c = 0; c < MAX_COLS - OFFSET; c++) {
            for (int r = 3; r < MAX_ROWS; r++) {
                if (board[r][c] == piece && board[r - 1][c + 1] == piece && board[r - 2][c + 2] == piece && board[r - 3][c + 3] == piece) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param board The board that will be checked
     * @return true if there are no valid locations for the pieces;else false
     */
    public static boolean GameOver(int[][] board) {
        return getValidLocations(board).isEmpty();
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * counts the specified piece in the array or the cell
     *
     * @param arr   The array or the cell that contains the pieces
     * @param piece the piece to count
     * @return the number of the specified pieces in the cell
     */
    public static int countPieces(int[] arr, int piece) {
        int cnt = 0;
        for (int j : arr) {
            if ((j == piece)) {
                ++cnt;
            }
        }
        return cnt;
    }

    /**
     * Evaluates the cell with the specified piece <br>
     * where if the cell has 4 pieces of the same specified kind then add 100000 to the score <br>
     * if the cell has 3 pieces of the specified kind and 1 empty cell then add 100 <br>
     * if the cell has 2 pieces of the specified kind and 2 empty cells then add 1 <br>
     * if the cell has 3 pieces of the enemy piece and 1 empty cell then add -80 <br>
     * if the cell has 4 pieces of the enemy piece then add -100000
     *
     * @param cell  the cell that will be evaluated
     * @param piece the main piece in the cell
     * @return score of the current cell.
     */
    private static int evaluateCell(int[] cell, int piece) {
        int cellScore = 0;
        int enemyPiece = PLAYER;
        if (piece == PLAYER) enemyPiece = AI;
        if (countPieces(cell, piece) == 4) cellScore += 100000;
        else if (countPieces(cell, piece) == 3 && countPieces(cell, EMPTY) == 1) cellScore += 100;
        else if (countPieces(cell, piece) == 2 && countPieces(cell, EMPTY) == 2) cellScore += 1;
        if (countPieces(cell, enemyPiece) == 3 && countPieces(cell, EMPTY) == 1) cellScore -= 80;
        else if (countPieces(cell, enemyPiece) == 4) cellScore -= 100000;
        return cellScore;
    }

    /**
     * checks if there are 4 pieces of the same type in a row otherwise return 0
     *
     * @param cell  the cell that is going to evaluate
     * @param piece that the cell will be evaluated for
     * @return 1 if there are 4 pieces of; else 0
     */
    private static int connectFour(int[] cell, int piece) {
        int cellScore = 0;
        if (countPieces(cell, piece) == 4) cellScore += 1;
        return cellScore;
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Makes the column into an array
     *
     * @param c     the column that will be made into an Array
     * @param board The board that contains the column
     * @return a column that it's made into an Array
     */
    private static int[] makeColArr(int[][] board, int c) {
        int[] colArr = new int[MAX_ROWS];
        for (int row = 0; row < MAX_ROWS; row++) {
            colArr[row] = board[row][c];
        }
        return colArr;
    }

    /**
     * Makes the row into an array
     *
     * @param board the board that contains the row
     * @param r     the row that will be made into an array
     * @return a row that's made into an array
     */
    private static int[] makeRowArr(int[][] board, int r) {
        int[] rowArr = new int[MAX_COLS];
        System.arraycopy(board[r], 0, rowArr, 0, MAX_COLS);
        return rowArr;
    }

    /**
     * Scores the current board where the current board is dived into cells that can be evaluated individually <br>
     * the cell are horizontal,vertical, and diagonal (+ve direction and -ve direction)
     *
     * @param board the board that will be scored
     * @param piece the piece that will the board will be evaluated for.
     * @return the score for the specified piece of the current board.
     */
    private static int scorePosition(int[][] board, int piece) {
        int boardScore = 0;
        // Score center column and favour it.
//        int[] centerArr = makeColArr(board, MAX_COLS / 2);
//        int centerCount = countPieces(centerArr, piece);
//        boardScore += centerCount * 3;
        // Score Horizontal
        for (int row = 0; row < MAX_ROWS; row++) {
            // make the row into an array
            int[] rowArr = makeRowArr(board, row);
            // takes cell units where each cell is 4 units
            for (int col = 0; col < MAX_COLS - OFFSET; col++) {
                int[] cell = new int[CELL_LENGTH];
                System.arraycopy(rowArr, col, cell, 0, CELL_LENGTH);
                boardScore += evaluateCell(cell, piece);
            }
        }
        // Score Vertical
        for (int col = 0; col < MAX_COLS; col++) {
            // makes the column into an array
            int[] colArr = makeColArr(board, col);
            for (int row = 0; row < MAX_ROWS - OFFSET; row++) {
                int[] cell = new int[CELL_LENGTH];
                System.arraycopy(colArr, row, cell, 0, CELL_LENGTH);
                boardScore += evaluateCell(cell, piece);
            }
        }
        // score Positive Diagonally
        for (int row = 0; row < MAX_ROWS - OFFSET; row++) {
            for (int col = 0; col < MAX_COLS - OFFSET; col++) {
                int[] cell = new int[CELL_LENGTH];
                for (int i = 0; i < CELL_LENGTH; i++) {
                    cell[i] = board[row + i][col + i];
                }
                boardScore += evaluateCell(cell, piece);
            }
        }
        // score negative Diagonally
        for (int row = 0; row < MAX_ROWS - OFFSET; row++) {
            for (int col = 0; col < MAX_COLS - OFFSET; col++) {
                int[] cell = new int[CELL_LENGTH];
                for (int i = 0; i < CELL_LENGTH; i++) {
                    cell[i] = board[row + OFFSET - i][col + i];
                }
                boardScore += evaluateCell(cell, piece);
            }
        }
        return boardScore;
    }

    /**
     * returns the number of connect fours in the board of a certain piece
     *
     * @param board the board that is going to score
     * @param piece the piece that the board is going be scored for.
     * @return the number of connect fours of a certain piece
     */

    private static int numberFours(int[][] board, int piece) {
        int boardScore = 0;
        // Score Horizontal
        for (int row = 0; row < MAX_ROWS; row++) {
            // make the row into an array
            int[] rowArr = makeRowArr(board, row);
            // takes cell units where each cell is 4 units
            for (int col = 0; col < MAX_COLS - OFFSET; col++) {
                int[] cell = new int[CELL_LENGTH];
                System.arraycopy(rowArr, col, cell, 0, CELL_LENGTH);
                boardScore += connectFour(cell, piece);
            }
        }
        // Score Vertical
        for (int col = 0; col < MAX_COLS; col++) {
            // makes the column into an array
            int[] colArr = makeColArr(board, col);
            for (int row = 0; row < MAX_ROWS - OFFSET; row++) {
                int[] cell = new int[CELL_LENGTH];
                System.arraycopy(colArr, row, cell, 0, CELL_LENGTH);
                boardScore += connectFour(cell, piece);
            }
        }
        // score Positive Diagonally
        for (int row = 0; row < MAX_ROWS - OFFSET; row++) {
            for (int col = 0; col < MAX_COLS - OFFSET; col++) {
                int[] cell = new int[CELL_LENGTH]; // creates a cell of length 4
                for (int i = 0; i < CELL_LENGTH; i++) { // Gets the elements of the +ve diagonal
                    cell[i] = board[row + i][col + i];
                }
                boardScore += connectFour(cell, piece);
            }
        }
        // score negative Diagonally
        for (int row = 0; row < MAX_ROWS - OFFSET; row++) {
            for (int col = 0; col < MAX_COLS - OFFSET; col++) {
                int[] cell = new int[CELL_LENGTH]; // creates a cell of length 4
                for (int i = 0; i < CELL_LENGTH; i++) { // gets the elements of the -ve diagonal
                    cell[i] = board[row + OFFSET - i][col + i];
                }
                boardScore += connectFour(cell, piece);
            }
        }
        return boardScore;
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Classical check of the game where the terminal board is board that has 4 pieces in a row of either pieces or is completely full
     *
     * @param terminalBoard The board that will check
     * @return true if the board is terminal i.e. it contains 4 pieces in a row of either type or is completely full; else false
     */
    // isTerminalNode is redundant if the game is not classical
    private static boolean isTerminalNode(int[][] terminalBoard) {
        return GameOver(terminalBoard, PLAYER) || GameOver(terminalBoard, AI) || getValidLocations(terminalBoard).isEmpty();
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * minimax algorithm without pruning <br>
     * The terminal node is a board that is completely full <br>
     * if the game is classical then the terminal node is the node that has a connect-four of either piece.
     *
     * @param board     The board that the minimax algorithm will be implemented on
     * @param depth     keeps track of the depth
     * @param maxPlayer maxPlayer:true or minPlayer:false
     * @param node      used to keep track of the tree i.e. for printing the tree in the terminal but degrades the performance
     * @return an array where the first index(0) is the column and index(1) is the value of the algorithm (i.e. the score)
     */
    private static int[] minimax(int[][] board, int depth, boolean maxPlayer, TreeNode node) {
        // Gets a list of valid locations (i.e. children of the current position)
        ArrayList<Integer> validLocations = getValidLocations(board);
//        boolean isTerminalNode = isTerminalNode(board);
        // Terminal node where it is the full board
        boolean isTerminalNode = GameOver(board);
        int connectFoursPlayer = numberFours(board, PLAYER), connectFoursAI = numberFours(board, AI);
        TreeNode newTreeNode;
        // Base case if the game is classical
        // GameOver(board,piece)
//        if (depth == 0 || isTerminalNode) {
//            if (isTerminalNode) {
//                if (GameOver(board, AI)) {
//                    return new int[]{-1, Integer.MAX_VALUE};
//                } else if (GameOver(board, PLAYER)) {
//                    return new int[]{-1, Integer.MIN_VALUE};
//                } else // game is over, and there is no more valid moves
//                    return new int[]{-1, 0};
//            } else {
//                return new int[]{-1, scorePosition(board, AI)};
//            }
//        }
        // GameOver(board)
        // Custom Base case
        if (depth == 0 || isTerminalNode) {
            if (isTerminalNode) {
                if (connectFoursAI > connectFoursPlayer) {
                    return new int[]{-1, Integer.MAX_VALUE};
                } else if (connectFoursPlayer > connectFoursAI) {
                    return new int[]{-1, Integer.MIN_VALUE};
                } else {
                    return new int[]{-1, 0};
                }
            } else
                return new int[]{-1, scorePosition(board, AI)}; // Game is not over yet and the board has a definite score
        }
        // value or the score of the minimax
        int value;
        // initialize the column to random column in the valid locations
        int column = validLocations.get(FXGLMath.random(0, validLocations.size() - 1));
        if (maxPlayer) {
            // initialize the value or score of the algorithm to -ve infinity where the -ve infinity is going to be represented by the minimum Integer value.
            value = Integer.MIN_VALUE;
            for (int col : validLocations) {
                // Gets the open row
                int row = getOpenRow(board, col);
                int[][] boardCopy = createBoard();
                // makes a copy of the board.
                for (int r = 0; r < MAX_ROWS; r++) {
                    System.arraycopy(board[r], 0, boardCopy[r], 0, MAX_COLS);
                }
                // then drops maxPlayer piece in to create the child node.
                dropPiece(boardCopy, row, col, AI);
                // Create a new node
                newTreeNode = new TreeNode(Integer.toString(scorePosition(boardCopy, AI)));

                int newScore = minimax(boardCopy, depth - 1, false, newTreeNode)[1];
                // change the value of the node to the new Score from the minimax
                newTreeNode.name = Integer.toString(newScore);
                if (newScore > value) {
                    value = newScore;
                    column = col;
                }
                // add The subtree to the main tree
                node.addChild(newTreeNode);
                node.name = Integer.toString(value);
                nodesExpanded++;
            }
        } else {
            // initialize the value or score of the algorithm to +ve infinity where the +ve infinity is going to be represented by the Maximum Integer value.
            value = Integer.MAX_VALUE;
            for (int col : validLocations) {
                // gets the new open row that the piece can be dropped into
                int row = getOpenRow(board, col);
                // initializes  a new game board.
                int[][] boardCopy = new int[MAX_ROWS][MAX_COLS];
                // copies the parent node.
                for (int r = 0; r < MAX_ROWS; r++) {
                    System.arraycopy(board[r], 0, boardCopy[r], 0, MAX_COLS);
                }
                // drops the piece into the new board (i.e. the child node)
                dropPiece(boardCopy, row, col, PLAYER);
                // create a new child node
                newTreeNode = new TreeNode(Integer.toString(scorePosition(boardCopy, PLAYER)));

                // recursive call
                int newScore = minimax(boardCopy, depth - 1, true, newTreeNode)[1];
                // update the name of the node with the new score.
                newTreeNode.name = Integer.toString(newScore);
                if (newScore < value) {
                    value = newScore;
                    column = col;
                }
                // add subtree with the main tree.
                node.addChild(newTreeNode);
                node.name = Integer.toString(value);
                nodesExpanded++;
            }
        }
        return new int[]{column, value};
    }

    /**
     * minimax algorithm without pruning <br>
     * The terminal node is a board that is completely full <br>
     * if the game is classical then the terminal node is the node that has a connect-four of either piece
     *
     * @param board     The board that the minimax algorithm will be implemented on
     * @param depth     keeps track of the depth
     * @param alpha     the value that is going to be passed if the player is maximizing
     * @param beta      the value that is going to be passed if the player is minimizing
     * @param maxPlayer maxPlayer:true or minPlayer:false
     * @param node      used to keep track of the tree i.e. for printing the tree in the terminal but degrades the performance
     * @return an array where the first index(0) is the column and index(1) is the value of the algorithm (i.e. the score)
     */
    private static int[] minimax(int[][] board, int depth, int alpha, int beta, boolean maxPlayer, TreeNode node) {
        // Gets a list of valid locations (i.e. children of the current position)
        ArrayList<Integer> validLocations = getValidLocations(board);
//        boolean isTerminalNode = isTerminalNode(board);
        boolean isTerminalNode = GameOver(board);
        int connectFoursPlayer = numberFours(board, PLAYER), connectFoursAI = numberFours(board, AI);
        TreeNode newTreeNode;
        // Base case if the game is classical
//        if (depth == 0 || isTerminalNode) {
//            if (isTerminalNode) {
//                if (GameOver(board, AI)) return new int[]{-1, Integer.MAX_VALUE};
//                else if (GameOver(board, PLAYER)) {
//                    return new int[]{-1, Integer.MIN_VALUE};
//                } else // game is over, and there is no more valid moves
//                    return new int[]{-1, 0};
//            } else {
//                return new int[]{-1, scorePosition(board, AI)};
//            }
//        }
        // custom base case
        if (depth == 0 || isTerminalNode) {
            if (isTerminalNode) {
                if (connectFoursAI > connectFoursPlayer) {
                    return new int[]{-1, Integer.MAX_VALUE};
                } else if (connectFoursPlayer > connectFoursAI) {
                    return new int[]{-1, Integer.MIN_VALUE};
                } else {
                    return new int[]{-1, 0};
                }
            } else
                return new int[]{-1, scorePosition(board, AI)}; // Game is not over yet and the board has a definite score
        }
        // value or the score of the minimax
        int value;
        // initialize the column to random column in the valid locations
        int column = validLocations.get(FXGLMath.random(0, validLocations.size() - 1));
        if (maxPlayer) {
            // initialize the value or score of the algorithm to -ve infinity where the -ve infinity is going to be represented by the minimum Integer value.
            value = Integer.MIN_VALUE;
            for (int col : validLocations) {
                // gets the next open that the piece can piece dropped into
                int row = getOpenRow(board, col);
                // initializes a new board
                int[][] boardCopy = new int[MAX_ROWS][MAX_COLS];
                // copies the parent board
                for (int r = 0; r < MAX_ROWS; r++) {
                    System.arraycopy(board[r], 0, boardCopy[r], 0, MAX_COLS);
                }
                // drops the piece into the copied board (i.e. the child node)
                dropPiece(boardCopy, row, col, AI);
                // creates a new node with the copied board.
                newTreeNode = new TreeNode(Integer.toString(scorePosition(boardCopy, AI)));
                // recursive call
                int newScore = minimax(boardCopy, depth - 1, alpha, beta, false, newTreeNode)[1];
                // updates the score
                if (newScore > value) {
                    value = newScore;
                    column = col;
                }
                // updates the alpha
                alpha = Integer.max(alpha, value);
                // updates the value of the node
                newTreeNode.name = value + ":" + alpha + ":" + beta;
                // prune if the alpha is greater or equal to beta
                if (alpha >= beta) break;
                // else add the subtree to the main tree
                node.addChild(newTreeNode);
                node.name = Integer.toString(value);
                nodesExpanded++;
            }
        } else {
            value = Integer.MAX_VALUE;
            for (int col : validLocations) {
                // gets the next open that the piece can piece dropped into
                int row = getOpenRow(board, col);
                // initializes a new board
                int[][] boardCopy = new int[MAX_ROWS][MAX_COLS];
                // copies the parent board
                for (int r = 0; r < MAX_ROWS; r++) {
                    System.arraycopy(board[r], 0, boardCopy[r], 0, MAX_COLS);
                }
                // drops the piece into the copied board (i.e. the child node)
                dropPiece(boardCopy, row, col, PLAYER);
                // creates a new node with the copied board
                newTreeNode = new TreeNode(Integer.toString(scorePosition(boardCopy, PLAYER)));
                // recursive call
                int newScore = minimax(boardCopy, depth - 1, alpha, beta, true, newTreeNode)[1];
                // update the score
                if (newScore < value) {
                    value = newScore;
                    column = col;
                }
                // update the beta score
                beta = Integer.min(beta, value);
                // update the name of the node
                newTreeNode.name = newScore + ":" + alpha + ":" + beta;
                // prune if the alpha is greater or equal to beta
                if (alpha >= beta) break;
                node.addChild(newTreeNode);
                node.name = Integer.toString(value);
                nodesExpanded++;
            }
        }
        return new int[]{column, value};
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Creates the GUI board
     */
    private void createGUIBoard() {
        int x, y;
        Color ballColor = Color.BLACK;
        for (int col = 0; col < MAX_COLS; col++, ballColor = Color.BLACK) {
            for (int row = MAX_ROWS - 1; row > -1; row--) {
                x = (col) * TILE_SIZE;
                y = (MAX_ROWS - row) * TILE_SIZE;
                if (terminalBoard[row][col] == 1) {
                    ballColor = Color.RED;
                } else if (terminalBoard[row][col] == 2) {
                    ballColor = Color.YELLOW;
                }
                // spawns the tile
                FXGL.spawn("Tile", new SpawnData(x, y).put("Tile Size", TILE_SIZE).put("color", ballColor));
            }
        }
    }

    /**
     * Changes the color of the ball <br>
     * Useful if the game is two player
     *
     * @param color The color of the new ball
     */
    protected void changeBallColor(Color color) {
        double horizontalPosition = getGameWorld().getSingleton(EntityType.PLAYER).getX();
        getGameWorld().getSingleton(EntityType.PLAYER).removeFromWorld();
        spawn("Player", new SpawnData(horizontalPosition, 0).put("Tile Size", TILE_SIZE).put("color", color));
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Takes care of the user turn to reduce redundancy
     *
     * @param x the position (x-axis) where the ball is going to be dropped into.
     * @return true if the player played; else false
     */
    protected boolean userTurn(int x) {
        col = FXGLMath.floor((float) x / TILE_SIZE);
        if (isValidLocation(terminalBoard, col)) {
            row = getOpenRow(terminalBoard, col);
            dropPiece(terminalBoard, row, col, PLAYER);
            System.out.println("The new Board after the player played : ");
            printBoard(terminalBoard);
            System.out.println("--------------------");
            createGUIBoard();
            changeBallColor(Color.YELLOW);
            totalTurns++;
            return true;
        }
        return false;
    }

    /**
     * Takes care of the AI turn to reduce redundancy
     */
    protected void AITurn() {
        boardTree = new TreeNode();
        double startTime, endTime;
        nodesExpanded = 0;
        if (alphaBeta) {
            startTime = System.currentTimeMillis();
            col = minimax(terminalBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true, boardTree)[0];
            endTime = System.currentTimeMillis();
            System.out.println(boardTree);
            System.out.println("Time Taken : " + (endTime - startTime) + " Millie Seconds");
            System.out.printf("Nodes Expanded : %d\n", nodesExpanded);

        } else {
            startTime = System.currentTimeMillis();
            col = minimax(terminalBoard, depth, true, boardTree)[0];
            endTime = System.currentTimeMillis();
            System.out.println(boardTree);
            System.out.println("Time Taken : " + (endTime - startTime) + " Millie Seconds");
            System.out.printf("Nodes Expanded : %d\n", nodesExpanded);
        }
        totalNodesExpanded += nodesExpanded;
        totalTime += endTime - startTime;
        totalTurns++;
        row = getOpenRow(terminalBoard, col);
        dropPiece(terminalBoard, row, col, AI);
        System.out.printf("Depth : %d\n", depth);
        System.out.println("alpha beta : " + alphaBeta);
        System.out.println("The new Board after AI Played : ");
        printBoard(terminalBoard);
        System.out.println("--------------------");
        createGUIBoard();
        changeBallColor(Color.RED);
    }

    /**
     * Updates the Score of both the AI and the player
     */
    protected void updateScore() {
        aiScore = numberFours(terminalBoard, AI);
        getWorldProperties().setValue("AI Score", aiScore);

        playerScore = numberFours(terminalBoard, PLAYER);
        getWorldProperties().setValue("Player Score", playerScore);
    }

    //------------------------------------------------------------------------------------------------------------------
    @Override
    protected void initSettings(GameSettings settings) {
        // Sets window width and height
        settings.setWidth(MAX_WIDTH);
        settings.setHeight(MAX_HEIGHT);
        // Sets the title of the window and version
        settings.setTitle("Connect 4");
        settings.setVersion("0.0.1");
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
        // Enables the main menu
        settings.setMainMenuEnabled(true);
        // Creates a new Scene factory and gets the custom main menu.
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                return new ConnectFourMainMenu.MyMainMenu();
            }
        });
    }


    // initiates the input to be handled
    @Override
    protected void initInput() {
        Input input = getInput();
        // Mouse tracking
        EventHandler<MouseEvent> handler = event -> getGameWorld().getSingleton(EntityType.PLAYER).setX(input.getMouseXUI() - (TILE_SIZE / 2.0));
        input.addEventHandler(MouseEvent.MOUSE_MOVED, handler);
        // Move Right function using the keyboard key D
        UserAction moveRight = new UserAction("Move Right") {
            @Override
            protected void onAction() {
                int x = (int) getGameWorld().getSingleton(EntityType.PLAYER).getX();
                if (x < getGameScene().getAppWidth() - TILE_SIZE) {
                    getGameWorld().getSingleton(EntityType.PLAYER).translateX(SPEED);
                }
            }
        };
        // Move Left function using Keyboard Key A
        UserAction moveLeft = new UserAction("Move Left") {
            @Override
            protected void onAction() {
                int x = (int) getGameWorld().getSingleton(EntityType.PLAYER).getX();
                if (x > 0) {
                    getGameWorld().getSingleton(EntityType.PLAYER).translateX(-SPEED);
                }
            }
        };
        // Placing piece using Keyboard Key S
        UserAction placePiece = new UserAction("Place Piece") {
            @Override
            protected void onActionBegin() {
                int posX = (int) getGameWorld().getSingleton(EntityType.PLAYER).getX();
                boolean userPlayed = userTurn(posX);
                if (userPlayed) {
                    if (!GameOver(terminalBoard))
                        AITurn();
                } else {
                    showMessage("Please Choose a correct position", () -> getGameController().resumeEngine());
                }
                updateScore();
            }
        };
        // Placing piece function using Mouse.
        UserAction placePieceMouse = new UserAction("Place Piece using Mouse") {
            @Override
            protected void onActionBegin() {
                System.out.println(input.getMousePositionUI());
                int posX = (int) input.getMouseXWorld();
                boolean userPlayed = userTurn(posX);
                if (userPlayed) {
                    if (!GameOver(terminalBoard))
                        AITurn();
                } else {
                    showMessage("Please Choose a correct position", () -> getGameController().resumeEngine());
                }
                updateScore();
            }
        };
        // Adding the actions the input to be handled
        input.addAction(moveLeft, KeyCode.A);
        input.addAction(moveRight, KeyCode.D);
        input.addAction(placePiece, KeyCode.S);
        input.addAction(placePieceMouse, MouseButton.PRIMARY);
    }

    @Override
    protected void onUpdate(double tpf) {
        // Classical check of the game
//        if (GameOver(terminalBoard, PLAYER) && playerScore > aiScore) {
//            System.out.println("Player 1 Wins");
//            showMessage("Player 1 Wins\n" + "AI Score : " + aiScore + "\nPlayer Score : " + playerScore, () -> getGameController().exit());
//        }
//        if (GameOver(terminalBoard, AI) && aiScore > playerScore) {
//            showMessage("Player 2 Wins\n" + "AI Score : " + aiScore + "\nPlayer Score : " + playerScore, () -> getGameController().exit());
//        }
        // Custom check of the game
        if (GameOver(terminalBoard) && playerScore > aiScore) {
            System.out.println("Player 1 Wins");
            System.out.println("Average time : " + (totalTime / totalTurns));
            System.out.println("Average nodes expanded : " + (totalNodesExpanded / totalTurns));
            showMessage("Player 1 Wins\n" + "AI Score : " + aiScore + "\nPlayer Score : " + playerScore, () -> getGameController().exit());
        }
        if (GameOver(terminalBoard) && aiScore > playerScore) {
            System.out.println("Player 2 Wins");
            System.out.println("Average time : " + (totalTime / totalTurns));
            System.out.println("Average nodes expanded : " + (totalNodesExpanded / totalTurns));
            showMessage("Player 2 Wins\n" + "AI Score : " + aiScore + "\nPlayer Score : " + playerScore, () -> getGameController().exit());
        }
        if (GameOver(terminalBoard) && aiScore == playerScore) {
            System.out.println("A Tie!!");
            System.out.println("Average time : " + (totalTime / totalTurns));
            System.out.println("Average nodes expanded : " + (totalNodesExpanded / totalTurns));
            showMessage("A Tie!!\n" + "AI Score : " + aiScore + "\nPlayer Score : " + playerScore, () -> getGameController().exit());
        }
    }


    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("AI Score", 0);
        vars.put("Player Score", 0);
    }

    @Override
    protected void initUI() {
        Text textScore1 = getUIFactoryService().newText("", Color.YELLOW, 22);
        Text textScore2 = getUIFactoryService().newText("", Color.RED, 22);

        textScore1.setTranslateX(10);
        textScore1.setTranslateY(50);

        textScore2.setTranslateX(getAppWidth() - 30);
        textScore2.setTranslateY(50);

        textScore1.textProperty().bind(getWorldProperties().intProperty("AI Score").asString());
        textScore2.textProperty().bind(getWorldProperties().intProperty("Player Score").asString());

        getGameScene().addUINodes(textScore1, textScore2);
    }

    // Initiates the main game loop
    @Override
    protected void initGame() {
        FXGL.getGameScene().setBackgroundColor(Color.BLACK);
        FXGL.getGameWorld().addEntityFactory(new ConnectFourFactory());
        createGUIBoard();
        spawn("Player", new SpawnData((MAX_WIDTH / 2.0) - TILE_SIZE / 2.0, 0).put("Tile Size", TILE_SIZE).put("color", Color.RED));
    }

    public static void main(String[] args) {
        launch(args);
    }
}