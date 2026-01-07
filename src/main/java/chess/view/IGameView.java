package chess.view;

import chess.model.PieceColor;

public interface IGameView {
    void addCapturedPiece(String pieceSymbol, boolean isWhitePiece);
    void addMoveToHistoryWithColor(String moveNotation, PieceColor color);
    void clearCapturedPieces();
}
