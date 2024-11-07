package com.george.main;

import com.george.window.ChessWindow;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;

public class Main {

    private static ChessWindow chessWindow;

    public static void main(String[] args) {
        Platform.startup(() -> {
            try {
                chessWindow = new ChessWindow();
                chessWindow.start(new javafx.stage.Stage());

                // I will run the engine in a different thread, so we
                // can easily see the pieces move on the board.
                // The window and engine must run in different threads. 
                ChessGame chessGame = new ChessGame(chessWindow);
                Thread gameThread = new Thread(() -> {
                    try {
                        chessGame.startGame();
                    } catch (IOException | InterruptedException e) {
                    }
                });

                gameThread.setDaemon(true);
                gameThread.start();
            } catch (Exception e) {
            }
        });
    }
}
