package ru.myitschool.platformer;

import com.badlogic.gdx.Game;

import java.net.InetAddress;

public class MyGame extends Game {

    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static int newScore;
    public static float elapsedTime;
    public static boolean isLevel2Available = false;
    public static boolean isLevel3Available = false;
    public static boolean isLevel4Available = false;
    static InetAddress ipAddress;
    static String ipAddressOfServer = "?";
    static MyServer server;
    static MyClient client;
    static boolean isServer;
    static boolean isClient;
    static MyRequest requestFromClient;
    static MyResponse responseFromServer;
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
        System.out.println(getScore());

    }

    public int getScore() {
        newScore = Player.getScore();
        return newScore;
    }
    public static float getElapsedTime(){
        return elapsedTime;
    }

}
