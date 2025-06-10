package ru.myitschool.platformer;

/**
 * Класс-ответ от сервера-клиенту
 */
public class MyResponse {
    public float x; // Координаты серверного игрока (player)
    public float y;
    public int skin;
    public boolean facingRight;

    public MyResponse() {} // Для Kryo

    public MyResponse(float x, float y, int skin, boolean facingRight) {
        this.x = x;
        this.y = y;
        this.skin = skin;
        this.facingRight = facingRight;
    }
}
