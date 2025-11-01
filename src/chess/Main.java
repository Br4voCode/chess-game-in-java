package chess;

import chess.game.*;
import chess.game.Game;
import chess.model.*;
import chess.model.PieceColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Simple Swing GUI to play against AI (explicit-tree minimax).
 * Click a piece to select, click target to move.
 */
public class Main {

    private JFrame frame;
    private JButton[][] squares = new JButton[8][8];
    private Game game;
    private Position selected = null;
    private JLabel statusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main().createAndShow();
        });
    }

    private void createAndShow() {
        // white = human, black = AI depth 3 (plies)
        game = new Game(new HumanPlayer(), new AIPlayer(PieceColor.BLACK, 3));

        frame = new JFrame("Simple Chess (explicit tree AI)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(8,8));
        Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 32);

        for (int r=0;r<8;r++) {
            for (int c=0;c<8;c++) {
                JButton b = new JButton();
                b.setFont(f);
                final int rr = r, cc = c;
                b.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onSquareClicked(new Position(rr,cc));
                    }
                });
                // color
                if ((r + c) % 2 == 0) b.setBackground(new Color(240,217,181));
                else b.setBackground(new Color(181,136,99));
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        statusLabel = new JLabel("White to move. Click a piece.");
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.SOUTH);

        updateBoardUI();

        frame.setSize(600, 600);
        frame.setVisible(true);
    }

    private void onSquareClicked(Position pos) {
        Board board = game.getBoard();
        PieceColor turn = game.getTurn();
        Piece p = board.getPieceAt(pos);

        // selecting piece
        if (selected == null) {
            if (p == null || p.getColor() != turn) {
                statusLabel.setText("Select a " + (turn==PieceColor.WHITE?"White":"Black") + " piece.");
                return;
            }
            selected = pos;
            statusLabel.setText("Selected " + p.getType() + " at " + pos + ". Choose target.");
            highlightPossibleMoves(selected);
            return;
        }

        // attempt move from selected -> pos
        Move attempt = new Move(selected, pos);
        // check legality by generating moves of current turn
        boolean legal = false;
        for (Move m : board.getAllPossibleMoves(turn)) {
            if (m.getFrom().equals(attempt.getFrom()) && m.getTo().equals(attempt.getTo())) {
                legal = true;
                break;
            }
        }
        if (legal) {
            game.applyMove(attempt);
            selected = null;
            updateBoardUI();
            statusLabel.setText("Move applied. " + game.getTurn() + " to move.");
            SwingUtilities.invokeLater(() -> aiIfNeeded());
        } else {
            statusLabel.setText("Illegal move. Select a piece.");
            selected = null;
            updateBoardUI();
        }
    }

    private void aiIfNeeded() {
        // if it's AI's turn, ask for move and apply
        Move aiMove = game.getAIMoveIfAny();
        if (aiMove != null) {
            game.applyMove(aiMove);
            updateBoardUI();
            statusLabel.setText("AI played " + aiMove + ". Your turn.");
        }
    }

    private void highlightPossibleMoves(Position pos) {
        updateBoardUI(); // clear
        Piece p = game.getBoard().getPieceAt(pos);
        if (p == null) return;
        for (Move m : game.getBoard().getAllPossibleMoves(p.getColor())) {
            if (m.getFrom().equals(pos)) {
                Position to = m.getTo();
                squares[to.getRow()][to.getCol()].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
            }
        }
        squares[pos.getRow()][pos.getCol()].setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
    }

    private void updateBoardUI() {
        Board board = game.getBoard();
        for (int r=0;r<8;r++) {
            for (int c=0;c<8;c++) {
                JButton b = squares[r][c];
                Piece p = board.getPieceAt(new Position(r,c));
                b.setText(p == null ? "" : p.toUnicode());
                b.setBorder(null);
            }
        }
    }
}
