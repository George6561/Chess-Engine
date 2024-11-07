package com.george.main;

import com.george.board.ChessBoard;
import com.george.stockfish.StockfishConnector;
import com.george.window.ChessWindow;
import javafx.application.Platform;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class ChessGame {

    private ChessWindow chessWindow;
    private StockfishConnector stockfish;
    private StringBuilder moveHistory;
    private Random random;

    public ChessGame(ChessWindow chessWindow) {
        this.chessWindow = chessWindow;
        this.stockfish = new StockfishConnector();
        this.moveHistory = new StringBuilder();
        this.random = new Random();
    }

    public void startGame() throws IOException, InterruptedException {
        if (stockfish.startEngine()) {
            try {
                initializeStockfish();
                displayInitialBoard();
                playGameLoop();
            } finally {
                stockfish.stopEngine();
            }
        } else {
            System.out.println("Failed to start Stockfish engine.");
        }
    }

    private void initializeStockfish() throws IOException {
        stockfish.sendCommand("uci");
        stockfish.getResponse();
        stockfish.sendCommand("isready");
        stockfish.getResponse();
        stockfish.sendCommand("position startpos");
    }

    private void displayInitialBoard() {
        Platform.runLater(() -> {
            try {
                chessWindow.displayChessPieces(-1, -1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private boolean isWhiteToMove = true;

    private void playGameLoop() throws IOException, InterruptedException {
        System.out.println("Starting game loop...");

        while (true) {
            if (isWhiteToMove) {
                System.out.println("STOCKFISH MOVE NOW (WHITE)");
                makeStockfishMove();
            } else {
                System.out.println("RANDOM MOVE NOW (BLACK)");
                makeRandomMove();
            }

            // Check if the game has ended
            if (isGameOver()) {
                System.out.println("Game over.");
                break;
            }

            // Switch turns after each move
            isWhiteToMove = !isWhiteToMove;

            // Debug print to verify turn change
            System.out.println("Is it White's turn now? " + isWhiteToMove);

            // Print current move history for debugging
            System.out.println("Current move history: " + moveHistory.toString());

            // Small delay for smooth UI updates
            Thread.sleep(500);
        }
        System.out.println("Exiting game loop.");
    }

    private void makeStockfishMove() throws IOException, InterruptedException {
        try {
            stockfish.sendCommand("go movetime 1000");
            String bestMove = stockfish.getBestMove();

            if (bestMove == null || bestMove.isEmpty()) {
                System.out.println("Stockfish could not find a move. Game over.");
                return;
            }

            System.out.println("Stockfish's Move (White): " + bestMove);
            updateMoveHistory(bestMove);

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    System.out.println("Applying Stockfish move to the board: " + bestMove);
                    chessWindow.movePiece(bestMove);
                    chessWindow.displayChessPieces(-1, -1);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
            latch.await();

            System.out.println("Board state after Stockfish's move:");
            chessWindow.getBoard().printBoardWithIndices();
        } catch (Exception e) {
            System.out.println("Exception in makeStockfishMove: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void makeRandomMove() throws IOException, InterruptedException {
        try {
            List<int[]> legalMoves = chessWindow.getBoard().getAllLegalMoves(ChessBoard.Player.BLACK);

            if (legalMoves.isEmpty()) {
                System.out.println("Black has no legal moves. Game over.");
                return;
            }

            int[] randomMove = legalMoves.get(random.nextInt(legalMoves.size()));
            String from = chessWindow.getBoard().toChessNotation(randomMove[0], randomMove[1]);
            String to = chessWindow.getBoard().toChessNotation(randomMove[2], randomMove[3]);
            String randomMoveNotation = from + to;

            System.out.println("Random Move (Black): " + randomMoveNotation);
            updateMoveHistory(randomMoveNotation);

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    System.out.println("Applying Black's random move to the board: " + randomMoveNotation);
                    chessWindow.movePiece(randomMoveNotation);
                    chessWindow.displayChessPieces(-1, -1);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
            latch.await();

            System.out.println("Board state after Black's move:");
            chessWindow.getBoard().printBoardWithIndices();

            // Inform Stockfish about the updated position after Black's move
            stockfish.sendCommand("position startpos moves " + moveHistory.toString());
        } catch (Exception e) {
            System.out.println("Exception in makeRandomMove: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateMoveHistory(String move) {
        if (moveHistory.length() > 0) {
            moveHistory.append(" ");
        }
        moveHistory.append(move);
    }

    private boolean isGameOver() {
        // Go through StockfishConnector and add function to determine
        // if game is over or not.
       return false;
    }
}
