package edu.uni.yugiooh.logic;

import java.util.Random;

import edu.uni.yugiooh.listener.BattleListener;
import edu.uni.yugiooh.model.Card;

public class Duel {
    public enum Position { ATTACK, DEFENSE }
    private final BattleListener listener;
    private int playerScore = 0;
    private int aiScore = 0;
    private final Random rnd = new Random();

    public Duel(BattleListener listener) { this.listener = listener; }

    public void playRound(Card playerCard, Position playerPos, Card aiCard) {
        Position aiPos = rnd.nextBoolean() ? Position.ATTACK : Position.DEFENSE;
        String winner;
        if (playerPos == Position.ATTACK && aiPos == Position.ATTACK) {
            if (playerCard.getAtk() > aiCard.getAtk()) { playerScore++; winner = "Player"; }
            else if (playerCard.getAtk() < aiCard.getAtk()) { aiScore++; winner = "AI"; }
            else { winner = "Tie"; }
        } else if (playerPos == Position.ATTACK && aiPos == Position.DEFENSE) {
            if (playerCard.getAtk() > aiCard.getDef()) { playerScore++; winner = "Player"; }
            else if (playerCard.getAtk() < aiCard.getDef()) { aiScore++; winner = "AI"; }
            else { winner = "Tie"; }
        } else if (playerPos == Position.DEFENSE && aiPos == Position.ATTACK) {
            if (aiCard.getAtk() > playerCard.getDef()) { aiScore++; winner = "AI"; }
            else if (aiCard.getAtk() < playerCard.getDef()) { playerScore++; winner = "Player"; }
            else { winner = "Tie"; }
        } else {
            if (playerCard.getDef() > aiCard.getDef()) { playerScore++; winner = "Player"; }
            else if (playerCard.getDef() < aiCard.getDef()) { aiScore++; winner = "AI"; }
            else { winner = "Tie"; }
        }
        listener.onTurn(playerCard.getName() + " (" + playerPos + ")",
                aiCard.getName() + " (" + aiPos + ")", winner);
        listener.onScoreChanged(playerScore, aiScore);
        if (playerScore >= 2 || aiScore >= 2) {
            String finalWinner = playerScore > aiScore ? "Player" : "AI";
            listener.onDuelEnded(finalWinner);
        }
    }

    public void reset() { playerScore = 0; aiScore = 0; }
}