package ru.myitschool.platformer;

import java.io.Serializable;

public class GameState implements Serializable {
    public float player1X, player1Y;
    public float player2X, player2Y;
    public long timestamp;
}
