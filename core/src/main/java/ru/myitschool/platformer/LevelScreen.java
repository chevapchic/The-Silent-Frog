package ru.myitschool.platformer;

import static ru.myitschool.platformer.MenuScreen.skin;
import static ru.myitschool.platformer.MenuScreen.skin1;
import static ru.myitschool.platformer.MyGame.SCREEN_HEIGHT;
import static ru.myitschool.platformer.MyGame.SCREEN_WIDTH;
import static ru.myitschool.platformer.MyGame.newScore;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.awt.Menu;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class LevelScreen implements Screen {

    public float playerX;
    public float playerY;
    private float playerX2;
    private  float playerY2;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private Stage stage;
    private Stage UIStage;
    private Stage skyStage;
    private FitViewport viewport;
    private static Player player;
    private static Player player2;
    private Integer maxMapSize;
    private Texture playerTexture;
    private ImageButton rightButton;
    private ImageButton leftButton;
    private ImageButton upButton;
    private ImageButton settingsButton;
    private ImageButton volumeButton;
    private ImageButton backToMenuButton;
    private FitViewport UIViewport;
    private Vector3 targetPosition; // Позиция, к которой стремится камера
    private float lerpSpeed = 0.1f;

    private Texture skyTexture;
    private float scrollX = 0;
    private OrthographicCamera orthographicCamera;
    private List<Coin> coins;
    private Texture coinTexture;
    private int score = 0;
    private final Game game;
    private Music music;
    private Sound deathSound;

    private static float elapsedTime = MyGame.getElapsedTime();
    private boolean timerRunning;
    private Label timeLabel;
    private Label levelLabel;
    private Label healthLabel;
    private Label.LabelStyle labelStyle;
    private static String timeString = null;
    private static int minutes;
    private static int seconds;
    private static int milliseconds;
    private boolean isVolumeButtonHere;
    private boolean isBackToMenuButtonHere;
    private boolean isMusicPlaying;
    private LevelTransition levelTransition;
    private CameraShake cameraShake;

    //сеть
    private InetAddress ipAddress;
    private String ipAddressOfServer = "?";
    MyServer server;
    MyClient client;
    static boolean isServer;
    static boolean isClient;
    MyRequest requestFromClient;
    MyResponse responseFromServer;
    private ImageButton createServerBut;
    private ImageButton createClientBut;
    private static final float LERP_SPEED = 0.2f;



    public LevelScreen(Game game) {
        this.game = game;
        this.server = MyGame.server;
        this.client = MyGame.client;
        this.isServer = MyGame.isServer;
        this.isClient = MyGame.isClient;
//        this.requestFromClient = MyGame.requestFromClient;
//        this.responseFromServer = MyGame.responseFromServer;
    }

    @Override
    public void show() {

        requestFromClient = new MyRequest();
        responseFromServer = new MyResponse();

        viewport = new FitViewport(SCREEN_WIDTH / 2F, SCREEN_HEIGHT / 2F);
        FitViewport skyViewport = new FitViewport(SCREEN_WIDTH / 2F, SCREEN_HEIGHT / 2F);
        UIViewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT);

        UIStage = new Stage(UIViewport);
        stage = new Stage(viewport);
        skyStage = new Stage(skyViewport);

        skyTexture = new Texture(Gdx.files.internal("SKY.png"));
        skyTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        Sky sky = new Sky(skyTexture, SCREEN_WIDTH, SCREEN_HEIGHT);
        sky.setZIndex(0);
        skyStage.addActor(sky);


        music = Gdx.audio.newMusic(Gdx.files.internal("sound/CantStopMyFeet.mp3"));
        music.setLooping(true);
        music.setVolume(0.04F);
        music.play();
        isMusicPlaying = true;

        deathSound = Gdx.audio.newSound(Gdx.files.internal("sound/death.mp3"));

        levelTransition = new LevelTransition();
        cameraShake = new CameraShake((OrthographicCamera) stage.getCamera());


        Gdx.input.setInputProcessor(stage);
        Gdx.input.setInputProcessor(skyStage);

        FileHandle fontFile = Gdx.files.internal("my/DynaPuff.ttf");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = 50;
        BitmapFont bitmapFont = generator.generateFont(params);
        labelStyle = new Label.LabelStyle(bitmapFont, Color.WHITE);

        Label coinLabel = new Label("Coins: 0", labelStyle);
        coinLabel.setY(625);
        coinLabel.setX(50);

        timerRunning = true;
        timeLabel = new Label(timeString,labelStyle);
        timeLabel.setPosition(1025, 657);

        levelLabel = new Label("Level 1", labelStyle);
        levelLabel.setPosition(560,625);

        healthLabel = new Label("Health:100", labelStyle);
        healthLabel.setPosition(coinLabel.getX(), coinLabel.getY()-50);

        TmxMapLoader tml = new TmxMapLoader();
        TiledMap tiledMap = tml.load("maps/level1.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        TileMapActor tileMapActor = new TileMapActor(tiledMap, (OrthogonalTiledMapRenderer) tiledMapRenderer);
        stage.addActor(tileMapActor);
        tileMapActor.setZIndex(1);


        maxMapSize = 120*32;
        MapLayers layers = tiledMap.getLayers();
        MapLayer objectLayer = layers.get("Слой объектов 1");
        MapObjects mapObjects = objectLayer.getObjects();
        for (MapObject mapObject : mapObjects) {
            MapProperties objectProperties = mapObject.getProperties();
            float x = (Float)objectProperties.get("x");
            float y = (Float)objectProperties.get("y");
            float width = (Float)objectProperties.get("width");
            float height = (Float)objectProperties.get("height");

            SolidActor solidActor = new SolidActor(x, y, width, height);
            stage.addActor(solidActor);
        }
        MapLayer objectLayer2 = layers.get("Шип");
        MapObjects mapObjects2 = objectLayer2.getObjects();
        for (MapObject mapObject : mapObjects2) {
            MapProperties objectProperties2 = mapObject.getProperties();
            float x = (Float)objectProperties2.get("x");
            float y = (Float)objectProperties2.get("y");
            float width = (Float)objectProperties2.get("width");
            float height = (Float)objectProperties2.get("height");

            Trap trap = new Trap(x, y, width, height);
            stage.addActor(trap);
        }

//        renderer = new OrthogonalTiledMapRenderer(tiledMap);



        Gdx.input.setInputProcessor(UIStage);
        orthographicCamera = (OrthographicCamera) stage.getCamera();
        tiledMapRenderer.setView(orthographicCamera);

        TextureRegionDrawable rightDrawableUp = new TextureRegionDrawable(new Texture("arrow2.png"));
        TextureRegionDrawable rightDrawableDown = new TextureRegionDrawable(new Texture("arrow2.png"));
        rightButton = new ImageButton(rightDrawableUp, rightDrawableDown);
        rightButton.setPosition(208, 56);
        rightDrawableUp.setMinHeight(rightButton.getHeight() * 0.3F);
        rightDrawableUp.setMinWidth(rightButton.getWidth() * 0.3F);
        rightDrawableDown.setMinHeight(rightButton.getHeight() * 0.26F);
        rightDrawableDown.setMinWidth(rightButton.getWidth() * 0.26F);
        rightButton.setSize(rightButton.getWidth()* 0.3F, rightButton.getHeight() * 0.3F);
        UIStage.addActor(rightButton);

        TextureRegionDrawable leftDrawableUp = new TextureRegionDrawable(new Texture("arrow.png"));
        TextureRegionDrawable leftDrawableDown = new TextureRegionDrawable(new Texture("arrow.png"));
        leftButton = new ImageButton(leftDrawableUp, leftDrawableDown);
        leftButton.setPosition(24,56);
        leftDrawableUp.setMinHeight(leftButton.getHeight() * 0.3F);
        leftDrawableUp.setMinWidth(leftButton.getWidth() * 0.3F);
        leftDrawableDown.setMinHeight(leftButton.getHeight() *0.26F);
        leftDrawableDown.setMinWidth(leftButton.getWidth() * 0.26F);
        leftButton.setSize(leftButton.getWidth()* 0.3F, leftButton.getHeight() * 0.3F);
        UIStage.addActor(leftButton);


        TextureRegionDrawable upDrawableUp = new TextureRegionDrawable(new Texture("arrow3.png"));
        TextureRegionDrawable upDrawableDown = new TextureRegionDrawable(new Texture("arrow3.png"));
        upButton = new ImageButton(upDrawableUp, upDrawableDown);
        upButton.setPosition(1280-168,56);
        upDrawableUp.setMinHeight(upButton.getHeight() * 0.3F);
        upDrawableUp.setMinWidth(upButton.getWidth() * 0.3F);
        upDrawableDown.setMinHeight(upButton.getHeight() * 0.26F);
        upDrawableDown.setMinWidth(upButton.getWidth() * 0.26F);
        upButton.setSize(upButton.getWidth()* 0.3F, upButton.getHeight() * 0.3F);
        UIStage.addActor(upButton);


        TextureRegionDrawable settingsDrUp = new TextureRegionDrawable(new Texture("buttons/Settings.png"));
        TextureRegionDrawable settingsDrDown = new TextureRegionDrawable(new Texture("buttons/Settings.png"));
        settingsButton = new ImageButton(settingsDrUp,settingsDrDown);
        settingsButton.setPosition(coinLabel.getX(), coinLabel.getY()-130);
        settingsDrUp.setMinHeight(settingsButton.getHeight() * 2.5F*1.5f);
        settingsDrUp.setMinWidth(settingsButton.getWidth() * 2.5F*1.5f);
        settingsDrDown.setMinHeight(settingsButton.getHeight() * 2.4F*1.5f);
        settingsDrDown.setMinWidth(settingsButton.getWidth() * 2.4F*1.5f);
        settingsButton.setSize(settingsButton.getWidth()* 2.5F*1.5f, settingsButton.getHeight() * 2.5F*1.5f);
        UIStage.addActor(settingsButton);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!isVolumeButtonHere && !isBackToMenuButtonHere) {
                    volumeButton.setVisible(true);
                    backToMenuButton.setVisible(true);
                    isVolumeButtonHere = true;
                    isBackToMenuButtonHere = true;
                } else {
                    volumeButton.setVisible(false);
                    backToMenuButton.setVisible(false);
                    isVolumeButtonHere = false;
                    isBackToMenuButtonHere = false;
                }
            }
        });

        TextureRegionDrawable volumeDrUp = new TextureRegionDrawable(new Texture("buttons/Volume.png"));
        TextureRegionDrawable volumeDrDown = new TextureRegionDrawable(new Texture("buttons/Volume.png"));
        TextureRegionDrawable musicOffDrawable = new TextureRegionDrawable(new Texture("buttons/Volume2.png"));
        volumeButton = new ImageButton(volumeDrUp,volumeDrDown);
        volumeButton.setPosition(coinLabel.getX()+80, coinLabel.getY()-130);
        volumeDrUp.setMinHeight(volumeButton.getHeight() * 2.5F*1.5f);
        volumeDrUp.setMinWidth(volumeButton.getWidth() * 2.5F*1.5f);
        musicOffDrawable.setMinHeight(volumeButton.getHeight() * 2.4F*1.5f);
        musicOffDrawable.setMinWidth(volumeButton.getWidth() * 2.4F*1.5f);
        volumeDrDown.setMinHeight(volumeButton.getHeight() * 2.4F*1.5f);
        volumeDrDown.setMinWidth(volumeButton.getWidth() * 2.4F*1.5f);
        volumeButton.setSize(volumeButton.getWidth()* 2.5F*1.5f, volumeButton.getHeight() * 2.5F*1.5f);
        UIStage.addActor(volumeButton);
        volumeButton.setVisible(false);
        volumeButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                if (isMusicPlaying) {
                    music.pause(); // Выключить музыку
                    isMusicPlaying = false;
                    volumeButton.getStyle().imageUp = musicOffDrawable;
                } else {
                    music.play();  // Включить музыку
                    isMusicPlaying = true;
                    volumeButton.getStyle().imageUp = volumeDrUp;
                }
            }
        });

        TextureRegionDrawable backToMenuDrUp = new TextureRegionDrawable(new Texture("buttons/Back.png"));
        TextureRegionDrawable backToMenuDrDown = new TextureRegionDrawable(new Texture("buttons/Back.png"));
        backToMenuButton = new ImageButton(backToMenuDrUp,backToMenuDrDown);
        backToMenuButton.setPosition(settingsButton.getX()-15, coinLabel.getY()-230);
        backToMenuDrUp.setMinHeight(backToMenuButton.getHeight() * 3.8F*1.5f);
        backToMenuDrUp.setMinWidth(backToMenuButton.getWidth() * 3.8F*1.5f);
        backToMenuDrDown.setMinHeight(backToMenuButton.getHeight() * 3.6F*1.5f);
        backToMenuDrDown.setMinWidth(backToMenuButton.getWidth() * 3.6F*1.5f);
        backToMenuButton.setSize(backToMenuButton.getWidth()* 4.8F*1.5f, backToMenuButton.getHeight() * 4.8F*1.5f);
        UIStage.addActor(backToMenuButton);
        backToMenuButton.setVisible(false);
        backToMenuButton.addListener(new ChangeListener(){
            public void changed(ChangeEvent event, Actor actor){
                levelTransition.startFade(() -> {
                    game.setScreen(new MenuScreen(game));
                    music.dispose();
                    Player.score = 0;
                    Player.JUMP = 552;
                    MyGame.elapsedTime = 0;
                });
            }
        });




        Texture playerTexture = new Texture("my/frog/FrogIdle_0.png");
        player = new Player(playerTexture, leftButton, rightButton, upButton, playerX, playerY, coinLabel, 3);
        player.setPosition(100, 500);
        player2 = new Player(playerTexture, leftButton, rightButton, upButton, playerX2, playerY2, coinLabel, 3);
        player2.setPosition(100, 500);
//        player.setPosition(1785, 1000);
        stage.addActor(player);
        stage.addActor(player2);

        player.setZIndex(2);

        UIStage.addActor(coinLabel);
//        UIStage.addActor(timeLabel);
        UIStage.addActor(levelLabel);
        UIStage.addActor(healthLabel);


        createCoins();
        createNetButtons();


    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        ScreenUtils.clear(0.1254902f, 0.11372549f, 0.1254902f, 1F);
        scrollX += 20 * delta;
        MyGame.newScore = score;
//        player.skin(0);
//        player2.skin(1);

        if (isServer) {
            // Отправляем свои координаты
            server.updateServerPlayer(player.getX(), player.getY(), player.getSkin(), player.getFacingRight());
            MyRequest request = server.getRequest();
            player.handleInput();

            if(request!= null) {
                player2.playerX += (request.x - player2.playerX) * LERP_SPEED;
                player2.playerY += (request.y - player2.playerY) * LERP_SPEED;
                player2.setPosition(player2.playerX, player2.playerY);
                player2.setSkin(request.skin);
                player2.setFacingRight(request.facingRight);
            }
        } else if (isClient) {
            // Отправляем свои координаты
            client.sendPosition(player2.getX(), player2.getY(), player2.getSkin(), player2.getFacingRight());
            // Получаем данные сервера
            player2.setSkin(skin);
            MyResponse response = client.getResponse();
            player2.handleInput();


            if (response != null) {
                player.playerX += (response.x - player.playerX) * LERP_SPEED;
                player.playerY += (response.y - player.playerY) * LERP_SPEED;
                player.setPosition(player.playerX, player.playerY);
                player.setSkin(response.skin);
                player.setFacingRight(response.facingRight);
            }
        }

        skyStage.act(delta);
        skyStage.draw();

        stage.act(delta);
        stage.draw();


        UIStage.act(delta);
        UIStage.draw();

        healthLabel.setText("Health:" + String.valueOf(player.getCurrentHealth()));

        levelTransition.render(delta);
        if (levelTransition.isTransitionComplete()) {
            Gdx.input.setInputProcessor(stage);
        }
        if(Player.isScreenShaking){
            shakeCamera(5,0.3f);
            Player.isScreenShaking = false;
        }


        if (timerRunning) {elapsedTime += Gdx.graphics.getDeltaTime();}
        minutes = (int) (elapsedTime / 60); // Целые минуты
        seconds = (int) (elapsedTime % 60); // Секунды (остаток от деления на 60)
        milliseconds = (int) ((elapsedTime * 100) % 100);
        timeString = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds);
        timeLabel.setText(timeString);
        MyGame.elapsedTime = elapsedTime;




        if(player.getY()<-256){
            player.die();
        }
        if(player2.getY()<-256){
            player2.die();
        }
        if(playerX>2100 && playerY<-100){
            levelTransition.startFade(() -> {
                game.setScreen(new Level2Screen(game));
                MyGame.newScore = score;

                MyGame.isLevel2Available = true;
                music.dispose();

            });

        }


        updateCamera(stage.getCamera());
        Matrix4 skyProjection = stage.getCamera().combined.cpy(); // Копия матрицы камеры
        float parallax = 0.5F;
        skyProjection.translate(-stage.getCamera().position.x * (1 - parallax), -stage.getCamera().position.y * (1 - parallax), 0);

    }
    public void shakeCamera(float intensity, float duration) {
        cameraShake.shake(intensity, duration);
    }



    public void updateCamera(Camera camera){

        if(isServer) {
            playerX = player.getX() + player.getWidth() / 2;
            playerY = player.getY() + player.getWidth() / 2;
            cameraShake.update(Gdx.graphics.getDeltaTime());
            targetPosition = new Vector3();
            targetPosition.set(MathUtils.clamp(playerX, SCREEN_WIDTH / 4F, maxMapSize - SCREEN_WIDTH / 4F), playerY + 100, 0);
            camera.position.y = camera.position.y - 10;
            camera.position.lerp(targetPosition, lerpSpeed);
            tiledMapRenderer.setView((OrthographicCamera) camera);
        }
        if(isClient){
            playerX2 = player2.getX() + player2.getWidth() / 2;
            playerY2 = player2.getY() + player2.getWidth() / 2;
            cameraShake.update(Gdx.graphics.getDeltaTime());
            targetPosition = new Vector3();
            targetPosition.set(MathUtils.clamp(playerX2, SCREEN_WIDTH / 4F, maxMapSize - SCREEN_WIDTH / 4F), playerY2 + 100, 0);
            camera.position.y = camera.position.y - 10;
            camera.position.lerp(targetPosition, lerpSpeed);
            tiledMapRenderer.setView((OrthographicCamera) camera);
        }
    }



    private void createNetButtons(){
        TextureRegionDrawable csbDrUp = new TextureRegionDrawable(new Texture("buttons/Level1Button.png"));
        TextureRegionDrawable csbDrDown = new TextureRegionDrawable(new Texture("buttons/Level1Button.png"));
        createServerBut = new ImageButton(csbDrUp, csbDrDown);
        createServerBut.setPosition(300, 500);
        createServerBut.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(!isServer && !isClient) {
                    new Thread(() -> {
                        try {
                            MyGame.server = new MyServer(MyGame.responseFromServer);
//                            MyGame.ipAddressOfServer = detectIP();
                            Gdx.app.postRunnable(() -> {
                                MyGame.isServer = true;
                                levelTransition.startFade(() -> {
                                    game.setScreen(new LevelScreen(game));
                                    music.dispose();
                                });
                            });
                        } catch (Exception e) {
                            Gdx.app.error("SERVER", "Creation failed", e);
                        }
                    }).start();
                }
            }
        });
        UIStage.addActor(createServerBut);

        TextureRegionDrawable ccbDrUp = new TextureRegionDrawable(new Texture("buttons/Level1Button.png"));
        TextureRegionDrawable ccbDrDown = new TextureRegionDrawable(new Texture("buttons/Level1Button.png"));
        createClientBut = new ImageButton(ccbDrUp, ccbDrDown);
        createClientBut.setPosition(500, 500);
        createClientBut.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(!isServer && !isClient) {
                    new Thread(() -> {
                        try {
                            MyGame.client = new MyClient(MyGame.requestFromClient);
//                            String hostAddress = MyGame.client.getIp().getHostAddress();
                            Gdx.app.postRunnable(() -> {
                                if(MyGame.client.isCantConnected) {
                                    MyGame.client = null;
                                    MyGame.ipAddressOfServer = "Server not found";
                                } else {
                                    MyGame.isClient = true;
//                                    MyGame.ipAddressOfServer = hostAddress;
                                    levelTransition.startFade(() -> {
                                        game.setScreen(new LevelScreen(game));
                                        music.dispose();
                                    });
                                }
                            });
                        } catch (Exception e) {
                            Gdx.app.error("CLIENT", "Connection failed", e);
                        }
                    }).start();
                }
            }
        });
        UIStage.addActor(createClientBut);
    }
//    public String detectIP() {
//        try {
//            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//            while (interfaces.hasMoreElements()) {
//                NetworkInterface networkInterface = interfaces.nextElement();
//                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
//                while (addresses.hasMoreElements()) {
//                    InetAddress address = addresses.nextElement();
//                    if (!address.isLinkLocalAddress() && !address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1) {
//                        MyGame.ipAddress = address;
//                        //System.out.println("IP-адрес устройства: " + ipAddress.getHostAddress());
//                    }
//                }
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//
//        if(MyGame.ipAddress != null){
//            return MyGame.ipAddress.getHostAddress();
//        }
//        return "";
//    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        UIViewport.update(width, height);
    }
    public static float getElapsedTime(){
        return elapsedTime;
    }

    @Override
    public void pause() {
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.stop();
        }
        // Освобождение остальных ресурсов
        deathSound.dispose();
        music.dispose();
        skyTexture.dispose();
        coinTexture.dispose();
        playerTexture.dispose();
        stage.dispose();
        UIStage.dispose();
        skyStage.dispose();
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.stop();
        }
        // Освобождение остальных ресурсов
        deathSound.dispose();
        music.dispose();
        skyTexture.dispose();
        coinTexture.dispose();
        playerTexture.dispose();
        stage.dispose();
        UIStage.dispose();
        skyStage.dispose();
    }
    private void createCoins(){
        coinTexture = new Texture("spinning coin_0.png");
        coins = new ArrayList<>();
//РАССТОЯНИЕ МЕЖДУ МОНЕТАМИ == 30-40
// +0|||ВЫСОТА МОНЕТЫ ОТ ПОЛА == 20
        coins.add(new Coin(240, 308, coinTexture));
        coins.add(new Coin(280, 308, coinTexture));

        coins.add(new Coin(404, 308, coinTexture));
        coins.add(new Coin(444, 308, coinTexture));

        coins.add(new Coin(554, 340, coinTexture));
        coins.add(new Coin(594, 340, coinTexture));
        coins.add(new Coin(634, 340, coinTexture));

        coins.add(new Coin(751, 276, coinTexture));
        coins.add(new Coin(791, 276, coinTexture));

        coins.add(new Coin(912, 308, coinTexture));
        coins.add(new Coin(952, 308, coinTexture));
        coins.add(new Coin(987, 308, coinTexture));

        coins.add(new Coin(1100, 340, coinTexture));
        coins.add(new Coin(1150, 340, coinTexture));

        coins.add(new Coin(1260, 308, coinTexture));
        coins.add(new Coin(1306, 308, coinTexture));

        coins.add(new Coin(1390, 340, coinTexture));
        coins.add(new Coin(1430, 340, coinTexture));
        coins.add(new Coin(1470, 340, coinTexture));

        coins.add(new Coin(1550, 372, coinTexture));
        coins.add(new Coin(1590, 372, coinTexture));
        coins.add(new Coin(1630, 372, coinTexture));

        coins.add(new Coin(1745, 340, coinTexture));
        coins.add(new Coin(1785, 340, coinTexture));


        coins.add(new Coin(1903, 372, coinTexture));
        coins.add(new Coin(1943, 372, coinTexture));
        coins.add(new Coin(1983, 372, coinTexture));



        //НИЖЕ - СТРЕЛКА В КОНЦЕ
        coins.add(new Coin(2120, 308, coinTexture));
        coins.add(new Coin(2140, 308, coinTexture));
        coins.add(new Coin(2120, 308-20, coinTexture));
        coins.add(new Coin(2140, 308-20, coinTexture));
        coins.add(new Coin(2120, 308-40, coinTexture));
        coins.add(new Coin(2140, 308-40, coinTexture));
        coins.add(new Coin(2120, 308-60, coinTexture));
        coins.add(new Coin(2140, 308-60, coinTexture));
        coins.add(new Coin(2120, 308-80, coinTexture));
        coins.add(new Coin(2140, 308-80, coinTexture));
        coins.add(new Coin(2100, 308-80, coinTexture));
        coins.add(new Coin(2160, 308-80, coinTexture));
        coins.add(new Coin(2120, 308-100, coinTexture));
        coins.add(new Coin(2140, 308-100, coinTexture));


        for (Coin coin : coins) {
            stage.addActor(coin);
        }
    }


    class Sky extends Actor {
        private Texture texture;
        private float layerWidth, layerHeight;

        public Sky(Texture texture, float layerWidth, float layerHeight) {
            this.texture = texture;
            texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            this.layerWidth = SCREEN_WIDTH;
            this.layerHeight = SCREEN_HEIGHT;
            setBounds(0, 0, layerWidth, layerHeight);
        }

        public void draw(Batch batch, float parentAlpha) {
            // Вычисляем смещение
            float offset = scrollX % layerWidth;

            // Отрисовываем текстуру с повторением (две копии, чтобы избежать пробелов)
            batch.draw(texture, -offset, 0, layerWidth, layerHeight, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
            batch.draw(texture, -offset + layerWidth, 0, layerWidth, layerHeight, 0, 0, texture.getWidth(), texture.getHeight(), false, false);

            // Если ширина экрана больше ширины текстуры, отрисовываем дополнительные копии
            if (layerWidth < SCREEN_WIDTH) {
                float additionalOffset = -offset + 2 * layerWidth; //Сдвигаем на ширину еще одной текстуры
                batch.draw(texture, additionalOffset, 0, layerWidth, layerHeight, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
            }
        }
    }
}
