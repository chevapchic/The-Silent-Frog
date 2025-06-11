package ru.myitschool.platformer;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;

/**
 * Класс - сервер
 * принимает запрос от клиента и отправляет ему ответ
 */

public class MyServer {

    Server server;
    MyResponse serverResponse;
    private MyRequest lastRequest;

    public MyServer(final MyResponse response) {
        server = new Server();

        Kryo kryoServer = server.getKryo();
        kryoServer.register(MyRequest.class);
        kryoServer.register(MyResponse.class);


        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof MyRequest) {
                    lastRequest = (MyRequest) object;
//                    Gdx.app.log("SERVER", "Received from client: " + lastRequest.x + "," + lastRequest.y);
                }
            }
        });

        try {
            server.bind(54557, 54779);
            server.update(250);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public MyRequest getRequest() {
        MyRequest req = lastRequest;
        lastRequest = null; // Очищаем после чтения
        return req;
    }
    public void updateServerPlayer(float x, float y, int skin, boolean facingRight) {
        serverResponse = new MyResponse(x, y, skin, facingRight);
        server.sendToAllTCP(serverResponse);
//        Gdx.app.log("SERVER", "Sent server position: " + x + "," + y);
    }

    public void stop() {
        if (server != null) {
            server.stop();
            try {
                Thread.sleep(500); // Короткая пауза
            } catch (InterruptedException ignored) {}
        }
    }
}
