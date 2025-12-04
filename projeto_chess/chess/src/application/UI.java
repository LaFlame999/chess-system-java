package application;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.Color;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.List;
import java.util.stream.Collectors;

public class UI {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[44m";

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static ChessPosition readChessPosition(Scanner sc) {
        try {
            String s = sc.nextLine();
            char column = s.charAt(0);
            int row = Integer.parseInt(s.substring(1));
            return new ChessPosition(column, row);
        }
        catch (RuntimeException e) {
            throw new InputMismatchException("Error reading ChessPosition. Valid values are from a1 to h8.");
        }
    }

    public static void printMatch(ChessMatch chessMatch, List<ChessPiece> captured) {
        printBoard(chessMatch.getPieces());
        System.out.println();
        printCapturedPieces(captured);

        if (!chessMatch.getCheckmate()) {
            System.out.println("Turn: " + chessMatch.getTurn());
            System.out.print("Waiting player: ");
            if (chessMatch.getCurrentPlayer() == Color.WHITE) {
                System.out.println(ANSI_WHITE + chessMatch.getCurrentPlayer() + ANSI_RESET);
            }
            else {
                System.out.println(ANSI_YELLOW + chessMatch.getCurrentPlayer() + ANSI_RESET);
            }
            if (chessMatch.getCheck()) {
                System.out.println(ANSI_RED + "CHECK!" + ANSI_RESET);
            }
        }
        else {
            System.out.println(ANSI_RED + "CHECKMATE!" + ANSI_RESET);
            System.out.println("Winner: " + chessMatch.getCurrentPlayer());
        }
    }

    public static void printBoard(ChessPiece[][] pieces) {
        printBoard(pieces, null);
    }

    public static void printBoard(ChessPiece[][] pieces, boolean[][] possibleMoves) {
        for (int i = 0; i < pieces.length; i++) {
            System.out.print(ANSI_YELLOW + (8 - i) + " " + ANSI_RESET);
            for (int j = 0; j < pieces.length; j++) {
                printPiece(pieces[i][j], (possibleMoves != null && possibleMoves[i][j]));
            }
            System.out.println();
        }
        System.out.println(ANSI_YELLOW + "  a b c d e f g h" + ANSI_RESET);
    }

    private static void printPiece(ChessPiece piece, boolean background) {
        if (background) {
            System.out.print(ANSI_BLUE);
        }
        if (piece == null) {
            System.out.print("-" + ANSI_RESET);
        }
        else {
            if (piece.getColor() == Color.WHITE) {
                System.out.print(ANSI_WHITE + piece + ANSI_RESET);
            }
            else {
                System.out.print(ANSI_YELLOW + piece + ANSI_RESET);
            }
        }
        System.out.print(" ");
    }

    public static void printCapturedPieces(List<ChessPiece> captured) {
        List<ChessPiece> white = captured.stream().filter(x -> x.getColor() == Color.WHITE).collect(Collectors.toList());
        List<ChessPiece> black = captured.stream().filter(x -> x.getColor() == Color.BLACK).collect(Collectors.toList());

        System.out.println("Captured pieces:");
        System.out.print("White: ");
        System.out.print(ANSI_WHITE);
        System.out.println(white.stream().map(Object::toString).collect(Collectors.joining(" ")));
        System.out.print(ANSI_RESET);

        System.out.print("Black: ");
        System.out.print(ANSI_YELLOW);
        System.out.println(black.stream().map(Object::toString).collect(Collectors.joining(" ")));
        System.out.print(ANSI_RESET);
    }
}
