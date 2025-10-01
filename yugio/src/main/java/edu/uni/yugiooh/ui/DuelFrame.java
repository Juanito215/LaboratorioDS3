package edu.uni.yugiooh.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import edu.uni.yugiooh.api.YgoApiClient;
import edu.uni.yugiooh.listener.BattleListener;
import edu.uni.yugiooh.logic.Duel;
import edu.uni.yugiooh.model.Card;

public class DuelFrame extends JFrame implements BattleListener {
    private final JButton btnLoad = new JButton("Cargar Cartas");
    private final JButton btnStart = new JButton("Iniciar Duelo");

    private final JTextArea logArea = new JTextArea(20, 28);
    private final JPanel playerPanel = new JPanel(new GridLayout(1, 3, 15, 15));
    private final JPanel aiPanel = new JPanel(new GridLayout(1, 3, 15, 15));

    private final JLabel lblScore = new JLabel("Jugador: 0   |   IA: 0", SwingConstants.CENTER);

    private final List<Card> playerCards = new ArrayList<>();
    private final List<Card> aiCards = new ArrayList<>();
    private final YgoApiClient apiClient = new YgoApiClient();
    private Duel duel;

    public DuelFrame() {
        super("Yu-Gi-Oh!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 🔹 Encabezado con marcador y botones
        JPanel top = new JPanel(new BorderLayout());
        lblScore.setFont(new Font("Arial", Font.BOLD, 18));
        lblScore.setForeground(Color.WHITE);
        lblScore.setOpaque(true);
        lblScore.setBackground(new Color(40, 40, 80));
        lblScore.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        top.add(lblScore, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        styleButton(btnLoad, new Color(70, 130, 180));
        styleButton(btnStart, new Color(34, 139, 34));
        controls.add(btnLoad);
        controls.add(btnStart);
        top.add(controls, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // 🔹 Panel central con AI arriba y jugador abajo
        JPanel center = new JPanel(new GridLayout(2, 1, 10, 10));
        aiPanel.setBorder(BorderFactory.createTitledBorder("Cartas de la Máquina"));
        playerPanel.setBorder(BorderFactory.createTitledBorder("Tus Cartas"));
        center.add(aiPanel);
        center.add(playerPanel);
        add(center, BorderLayout.CENTER);

        // 🔹 Log de batalla a la derecha
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Registro de Batalla"));
        add(scrollLog, BorderLayout.EAST);

        // Config inicial
        btnStart.setEnabled(false);
        btnLoad.addActionListener(this::onLoadClicked);
        btnStart.addActionListener(e -> onStartClicked());

        setSize(1200, 750);
        setLocationRelativeTo(null);
    }

    private void styleButton(JButton button, Color bg) {
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(140, 40));
    }

    private void onLoadClicked(ActionEvent e) {
        btnLoad.setEnabled(false);
        log("Cargando cartas...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                playerCards.clear(); aiCards.clear();
                for (int i = 0; i < 3; i++) {
                    playerCards.add(apiClient.getRandomMonsterCard());
                    aiCards.add(apiClient.getRandomMonsterCard());
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    renderCards();
                    log("Cartas cargadas correctamente.");
                    btnStart.setEnabled(true);
                } catch (InterruptedException | ExecutionException ex) {
                    log("Error cargando cartas: " + ex.getCause().getMessage());
                } finally {
                    btnLoad.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void renderCards() {
        aiPanel.removeAll();
        playerPanel.removeAll();

        for (Card c : aiCards) {
            aiPanel.add(createCardPanel(c, -1, false));
        }
        for (int i = 0; i < playerCards.size(); i++) {
            playerPanel.add(createCardPanel(playerCards.get(i), i, true));
        }

        aiPanel.revalidate(); aiPanel.repaint();
        playerPanel.revalidate(); playerPanel.repaint();
    }

    private JPanel createCardPanel(Card c, int index, boolean isPlayer) {
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setPreferredSize(new Dimension(180, 260));
        cardPanel.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2));
        cardPanel.setBackground(new Color(245, 245, 245));

        JLabel lbl = new JLabel("<html><b>" + c.getName() + "</b></html>", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        cardPanel.add(lbl, BorderLayout.NORTH);

        if (c.getImageUrl() != null) {
            try {
                Image img = ImageIO.read(new URL(c.getImageUrl()));
                Image scaled = img.getScaledInstance(140, 200, Image.SCALE_SMOOTH);
                cardPanel.add(new JLabel(new ImageIcon(scaled)), BorderLayout.CENTER);
            } catch (IOException ex) {
                cardPanel.add(new JLabel("[Imagen no disponible]", SwingConstants.CENTER), BorderLayout.CENTER);
            }
        }

        JLabel stats = new JLabel(String.format("ATK: %d  DEF: %d", c.getAtk(), c.getDef()), SwingConstants.CENTER);
        stats.setFont(new Font("Arial", Font.BOLD, 12));
        stats.setForeground(new Color(50, 50, 50));
        cardPanel.add(stats, BorderLayout.SOUTH);

        if (isPlayer) {
            JPanel actions = new JPanel(new GridLayout(2, 1, 5, 5));

            JButton btnAttack = new JButton("Atacar");
            btnAttack.setBackground(new Color(220, 20, 60));
            btnAttack.setForeground(Color.WHITE);
            btnAttack.setFont(new Font("Arial", Font.BOLD, 12));
            btnAttack.setFocusPainted(false);
            btnAttack.setPreferredSize(new Dimension(80, 40)); // cuadrado compacto

            JButton btnDefend = new JButton("Defender");
            btnDefend.setBackground(new Color(70, 130, 180));
            btnDefend.setForeground(Color.WHITE);
            btnDefend.setFont(new Font("Arial", Font.BOLD, 12));
            btnDefend.setFocusPainted(false);
            btnDefend.setPreferredSize(new Dimension(80, 40)); // cuadrado compacto

            actions.add(btnAttack);
            actions.add(btnDefend);
            cardPanel.add(actions, BorderLayout.EAST);

            btnAttack.addActionListener(ev -> onPlayerPlays(index, Duel.Position.ATTACK));
            btnDefend.addActionListener(ev -> onPlayerPlays(index, Duel.Position.DEFENSE));
        }


        return cardPanel;
    }

    private void onStartClicked() {
        if (playerCards.size() < 3 || aiCards.size() < 3) {
            log("Ambos jugadores deben tener 3 cartas antes de iniciar.");
            return;
        }
        duel = new Duel(this); duel.reset();
        log("Duelo iniciado. Selecciona una carta y elige Atacar o Defender.");
        btnStart.setEnabled(false);
    }

    private void onPlayerPlays(int playerIndex, Duel.Position pos) {
        if (duel == null) { log("Inicia el duelo primero."); return; }
        if (playerIndex < 0 || playerIndex >= playerCards.size()) return;

        Card playerCard = playerCards.remove(playerIndex);
        Card aiCard = aiCards.remove((int) (Math.random() * aiCards.size()));

        renderCards();
        duel.playRound(playerCard, pos, aiCard);
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // 🔹 Métodos del BattleListener
    @Override public void onTurn(String playerCard, String aiCard, String winner) {
        log(String.format("▶ Turno: Jugador %s vs IA %s -> Ganador: %s", playerCard, aiCard, winner));
    }
    @Override public void onScoreChanged(int playerScore, int aiScore) {
        lblScore.setText(String.format("Jugador: %d   |   IA: %d", playerScore, aiScore));
    }
    @Override public void onDuelEnded(String winner) {
        log("🏆 Duelo terminado. Ganador: " + winner);
        JOptionPane.showMessageDialog(this, "Ganador: " + winner, "Resultado", JOptionPane.INFORMATION_MESSAGE);
        duel = null; btnStart.setEnabled(true);
    }
    @Override public void onError(String message) { log("Error: " + message); }
}
