package chess;

import board.Board;
import board.Position;
import board.Piece;
import chess.pieces.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ChessMatch {

    private Board board;
    private Color currentPlayer;
    private int turn;
    private boolean check;
    private boolean checkmate;
    private Piece enPassantVulnerable;
    private ChessPiece promoted;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();

    public ChessMatch() {
        this.board = new Board(8, 8);
        this.turn = 1;
        this.currentPlayer = Color.WHITE;
        initialSetup();
    }

    public int getTurn() {
        return turn;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getCheck() {
        return check;
    }

    public boolean getCheckmate() {
        return checkmate;
    }

    public Piece getEnPassantVulnerable() {
        return enPassantVulnerable;
    }

    public ChessPiece getPromoted() {
        return promoted;
    }

    public ChessPiece[][] getPieces() {
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) board.piece(i, j);
            }
        }
        return mat;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();
        if (!board.positionExists(position)) {
            throw new ChessException("Source position not found on the board.");
        }
        if (board.piece(position) == null) {
            throw new ChessException("There is no piece on source position.");
        }
        return board.piece(position).possibleMoves();
    }

    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();

        validateSourcePosition(source);
        validateTargetPosition(source, target);

        Piece capturedPiece = makeMove(source, target);

        promoted = null;
        if (board.piece(target) instanceof Pawn &&
                (target.getRow() == 0 || target.getRow() == 7))
        {
            promoted = (ChessPiece)board.piece(target);
            promoted = replacePromotedPiece("Q");
        }

        if (testCheck(currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new ChessException("You can't put yourself in check.");
        }

        check = (testCheck(opponent(currentPlayer))) ? true : false;

        if (testCheckmate(opponent(currentPlayer))) {
            checkmate = true;
        }
        else {
            nextTurn();
        }

        Piece movedPiece = board.piece(target);
        if (movedPiece instanceof Pawn &&
                (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2))
        {
            enPassantVulnerable = movedPiece;
        }
        else {
            enPassantVulnerable = null;
        }

        return (ChessPiece) capturedPiece;
    }

    public ChessPiece replacePromotedPiece(String type) {
        if (promoted == null) {
            throw new IllegalStateException("There is no piece to be promoted.");
        }
        if (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
            return promoted;
        }

        Position pos = promoted.getChessPosition().toPosition();
        Piece p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);

        ChessPiece newPiece = newPiece(type, promoted.getColor());
        board.placePiece(newPiece, pos);
        piecesOnTheBoard.add(newPiece);

        return newPiece;
    }

    private ChessPiece newPiece(String type, Color color) {
        if (type.equals("B")) return new Bishop(board, color);
        if (type.equals("N")) return new Knight(board, color);
        if (type.equals("Q")) return new Queen(board, color);
        return new Rook(board, color);
    }


    private Piece makeMove(Position source, Position target) {
        ChessPiece p = (ChessPiece) board.removePiece(source);
        p.increaseMoveCount();

        Piece capturedPiece = board.removePiece(target);

        // #specialmove castling kingside
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            Piece rook = board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            ((ChessPiece)rook).increaseMoveCount();
        }
        // #specialmove castling queenside
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            Piece rook = board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            ((ChessPiece)rook).increaseMoveCount();
        }

        // #specialmove en passant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == null) {
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(target.getRow() + 1, target.getColumn());
                }
                else {
                    pawnPosition = new Position(target.getRow() - 1, target.getColumn());
                }
                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }

        board.placePiece(p, target);

        if (capturedPiece != null) {
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        return capturedPiece;
    }

    public void undoMove(Position source, Position target, Piece capturedPiece) {
        ChessPiece p = (ChessPiece) board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);

        if (capturedPiece != null) {
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
            board.placePiece(capturedPiece, target);
        }

        // #specialmove castling kingside
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            Piece rook = board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            ((ChessPiece)rook).decreaseMoveCount();
        }
        // #specialmove castling queenside
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            Piece rook = board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            ((ChessPiece)rook).decreaseMoveCount();
        }

        // #specialmove en passant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
                Piece pawn = board.removePiece(target);
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(3, target.getColumn());
                }
                else {
                    pawnPosition = new Position(4, target.getColumn());
                }
                board.placePiece(pawn, pawnPosition);
            }
        }
    }

    private void validateSourcePosition(Position position) {
        if (!board.thereIsAPiece(position)) {
            throw new ChessException("There is no piece on source position.");
        }

        if (currentPlayer != ((ChessPiece)board.piece(position)).getColor()) {
            throw new ChessException("The chosen piece is not yours.");
        }

        if (!board.piece(position).isThereAnyPossibleMove()) {
            throw new ChessException("There are no possible moves for the chosen piece.");
        }
    }

    private void validateTargetPosition(Position source, Position target) {
        if (!board.piece(source).possibleMove(target)) {
            throw new ChessException("The chosen piece can't move to target position.");
        }
    }

    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private Color opponent(Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private ChessPiece king(Color color) {
        List<Piece> list = piecesOnTheBoard.stream()
                .filter(x -> ((ChessPiece)x).getColor() == color)
                .collect(Collectors.toList());

        for (Piece p : list) {
            if (p instanceof King) {
                return (King)p;
            }
        }
        throw new IllegalStateException("There is no " + color + " king on the board.");
    }

    protected boolean testCheck(Color color) {
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream()
                .filter(x -> ((ChessPiece)x).getColor() == opponent(color))
                .collect(Collectors.toList());

        for (Piece p : opponentPieces) {
            boolean[][] mat = p.possibleMoves();
            if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
                return true;
            }
        }
        return false;
    }

    protected boolean testCheckmate(Color color) {
        if (!testCheck(color)) {
            return false;
        }
        List<Piece> list = piecesOnTheBoard.stream()
                .filter(x -> ((ChessPiece)x).getColor() == color)
                .collect(Collectors.toList());

        for (Piece p : list) {
            boolean[][] mat = p.possibleMoves();
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getColumns(); j++) {
                    if (mat[i][j]) {
                        Position source = ((ChessPiece)p).getChessPosition().toPosition();
                        Position target = new Position(i, j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean testCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);
                        if (!testCheck) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void placeNewPiece(char column, int row, Color color, String pieceType) {
        Piece p = null;
        if (pieceType.equals("R")) p = new Rook(board, color);
        if (pieceType.equals("K")) p = new King(board, color, this);
        if (pieceType.equals("N")) p = new Knight(board, color);
        if (pieceType.equals("B")) p = new Bishop(board, color);
        if (pieceType.equals("Q")) p = new Queen(board, color);
        if (pieceType.equals("P")) p = new Pawn(board, color, this);

        board.placePiece(p, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(p);
    }

    private void initialSetup() {
        placeNewPiece('a', 1, Color.WHITE, "R");
        placeNewPiece('b', 1, Color.WHITE, "N");
        placeNewPiece('c', 1, Color.WHITE, "B");
        placeNewPiece('d', 1, Color.WHITE, "Q");
        placeNewPiece('e', 1, Color.WHITE, "K");
        placeNewPiece('f', 1, Color.WHITE, "B");
        placeNewPiece('g', 1, Color.WHITE, "N");
        placeNewPiece('h', 1, Color.WHITE, "R");

        for (char c = 'a'; c <= 'h'; c++) {
            placeNewPiece(c, 2, Color.WHITE, "P");
        }

        placeNewPiece('a', 8, Color.BLACK, "R");
        placeNewPiece('b', 8, Color.BLACK, "N");
        placeNewPiece('c', 8, Color.BLACK, "B");
        placeNewPiece('d', 8, Color.BLACK, "Q");
        placeNewPiece('e', 8, Color.BLACK, "K");
        placeNewPiece('f', 8, Color.BLACK, "B");
        placeNewPiece('g', 8, Color.BLACK, "N");
        placeNewPiece('h', 8, Color.BLACK, "R");

        for (char c = 'a'; c <= 'h'; c++) {
            placeNewPiece(c, 7, Color.BLACK, "P");
        }
    }
}