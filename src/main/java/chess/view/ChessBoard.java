package chess.view;

import java.util.List;
import java.util.function.Consumer;

import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;
import chess.model.Position;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Componente que representa el tablero de ajedrez con animaciones
 */
public class ChessBoard {
	private GridPane board;
	private ChessSquare[][] squares;
	private Consumer<Position> squareClickListener;
	private Board currentBoard;
	private StackPane boardContainer;

	public ChessBoard() {
		initializeBoard();
	}

	private void initializeBoard() {
		boardContainer = new StackPane();
		board = new GridPane();
		squares = new ChessSquare[8][8];

		board.setMaxSize(480, 480);
		board.setMinSize(480, 480);
		boardContainer.setMaxSize(480, 480);
		boardContainer.setMinSize(480, 480);

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				Position position = new Position(row, col);
				ChessSquare square = new ChessSquare(position);

				final int r = row, c = col;
				square.setOnMouseClicked(e -> {
					if (squareClickListener != null) {
						squareClickListener.accept(new Position(r, c));
					}
				});

				squares[row][col] = square;
				board.add(square.getRoot(), col, row);
			}
		}

		boardContainer.getChildren().add(board);
	}

	public void setSquareClickListener(Consumer<Position> listener) {
		this.squareClickListener = listener;
	}

	public void setCurrentBoard(Board board) {
		this.currentBoard = board;
	}

	public void updateSquare(Position pos, String pieceSymbol, boolean isSelected, 
			boolean isPossibleMove, boolean isCaptureMove) {
		if (isValidPosition(pos)) {
			// Obtener la casilla
			ChessSquare square = squares[pos.getRow()][pos.getCol()];

			// Si hay un cambio de pieza, limpiar primero
			String currentSymbol = square.getPieceSymbol();
			if (!currentSymbol.equals(pieceSymbol != null ? pieceSymbol : "")) {
				square.clearHighlight();
			}

			// Actualizar apariencia
			square.updateAppearance(
					pieceSymbol, isSelected, isPossibleMove, isCaptureMove
					);
		}
	}

	public void updateSquare(Position pos, String pieceSymbol, boolean isSelected, boolean isPossibleMove) {
		updateSquare(pos, pieceSymbol, isSelected, isPossibleMove, false);
	}

	public void clearHighlights() {
	    // Usar Platform.runLater para asegurar que se ejecute en el hilo de JavaFX
	    javafx.application.Platform.runLater(() -> {
	        for (int row = 0; row < 8; row++) {
	            for (int col = 0; col < 8; col++) {
	                ChessSquare square = squares[row][col];
	                if (square != null) {
	                    // Limpiar inmediatamente, sin animaciones
	                    square.getRoot().setEffect(null);
	                    square.clearHighlight();
	                }
	            }
	        }
	    });
	}


	public void highlightPossibleMoves(Position from, List<Move> possibleMoves) {
	    // Limpiar TODOS los highlights primero de forma síncrona
	    clearHighlightsImmediately();
	    
	    if (currentBoard == null) {
	        return;
	    }
	    
	    Piece selectedPiece = currentBoard.getPieceAt(from);
	    if (selectedPiece == null) {
	        return;
	    }
	    
	    // Pequeño delay para asegurar que se limpió
	    PauseTransition pause = new PauseTransition(Duration.millis(10));
	    pause.setOnFinished(e -> {
	        String selectedSymbol = selectedPiece.toUnicode();
	        
	        // Actualizar casilla seleccionada
	        updateSquare(from, selectedSymbol, true, false, false);
	        
	        // Resaltar movimientos posibles
	        for (Move move : possibleMoves) {
	            if (move.getFrom().equals(from)) {
	                Position to = move.getTo();
	                Piece targetPiece = currentBoard.getPieceAt(to);
	                String targetSymbol = targetPiece != null ? targetPiece.toUnicode() : "";
	                boolean isCapture = targetPiece != null && targetPiece.getColor() != selectedPiece.getColor();
	                
	                updateSquare(to, targetSymbol, false, true, isCapture);
	            }
	        }
	    });
	    pause.play();
	}
	
	private void clearHighlightsImmediately() {
	    for (int row = 0; row < 8; row++) {
	        for (int col = 0; col < 8; col++) {
	            ChessSquare square = squares[row][col];
	            if (square != null) {
	                // Llamar al método clearHighlight de la casilla
	                square.clearHighlight();
	            }
	        }
	    }
	}

	public void updateBoard(Board board) {
		this.currentBoard = board;
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				Position pos = new Position(row, col);
				Piece piece = board.getPieceAt(pos);
				String symbol = piece != null ? piece.toUnicode() : "";
				updateSquare(pos, symbol, false, false, false);
			}
		}
	}

	public void updateSingleSquare(Position pos) {
		if (currentBoard != null && isValidPosition(pos)) {
			Piece piece = currentBoard.getPieceAt(pos);
			String symbol = piece != null ? piece.toUnicode() : "";
			squares[pos.getRow()][pos.getCol()].updateAppearance(symbol, false, false, false);
		}
	}

	/**
	 * Método principal para animar movimientos con efecto fade
	 */
	public void animateMove(Move move, Runnable onFinished) {
		ChessSquare fromSquare = getSquare(move.getFrom());
		ChessSquare toSquare = getSquare(move.getTo());

		if (fromSquare == null || toSquare == null) {
			if (onFinished != null) onFinished.run();
			return;
		}

		// Obtener información de la pieza
		String pieceSymbol = "";
		if (!fromSquare.isEmpty()) {
			pieceSymbol = fromSquare.getPieceSymbol();
		}

		boolean isCapture = !toSquare.isEmpty();

		// Animación secuencial: fade out -> (capture) -> fade in
		animateSequentialMovement(fromSquare, toSquare, pieceSymbol, isCapture, onFinished);
	}

	private void animateSequentialMovement(ChessSquare fromSquare, ChessSquare toSquare, 
			String pieceSymbol, boolean isCapture, 
			Runnable onFinished) {

		// Paso 1: Fade out en la casilla de origen
		FadeTransition fadeOut = new FadeTransition(Duration.millis(300));

		if (fromSquare.isUsingImages() && fromSquare.getPieceImageView().isVisible()) {
			fadeOut.setNode(fromSquare.getPieceImageView());
		} else if (fromSquare.getPieceLabel().isVisible()) {
			fadeOut.setNode(fromSquare.getPieceLabel());
		} else {
			// Si no hay pieza visible, pasar al siguiente paso
			fadeOut.setNode(fromSquare.getRoot());
		}

		fadeOut.setFromValue(1.0);
		fadeOut.setToValue(0.0);

		fadeOut.setOnFinished(e -> {
			// Limpiar la casilla de origen visualmente
			fromSquare.updateAppearance("", false, false, false);

			// Si es captura, animar efecto de captura
			if (isCapture) {
				animateCaptureEffect(toSquare, () -> {
					// Después del efecto de captura, mostrar la pieza en destino
					showPieceAtDestination(toSquare, pieceSymbol, onFinished);
				});
			} else {
				// Si no es captura, mostrar directamente en destino
				showPieceAtDestination(toSquare, pieceSymbol, onFinished);
			}
		});

		fadeOut.play();
	}

	private void showPieceAtDestination(ChessSquare toSquare, String pieceSymbol, Runnable onFinished) {
	    // Primero establecer la pieza (pero invisible)
	    toSquare.updateAppearance(pieceSymbol, false, false, false);
	    
	    // Hacer invisible la representación de la pieza
	    if (toSquare.isUsingImages() && toSquare.getPieceImageView().isVisible()) {
	        toSquare.getPieceImageView().setOpacity(0);
	    } else if (toSquare.getPieceLabel().isVisible()) {
	        toSquare.getPieceLabel().setOpacity(0);
	    }
	    
	    // Efecto fade in
	    FadeTransition fadeIn = new FadeTransition(Duration.millis(400));
	    
	    if (toSquare.isUsingImages() && toSquare.getPieceImageView().isVisible()) {
	        fadeIn.setNode(toSquare.getPieceImageView());
	    } else if (toSquare.getPieceLabel().isVisible()) {
	        fadeIn.setNode(toSquare.getPieceLabel());
	    } else {
	        fadeIn.setNode(toSquare.getRoot());
	        toSquare.getRoot().setOpacity(0);
	    }
	    
	    fadeIn.setFromValue(0.0);
	    fadeIn.setToValue(1.0);
	    
	    fadeIn.setOnFinished(e -> {
	        // Asegurar que todo está visible
	        toSquare.getRoot().setOpacity(1.0);
	        if (toSquare.getPieceImageView() != null) {
	            toSquare.getPieceImageView().setOpacity(1.0);
	            toSquare.getPieceImageView().setScaleX(1.0);
	            toSquare.getPieceImageView().setScaleY(1.0);
	        }
	        if (toSquare.getPieceLabel() != null) {
	            toSquare.getPieceLabel().setOpacity(1.0);
	            toSquare.getPieceLabel().setScaleX(1.0);
	            toSquare.getPieceLabel().setScaleY(1.0);
	        }
	        
	        if (onFinished != null) {
	            onFinished.run();
	        }
	    });
	    
	    fadeIn.play();
	}
	
	/**
	 * Efecto de captura
	 */
	private void animateCaptureEffect(ChessSquare targetSquare, Runnable onCaptureFinished) {
	    // Guardar la pieza capturada temporalmente
	    String capturedSymbol = targetSquare.getPieceSymbol();
	    
	    // Efecto de sacudida y desvanecimiento
	    TranslateTransition shake = new TranslateTransition(Duration.millis(100), targetSquare.getRoot());
	    shake.setByX(5);
	    shake.setCycleCount(4);
	    shake.setAutoReverse(true);
	    
	    FadeTransition fadeOut = new FadeTransition(Duration.millis(200));
	    if (targetSquare.isUsingImages() && targetSquare.getPieceImageView().isVisible()) {
	        fadeOut.setNode(targetSquare.getPieceImageView());
	    } else if (targetSquare.getPieceLabel().isVisible()) {
	        fadeOut.setNode(targetSquare.getPieceLabel());
	    } else {
	        fadeOut.setNode(targetSquare.getRoot());
	    }
	    
	    fadeOut.setFromValue(1.0);
	    fadeOut.setToValue(0.0);
	    
	    // Ejecutar efectos en paralelo
	    ParallelTransition parallel = new ParallelTransition(shake, fadeOut);
	    parallel.setOnFinished(e -> {
	        // Limpiar la pieza capturada
	        targetSquare.updateAppearance("", false, false, false);
	        // Restaurar opacidad
	        targetSquare.getRoot().setOpacity(1.0);
	        if (targetSquare.getPieceImageView() != null) targetSquare.getPieceImageView().setOpacity(1.0);
	        if (targetSquare.getPieceLabel() != null) targetSquare.getPieceLabel().setOpacity(1.0);
	        
	        onCaptureFinished.run();
	    });
	    
	    parallel.play();
	}

	/**
	 * Animación de movimiento con fade
	 */
	private void animatePieceMovementWithFade(ChessSquare fromSquare, ChessSquare toSquare, 
			String pieceSymbol, Runnable onFinished) {
		// Preparar destino (inicialmente invisible)
		toSquare.updateAppearance(pieceSymbol, false, false, false);

		if (toSquare.isUsingImages() && toSquare.getPieceImageView().isVisible()) {
			toSquare.getPieceImageView().setOpacity(0);
		} else if (toSquare.getPieceLabel().isVisible()) {
			toSquare.getPieceLabel().setOpacity(0);
		}

		// Fade in en destino
		FadeTransition fadeIn = new FadeTransition(Duration.millis(400));

		if (toSquare.isUsingImages() && toSquare.getPieceImageView().isVisible()) {
			fadeIn.setNode(toSquare.getPieceImageView());
		} else if (toSquare.getPieceLabel().isVisible()) {
			fadeIn.setNode(toSquare.getPieceLabel());
		} else {
			fadeIn.setNode(toSquare.getRoot());
			toSquare.getRoot().setOpacity(0);
		}

		fadeIn.setFromValue(0.0);
		fadeIn.setToValue(1.0);

		fadeIn.setOnFinished(e -> {
			toSquare.getRoot().setOpacity(1.0);
			if (toSquare.getPieceImageView() != null) toSquare.getPieceImageView().setOpacity(1.0);
			if (toSquare.getPieceLabel() != null) toSquare.getPieceLabel().setOpacity(1.0);

			if (onFinished != null) {
				onFinished.run();
			}
		});

		fadeIn.play();
	}

	private boolean isValidPosition(Position pos) {
		return pos != null && 
				pos.getRow() >= 0 && pos.getRow() < 8 && 
				pos.getCol() >= 0 && pos.getCol() < 8;
	}

	public GridPane getBoard() {
		return board;
	}

	public StackPane getBoardWithAnimations() {
		return boardContainer;
	}

	public ChessSquare getSquare(Position pos) {
		if (isValidPosition(pos)) {
			return squares[pos.getRow()][pos.getCol()];
		}
		return null;
	}
}