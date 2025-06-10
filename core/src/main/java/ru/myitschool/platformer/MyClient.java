package ru.myitschool.platformer;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Класс-клиент
 * отправляет запрос серверу и принимает от него ответ
 */

public class MyClient {
    Client client;
    boolean isCantConnected;
    private  MyRequest clientRequest;
    private MyResponse response;
    private MyResponse lastResponse;
    private InetAddress  host;

    public MyClient(MyRequest request) {
        clientRequest = new MyRequest();
        response = new MyResponse();
        client = new Client();

        Kryo kryoClient = client.getKryo();
        kryoClient.register(MyRequest.class);
        kryoClient.register(MyResponse.class);

        try{
            host = client.discoverHost(54778, 20000);
        }catch(Exception e){  }
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof MyResponse) {
                    lastResponse = (MyResponse) object;
                    Gdx.app.log("CLIENT", "Received from server: " + lastResponse.x + "," + lastResponse.y);
                }
            }});
        client.addListener(new Listener() {
            @Override
            public void disconnected(Connection connection) {
                System.out.println("Соединение закрыто!");
            }
        });
        new Thread(() -> {
            try {
                client.start();
                client.connect(5000, host, 54556, 54778);
            } catch (IOException e) {
                Gdx.app.error("CLIENT", "Connection failed: " + e.getMessage());
            }
        }).start();
    }


    public MyResponse getResponse() {
        return lastResponse;
    }

    public void sendPosition(float x, float y, int skin, boolean facingRight) {
        MyRequest request = new MyRequest(x, y, skin, facingRight);
        client.sendTCP(request);
        Gdx.app.log("CLIENT", "Sent client position: " + x + "," + y);
    }


    public InetAddress  getIp() {
        return host;
    }
    public boolean isCantConnected() {
        return isCantConnected;
    }

    public void stop(){
        client.stop();
        client.close();
        try {
            client.dispose();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
