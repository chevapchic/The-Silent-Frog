package ru.myitschool.platformer;

/**
 * Класс-ответ от сервера-клиенту
 */
public class MyResponse {
    public float x; // Координаты серверного игрока (player)
    public float y;

    public MyResponse() {} // Для Kryo

    public MyResponse(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
