package edu.uni.ygoduellite.ui;

import edu.uni.ygoduellite.api.YgoApiClient;
import edu.uni.ygoduellite.listener.BattleListener;
import edu.uni.ygoduellite.logic.Duel;
import edu.uni.ygoduellite.model.Card;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DuelFrame extends JFrame implements BattleListener {
    private final JButton btnLoad = new JButton("Cargar cartas");
    private final JButton btnStart = new JButton("Iniciar duelo");
    private final JTextArea logArea = new JTextArea(12, 40);
    private final JPanel playerPanel = new JPanel();
    private final List<Card> playerCards = new ArrayList<>();
    private final List<Card> aiCards = new ArrayList<>();
    private final YgoApiClient apiClient = new YgoApiClient();
    private Duel duel;

    public DuelFrame() {
        super("Yu-Gi-Oh! Duel Lite");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel top = new JPanel();
        top.add(btnLoad);
        top.add(btnStart);
        add(top, BorderLayout.NORTH);
        playerPanel.setLayout(new FlowLayout());
        add(new JScrollPane(playerPanel), BorderLayout.CENTER);
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);
        btnStart.setEnabled(false);
        btnLoad.addActionListener(this::onLoadClicked);
        btnStart.addActionListener(e -> onStartClicked());
        setSize(900, 700);
        setLocationRelativeTo(null);
    }

    private void onLoadClicked(ActionEvent e) {
        btnLoad.setEnabled(false);
        log("Cargando cartas...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                playerCards.clear(); aiCards.clear();
                for (int i = 0; i < 3; i++) {
                    playerCards.add(apiClient.fetchRandomMonsterCard());
                    aiCards.add(apiClient.fetchRandomMonsterCard());
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    renderPlayerCards();
                    log("Cartas cargadas correctamente.");
                    btnStart.setEnabled(true);
                } catch (InterruptedException | ExecutionException ex) {
                    log("Error cargando cartas: " + ex.getCause().getMessage());
                } finally { btnLoad.setEnabled(true); }
            }
        };
        worker.execute();
    }

    private void renderPlayerCards() {
        playerPanel.removeAll();
        for (int i = 0; i < playerCards.size(); i++) {
            Card c = playerCards.get(i);
            JPanel cardPanel = new JPanel(new BorderLayout());
            JLabel lbl = new JLabel(c.getName(), SwingConstants.CENTER);
            cardPanel.add(lbl, BorderLayout.NORTH);
            if (c.getImageUrl() != null) {
                try {
                    Image img = ImageIO.read(new URL(c.getImageUrl()));
                    Image scaled = img.getScaledInstance(140, 200, Image.SCALE_SMOOTH);
                    cardPanel.add(new JLabel(new ImageIcon(scaled)), BorderLayout.CENTER);
                } catch (IOException ex) {
                    cardPanel.add(new JLabel("[Imagen no disponible]"), BorderLayout.CENTER);
                }
            } else cardPanel.add(new JLabel("[Sin imagen]"), BorderLayout.CENTER);
            JLabel stats = new JLabel(String.format("ATK: %d  DEF: %d", c.getAtk(), c.getDef()), SwingConstants.CENTER);
            cardPanel.add(stats, BorderLayout.SOUTH);
            int index = i;
            JButton btnAttack = new JButton("Atacar");
            JButton btnDefend = new JButton("Defender");
            JPanel actions = new JPanel(); actions.add(btnAttack); actions.add(btnDefend);
            cardPanel.add(actions, BorderLayout.EAST);
            btnAttack.addActionListener(ev -> onPlayerPlays(index, Duel.Position.ATTACK));
            btnDefend.addActionListener(ev -> onPlayerPlays(index, Duel.Position.DEFENSE));
            cardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            playerPanel.add(cardPanel);
        }
        playerPanel.revalidate(); playerPanel.repaint();
    }

    private void onStartClicked() {
        if (playerCards.size() < 3 || aiCards.size() < 3) { log("Ambos jugadores deben tener 3 cartas antes de iniciar."); return; }
        duel = new Duel(this); duel.reset();
        log("Duelo iniciado. Selecciona una carta y elige Atacar o Defender.");
        btnStart.setEnabled(false);
    }

    private void onPlayerPlays(int playerIndex, Duel.Position pos) {
        if (duel == null) { log("Inicia el duelo primero."); return; }
        if (playerIndex < 0 || playerIndex >= playerCards.size()) return;
        Card playerCard = playerCards.remove(playerIndex);
        Card aiCard = aiCards.remove((int) (Math.random() * aiCards.size()));
        renderPlayerCards();
        duel.playRound(playerCard, pos, aiCard);
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    @Override public void onTurn(String playerCard, String aiCard, String winner) {
        log(String.format("Turno: Jugador %s vs AI %s -> Ganador: %s", playerCard, aiCard, winner));
    }
    @Override public void onScoreChanged(int playerScore, int aiScore) {
        log(String.format("Puntaje -> Jugador: %d  AI: %d", playerScore, aiScore));
    }
    @Override public void onDuelEnded(String winner) {
        log("Duelo terminado. Ganador: " + winner);
        JOptionPane.showMessageDialog(this, "Ganador: " + winner);
        duel = null; btnStart.setEnabled(true);
    }
    @Override public void onError(String message) { log("Error: " + message); }
}