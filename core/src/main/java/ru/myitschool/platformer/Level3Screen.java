package ru.myitschool.platformer;

import static ru.myitschool.platformer.MenuScreen.skin;
import static ru.myitschool.platformer.MyGame.SCREEN_HEIGHT;
import static ru.myitschool.platformer.MyGame.SCREEN_WIDTH;
import static ru.myitschool.platformer.MyGame.newScore;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Level3Screen implements Screen{
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private Stage stage;
    private Stage UIStage;
    private Stage skyStage;
    private FitViewport viewport;
    private FitViewport skyViewport;
    private Player player;
    private Integer maxMapSize;

    private ImageButton rightButton;
    private ImageButton leftButton;
    private ImageButton upButton;
    private FitViewport UIViewport;
    public static float playerX;
    private Vector3 targetPosition; // Позиция, к которой стремится камера
    private float lerpSpeed = 0.1f;
    private float playerY;
    private Texture skyTexture;
    private float parallax = 0.5F;
    private float scrollX = 0;
    private OrthographicCamera orthographicCamera;
    private List<Coin> coins;
    private Texture coinTexture;
    private int score;
    private Label coinLabel;
    private final Game game;

    private Sound deathSound;
    private Music music;

    private float elapsedTime = MyGame.getElapsedTime();
    private boolean timerRunning;
    private String timeString;
    private Label timeLabel;
    private Label healthLabel;
    private Label levelLabel;

    private SolidActor solidActor;

    //x1 - ground / x2 - platform
    private float x1, x2;
    private float y1, y2;
    private float width1, width2;
    private float height1, height2;

    private MapObjects mapObjects2;
    private MapLayer objectLayer2;

    private ImageButton settingsButton;
    private ImageButton volumeButton;
    private ImageButton backToMenuButton;

    private boolean isVolumeButtonHere;
    private boolean isMusicPlaying;
    private boolean isBackToMenuButtonHere;

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
    private Player player2;
    private float playerX2;
    private  float playerY2;
    private int deathCount;


    public Level3Screen(Game game) {
        this.game = game;
        this.server = MyGame.server;
        this.client = MyGame.client;
        this.isServer = MyGame.isServer;
        this.isClient = MyGame.isClient;
        levelTransition = new LevelTransition();
    }

    @Override
    public void show() {
        viewport = new FitViewport(SCREEN_WIDTH / 2F, SCREEN_HEIGHT / 2F);
        skyViewport = new FitViewport(SCREEN_WIDTH/2F, SCREEN_HEIGHT/2F);
        UIViewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT);
        UIStage = new Stage(UIViewport);
        stage = new Stage(viewport);
        skyStage = new Stage(skyViewport);

        score = MyGame.newScore;
        skyTexture = new Texture(Gdx.files.internal("SKY.png"));
        skyTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat); // Бесконечная прокрутка

        Level3Screen.Sky sky = new Level3Screen.Sky(skyTexture, SCREEN_WIDTH, SCREEN_HEIGHT);
        sky.setZIndex(0);

        cameraShake = new CameraShake((OrthographicCamera) stage.getCamera());

        skyStage.addActor(sky);

        deathSound = Gdx.audio.newSound(Gdx.files.internal("sound/death.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("sound/CantStopMyFeet.mp3"));
        music.setLooping(true);
        music.setVolume(0.04F);
        music.play();
        isMusicPlaying = true;

        Gdx.input.setInputProcessor(stage);
        Gdx.input.setInputProcessor(skyStage);

        FileHandle fontFile = Gdx.files.internal("my/DynaPuff.ttf");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = 50;
        BitmapFont bitmapFont = generator.generateFont(params);
        Label.LabelStyle labelStyle = new Label.LabelStyle(bitmapFont, Color.WHITE);
        Label coinLabel = new Label("Coins: ", labelStyle);
        coinLabel.setPosition(50,625);


        timerRunning = true;
        timeLabel = new Label(timeString,labelStyle);
        timeLabel.setPosition(1025, 657);

        levelLabel = new Label("Level 3", labelStyle);
        levelLabel.setPosition(560,625);

        healthLabel = new Label("Health:100", labelStyle);
        healthLabel.setPosition(coinLabel.getX(), coinLabel.getY()-50);

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
        Level3Screen level3Screen = new Level3Screen(game);
        player = new Player(playerTexture, leftButton, rightButton, upButton, playerX, playerY, coinLabel, 3);
        player.setPosition(400, 256);
        stage.addActor(player);
        player2 = new Player(playerTexture, leftButton, rightButton, upButton, playerX2, playerY2, coinLabel, 3);

        if(MyGame.isMultiPlayer && (isServer || isClient)) {
            player2.setPosition(400, 256);
            stage.addActor(player2);
        }

        player.setZIndex(2);

        if(MyGame.isMultiPlayer){createNetButtons();}


//        player.setPosition(500,400);


        TmxMapLoader tml = new TmxMapLoader();
        TiledMap tiledMap = tml.load("maps/level3.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        TileMapActor tileMapActor = new TileMapActor(tiledMap, (OrthogonalTiledMapRenderer) tiledMapRenderer);
        stage.addActor(tileMapActor);
        tileMapActor.setZIndex(1);

        maxMapSize = 32*120;
        MapLayers layers = tiledMap.getLayers();
        MapLayer objectLayer1 = layers.get("Слой объектов 1");
        MapObjects mapObjects = objectLayer1.getObjects();
        for (MapObject mapObject : mapObjects) {
            MapProperties objectProperties = mapObject.getProperties();
            x1 = (Float)objectProperties.get("x");
            y1 = (Float)objectProperties.get("y");
            width1 = (Float)objectProperties.get("width");
            height1 = (Float)objectProperties.get("height");
            solidActor = new SolidActor(x1,y1,width1,height1);
            stage.addActor(solidActor);
        }
        objectLayer2 = layers.get("Слой объектов 2");
        mapObjects2 = objectLayer2.getObjects();
        for (MapObject mapObject2 : mapObjects2) {
            MapProperties objectProperties2 = mapObject2.getProperties();
            x2 = (Float)objectProperties2.get("x");
            y2 = (Float)objectProperties2.get("y");
            width2 = (Float)objectProperties2.get("width");
            height2 = (Float)objectProperties2.get("height");
            OneWayPlatform oneWayPlatform = new OneWayPlatform(x2, y2, width2, height2);
            stage.addActor(oneWayPlatform);
        }

        Gdx.input.setInputProcessor(UIStage);
        orthographicCamera = (OrthographicCamera) stage.getCamera();
        tiledMapRenderer.setView(orthographicCamera);

        UIStage.addActor(coinLabel);
//        UIStage.addActor(timeLabel);
        UIStage.addActor(levelLabel);
        UIStage.addActor(healthLabel);

        createCoins();
        ((OrthographicCamera) stage.getCamera()).zoom += 0.5f;

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.CLEAR);
        scrollX += 20 * delta;

        if(!MyGame.isMultiPlayer){
            player.handleInput();
        }

        if(MyGame.isMultiPlayer) {
            if (isServer || isClient) {
                createServerBut.setVisible(false);
                createClientBut.setVisible(false);
            }

            if (isServer) {
                // Отправляем свои координаты
                server.updateServerPlayer(player.getX(), player.getY(), player.getSkin(), player.getFacingRight());
                MyRequest request = server.getRequest();
                player.handleInput();
                if (request != null) {
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
        }

        if(deathCount>1){
            levelTransition.startFade(() -> {
                game.setScreen(new Level3Screen(game));
                player.setPosition(100,1000);
                music.dispose();
            });
        }

        if(player.getY()<-256){
            player.die();
            deathCount++;
        }
        if(player2.getY()<-256){
            player2.die();
            deathCount++;
        }
        if(Player.isScreenShaking){
            shakeCamera(5,0.3f);
            Player.isScreenShaking = false;
        }
        // Проверка перехода на уровень 4
        boolean player1AtExit = player.getX() > 400 && player.getY() >= 2656;
        boolean player2AtExit = player2.getX() > 400 && player2.getY() >= 2656;

        if (MyGame.isMultiPlayer) {
            if ((player1AtExit) || (player2AtExit)) {
                levelTransition.startFade(() -> {
                    game.setScreen(new Level4Screen(game));
                    MyGame.newScore = score;
                    MyGame.isLevel4Available = true;
                    music.dispose();
                });
            }
        } else {
            if (player1AtExit) {
                levelTransition.startFade(() -> {
                    game.setScreen(new Level4Screen(game));
                    MyGame.newScore = score;
                    MyGame.isLevel4Available = true;
                    music.dispose();
                });
            }
        }


        if(isServer){
            healthLabel.setText("Health:" + player.getCurrentHealth());
        }else if(isClient){
            healthLabel.setText("Health:" + player2.getCurrentHealth());
        }

//        if (timerRunning) {elapsedTime += Gdx.graphics.getDeltaTime();}
//        int minutes = (int) (elapsedTime / 60);
//        int seconds = (int) (elapsedTime % 60);
//        int milliseconds = (int) ((elapsedTime * 100) % 100);
//        timeString = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds);
//        timeLabel.setText(timeString);
//        MyGame.elapsedTime = elapsedTime;

        updateCamera(stage.getCamera());
        Matrix4 skyProjection = stage.getCamera().combined.cpy();
        skyProjection.translate(-stage.getCamera().position.x * (1 - parallax), -stage.getCamera().position.y * (1 - parallax), 0);

        skyStage.act(delta);
        skyStage.draw();

        stage.act(delta);
        stage.draw();

        UIStage.act(delta);
        UIStage.draw();

        levelTransition.render(delta);
        if (levelTransition.isTransitionComplete()) {
            Gdx.input.setInputProcessor(stage);
        }
    }

    public void shakeCamera(float intensity, float duration) {
        cameraShake.shake(intensity, duration);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        UIViewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }
    private void createNetButtons(){
        TextureRegionDrawable csbDrUp = new TextureRegionDrawable(new Texture("buttons/player1Button.png"));
        TextureRegionDrawable csbDrDown = new TextureRegionDrawable(new Texture("buttons/player1Button.png"));
        createServerBut = new ImageButton(csbDrUp, csbDrDown);
        csbDrUp.setMinHeight(createServerBut.getHeight() * 3.8F*1.5f);
        csbDrUp.setMinWidth(createServerBut.getWidth() * 3.8F*1.5f);
        csbDrDown.setMinHeight(createServerBut.getHeight() * 3.6F*1.5f);
        csbDrDown.setMinWidth(createServerBut.getWidth() * 3.6F*1.5f);
        createServerBut.setSize(createServerBut.getWidth()* 4.8F*1.5f, createServerBut.getHeight() * 4.8F*1.5f);
        createServerBut.setPosition(stage.getViewport().getWorldWidth()-createServerBut.getWidth(), 300);
        createServerBut.setVisible(true);
        createServerBut.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(!isServer && !isClient) {
                    createServerBut.remove();
                    createClientBut.remove();
                    new Thread(() -> {
                        try {
                            MyGame.server = new MyServer(MyGame.responseFromServer);
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
        TextureRegionDrawable ccbDrUp = new TextureRegionDrawable(new Texture("buttons/joinButton.png"));
        TextureRegionDrawable ccbDrDown = new TextureRegionDrawable(new Texture("buttons/joinButton.png"));
        createClientBut = new ImageButton(ccbDrUp, ccbDrDown);
        createClientBut.setPosition(stage.getViewport().getWorldWidth()+createServerBut.getWidth(), 500);
        ccbDrUp.setMinHeight(createClientBut.getHeight() * 3.8F*1.5f);
        ccbDrUp.setMinWidth(createClientBut.getWidth() * 3.8F*1.5f);
        ccbDrDown.setMinHeight(createClientBut.getHeight() * 3.6F*1.5f);
        ccbDrDown.setMinWidth(createClientBut.getWidth() * 3.6F*1.5f);
        createClientBut.setSize(createClientBut.getWidth()* 4.8F*1.5f, createClientBut.getHeight() * 4.8F*1.5f);
        createClientBut.setPosition(stage.getViewport().getWorldWidth(), 300);
        createClientBut.setVisible(true);
        createClientBut.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(!isServer && !isClient) {
                    createServerBut.remove();
                    createClientBut.remove();
                    new Thread(() -> {
                        try {
                            MyGame.client = new MyClient(MyGame.requestFromClient);
                            Gdx.app.postRunnable(() -> {
                                if(MyGame.client.isCantConnected) {
                                    MyGame.client = null;
                                    MyGame.ipAddressOfServer = "Server not found";
                                } else {
                                    MyGame.isClient = true;
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
        UIStage.addActor(createServerBut);
        UIStage.addActor(createClientBut);
    }

    private void updateCamera(Camera camera){
        if(MyGame.isMultiPlayer) {
            if (isServer) {
                playerX = player.getX() + player.getWidth() / 2;
                playerY = player.getY() + player.getWidth() / 2;
                cameraShake.update(Gdx.graphics.getDeltaTime());
                targetPosition = new Vector3();
                targetPosition.set(MathUtils.clamp(500, SCREEN_WIDTH / 4F, maxMapSize - SCREEN_WIDTH / 4F), playerY + 100, 0);
                camera.position.y = camera.position.y - 10;
                camera.position.lerp(targetPosition, lerpSpeed);
                tiledMapRenderer.setView((OrthographicCamera) camera);
            }
            if (isClient) {
                playerX2 = player2.getX() + player2.getWidth() / 2;
                playerY2 = player2.getY() + player2.getWidth() / 2;
                cameraShake.update(Gdx.graphics.getDeltaTime());
                targetPosition = new Vector3();
                targetPosition.set(MathUtils.clamp(500, SCREEN_WIDTH / 4F, maxMapSize - SCREEN_WIDTH / 4F), playerY2 + 100, 0);
                camera.position.y = camera.position.y - 10;
                camera.position.lerp(targetPosition, lerpSpeed);
                tiledMapRenderer.setView((OrthographicCamera) camera);
            }
        }else{
            playerX = player.getX() + player.getWidth() / 2;
            playerY = player.getY() + player.getWidth() / 2;
            cameraShake.update(Gdx.graphics.getDeltaTime());
            targetPosition = new Vector3();
            targetPosition.set(MathUtils.clamp(500, SCREEN_WIDTH / 4F, maxMapSize - SCREEN_WIDTH / 4F), playerY + 100, 0);
            camera.position.y = camera.position.y - 10;
            camera.position.lerp(targetPosition, lerpSpeed);
            tiledMapRenderer.setView((OrthographicCamera) camera);
        }
    }
    private void createCoins(){
        coinTexture = new Texture("spinning coin_0.png");
        coins = new ArrayList<>();
        coins.add(new Coin(400, 256, coinTexture));
        coins.add(new Coin(500, 372, coinTexture));
        coins.add(new Coin(443, 468, coinTexture));
        coins.add(new Coin(533, 564, coinTexture));
        coins.add(new Coin(441, 660, coinTexture));
        coins.add(new Coin(373, 756, coinTexture));
        coins.add(new Coin(470, 852, coinTexture));
        coins.add(new Coin(568, 948, coinTexture));
        coins.add(new Coin(471, 1044, coinTexture));
        coins.add(new Coin(568, 1108, coinTexture));
        coins.add(new Coin(629, 1204, coinTexture));
        coins.add(new Coin(531, 1300, coinTexture));
        coins.add(new Coin(631, 1396, coinTexture));
        coins.add(new Coin(693, 1492, coinTexture));
        coins.add(new Coin(598, 1588, coinTexture));
        coins.add(new Coin(504, 1684, coinTexture));
        coins.add(new Coin(591, 1780, coinTexture));
        coins.add(new Coin(498, 1844, coinTexture));
        coins.add(new Coin(407, 1940, coinTexture));
        coins.add(new Coin(496, 2036, coinTexture));
        coins.add(new Coin(561, 2132, coinTexture));
        coins.add(new Coin(485, 2228, coinTexture));
        coins.add(new Coin(371, 2324, coinTexture));
        coins.add(new Coin(470, 2420, coinTexture));
        coins.add(new Coin(560, 2516, coinTexture));
        coins.add(new Coin(499, 2612, coinTexture));

//РАССТОЯНИЕ МЕЖДУ МОНЕТАМИ == 30-40
//ВЫСОТА МОНЕТЫ ОТ ПОЛА == 20

        for (Coin coin : coins) {
            stage.addActor(coin);
        }
    };


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
        stage.dispose();
        UIStage.dispose();
        skyStage.dispose();

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
            float offset = scrollX % layerWidth;

            batch.draw(texture, -offset, 0, layerWidth, layerHeight, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
            batch.draw(texture, -offset + layerWidth, 0, layerWidth, layerHeight, 0, 0, texture.getWidth(), texture.getHeight(), false, false);

            if (layerWidth < SCREEN_WIDTH) {
                float additionalOffset = -offset + 2 * layerWidth;
                batch.draw(texture, additionalOffset, 0, layerWidth, layerHeight, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
            }
        }
    }
}
