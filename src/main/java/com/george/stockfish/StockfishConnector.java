package com.george.stockfish;

import java.io.*;

public class StockfishConnector {

    private Process stockfish;
    private BufferedReader input;
    private BufferedWriter output;
    private static final String ENGINE_SOURCE = "stockfish/stockfish-windows-x86-64-avx2";

    public boolean startEngine() {
        try {
            stockfish = new ProcessBuilder(ENGINE_SOURCE).start();
            input = new BufferedReader(new InputStreamReader(stockfish.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(stockfish.getOutputStream()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendCommand(String command) throws IOException {
        output.write(command + "\n");
        output.flush();
    }

    public String getResponse() throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            sb.append(line).append("\n");
            if (line.equals("uciok") || line.startsWith("bestmove") || line.equals("readyok")) {
                break;
            }
        }
        return sb.toString();
    }

    public String getBestMove() throws IOException {
        String bestMove = null;
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            sb.append(line).append("\n");
            if (line.startsWith("bestmove")) {
                String[] parts = line.split(" ");
                bestMove = parts[1];
                break;
            }
        }
        return bestMove;
    }

    public void stopEngine() {
        try {
            sendCommand("quit");
            stockfish.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the current game state with the provided move history to Stockfish.
     *
     * @param moveHistory The current move history in UCI format (e.g., "e2e4 e7e5").
     * @throws IOException If an error occurs while sending the command.
     */
    public void updateGameState(String moveHistory) throws IOException {
        if (moveHistory == null || moveHistory.isEmpty()) {
            sendCommand("position startpos");
        } else {
            sendCommand("position startpos moves " + moveHistory);
        }
    }

    /**
     * Sends a command to Stockfish to prepare for the next move calculation.
     *
     * @param timeLimitMillis The time limit for Stockfish to calculate the best move, in milliseconds.
     * @throws IOException If an error occurs while sending the command.
     */
    public void calculateBestMove(int timeLimitMillis) throws IOException {
        sendCommand("go movetime " + timeLimitMillis);
    }
}
