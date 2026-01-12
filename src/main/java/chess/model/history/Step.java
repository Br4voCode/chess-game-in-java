package chess.model.history;

import java.io.Serializable;

import chess.model.pieces.PieceColor;
import chess.model.pieces.PieceType;
import chess.model.Move;
import chess.model.pieces.Piece;
import chess.model.Position;

public class Step implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Move move;

    private final PieceColor moverColor;
    private final PieceType moverType;
    private final String displayText;

    private final Piece capturedPiece;

    private final boolean castling;
    private final Position rookFrom;
    private final Position rookTo;
    private final Boolean rookHadMovedBefore;

    private final boolean enPassant;
    private final Position enPassantCapturedPawnPos;

    private final boolean promotion;
    private final Piece promotedTo;
    private final Piece originalPawn;

    private final Boolean moverHadMovedBefore;

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
