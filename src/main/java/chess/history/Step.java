package chess.history;

import java.io.Serializable;

import chess.model.Move;
import chess.model.Piece;
import chess.model.PieceColor;
import chess.model.PieceType;
import chess.model.Position;

/**
 * A reversible representation of one action in the game history.
 *
 * <p>
 * This class carries:
 * <ul>
 *   <li>Model data to apply/undo the move deterministically (without snapshots)</li>
 *   <li>UI-friendly data to render move history</li>
 * </ul>
 */
public class Step implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Move move;

    // UI-friendly
    private final PieceColor moverColor;
    private final PieceType moverType;
    private final String displayText;

    // Reversible info
    private final Piece capturedPiece; // null if none

    // Special move flags / metadata to support deterministic undo/redo
    private final boolean castling;
    private final Position rookFrom;
    private final Position rookTo;
    private final Boolean rookHadMovedBefore;

    private final boolean enPassant;
    private final Position enPassantCapturedPawnPos;

    private final boolean promotion;
    private final Piece promotedTo; // the piece that ended up on the target square
    private final Piece originalPawn; // the pawn (before promotion)

    private final Boolean moverHadMovedBefore; // only relevant for King/Rook

    private final Position enPassantTargetBefore;
    private final Position enPassantTargetAfter;

    public Step(
            Move move,
            PieceColor moverColor,
            PieceType moverType,
            String displayText,
            Piece capturedPiece,
            boolean castling,
            Position rookFrom,
            Position rookTo,
            Boolean rookHadMovedBefore,
            boolean enPassant,
            Position enPassantCapturedPawnPos,
            boolean promotion,
            Piece promotedTo,
            Piece originalPawn,
            Boolean moverHadMovedBefore,
            Position enPassantTargetBefore,
            Position enPassantTargetAfter) {
        this.move = move;
        this.moverColor = moverColor;
        this.moverType = moverType;
        this.displayText = displayText;
        this.capturedPiece = capturedPiece;
        this.castling = castling;
        this.rookFrom = rookFrom;
        this.rookTo = rookTo;
        this.rookHadMovedBefore = rookHadMovedBefore;
        this.enPassant = enPassant;
        this.enPassantCapturedPawnPos = enPassantCapturedPawnPos;
        this.promotion = promotion;
        this.promotedTo = promotedTo;
        this.originalPawn = originalPawn;
        this.moverHadMovedBefore = moverHadMovedBefore;
        this.enPassantTargetBefore = enPassantTargetBefore;
        this.enPassantTargetAfter = enPassantTargetAfter;
    }

    public Move getMove() {
        return move;
    }

    public PieceColor getMoverColor() {
        return moverColor;
    }

    public PieceType getMoverType() {
        return moverType;
    }

    public String getDisplayText() {
        return displayText;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public boolean isCastling() {
        return castling;
    }

    public Position getRookFrom() {
        return rookFrom;
    }

    public Position getRookTo() {
        return rookTo;
    }

    public Boolean getRookHadMovedBefore() {
        return rookHadMovedBefore;
    }

    public boolean isEnPassant() {
        return enPassant;
    }

    public Position getEnPassantCapturedPawnPos() {
        return enPassantCapturedPawnPos;
    }

    public boolean isPromotion() {
        return promotion;
    }

    public Piece getPromotedTo() {
        return promotedTo;
    }

    public Piece getOriginalPawn() {
        return originalPawn;
    }

    public Boolean getMoverHadMovedBefore() {
        return moverHadMovedBefore;
    }

    public Position getEnPassantTargetBefore() {
        return enPassantTargetBefore;
    }

    public Position getEnPassantTargetAfter() {
        return enPassantTargetAfter;
    }
}
