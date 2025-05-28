package ru.myitschool.platformer;

/**
 * Класс-запрос от клиента-серверу
 */
public class MyRequest {
    public float x; // Координаты клиентского игрока (player2)
    public float y;
    public int skin;

    public MyRequest() {} // Для Kryo

    public MyRequest(float x, float y, int skin) {
        this.x = x;
        this.y = y;
        this.skin = skin;
    }
}
