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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

public class Level4Screen implements Screen{
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
    //коорды для слизней
    private int minX1 = 1016;
    private int maxX1 = 1207;
    private int minX2 = 1932;
    private int maxX2 = 2097;


    private boolean movingRight = false;

    private float SLIME_SPEED = 100;
    private Texture slimeTexture;
    private SlimeEnemy slime;
    private SlimeEnemy slime2;
    private SlimeEnemy slime3;
    private TextureRegion slimeTextureRegion;

    private Sound deathSound;
    private Sound clickSound;
    private Sound winSound;
    private Music music;

    private static float elapsedTime = MyGame.getElapsedTime();
    private boolean timerRunning;
    private boolean gameFinished = false;
    private Label timeLabel;
    private Label levelLabel;
    private Label healthLabel;
    private String timeString;
    private LevelTransition levelTransition;
    private ImageButton volumeButton;
    private ImageButton settingsButton;
    private ImageButton backToMenuButton;
    private ImageButton finishButton;
    private ImageButton backButton;
    private boolean isVolumeButtonHere;
    private boolean isMusicPlaying;
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


    public Level4Screen(Game game) {
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

        Level4Screen.Sky sky = new Level4Screen.Sky(skyTexture, SCREEN_WIDTH, SCREEN_HEIGHT);
        sky.setZIndex(0);
        skyStage.addActor(sky);

        deathSound = Gdx.audio.newSound(Gdx.files.internal("sound/death.mp3"));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("sound/clickSound.wav"));
        winSound = Gdx.audio.newSound(Gdx.files.internal("sound/winSound.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("sound/CantStopMyFeet.mp3"));
        music.setLooping(true);
        music.setVolume(0.04F);
        music.play();
        isMusicPlaying = true;

        cameraShake = new CameraShake((OrthographicCamera) stage.getCamera());

        Gdx.input.setInputProcessor(stage);
        Gdx.input.setInputProcessor(skyStage);

        FileHandle fontFile = Gdx.files.internal("my/DynaPuff.ttf");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = 50;
        BitmapFont bitmapFont = generator.generateFont(params);
        Label.LabelStyle labelStyle = new Label.LabelStyle(bitmapFont, Color.WHITE);
        coinLabel = new Label("Coins: ", labelStyle);
        coinLabel.setPosition(50,625);

        timerRunning = true;
        timeLabel = new Label(timeString,labelStyle);
        timeLabel.setPosition(1025, 657);

        levelLabel = new Label("Level 4", labelStyle);
        levelLabel.setPosition(560,625);

        healthLabel = new Label("Health:3", labelStyle);
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
                boolean isBackToMenuButtonHere = false;
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

        TextureRegionDrawable finishButUp = new TextureRegionDrawable(new Texture("buttons/FinishButton.png"));
        TextureRegionDrawable finishButDown = new TextureRegionDrawable(new Texture("buttons/FinishButton.png"));
        finishButton = new ImageButton(finishButUp, finishButDown);
        finishButUp.setMinHeight(finishButton.getHeight() * 4F);
        finishButUp.setMinWidth(finishButton.getWidth() * 4F);
        finishButDown.setMinHeight(finishButton.getHeight() * 4F);
        finishButDown.setMinWidth(finishButton.getWidth() * 4F);
        finishButton.setSize(finishButton.getWidth()* 4F, finishButton.getHeight() * 4F);
        finishButton.setPosition(SCREEN_WIDTH/2F-finishButton.getWidth()/2, SCREEN_HEIGHT/2F-finishButton.getHeight()/2);
        finishButton.setVisible(false);
        finishButton.setZIndex(2);
        UIStage.addActor(finishButton);

        TextureRegionDrawable backButUp = new TextureRegionDrawable(new Texture("buttons/backButton.png"));
        TextureRegionDrawable backButDown = new TextureRegionDrawable(new Texture("buttons/backButton.png"));
        backButton = new ImageButton(backButUp,backButDown);
        backButton.setPosition(finishButton.getX()+400, 70);
        backButUp.setMinHeight(backButton.getHeight() * 3.8F*1.5f);
        backButUp.setMinWidth(backButton.getWidth() * 3.8F*1.5f);
        backButDown.setMinHeight(backButton.getHeight() * 3.6F*1.5f);
        backButDown.setMinWidth(backButton.getWidth() * 3.6F*1.5f);
        backButton.setSize(backButton.getWidth()* 4.8F*1.5f, backButton.getHeight() * 4.8F*1.5f);
        backButton.setVisible(false);
        backButton.setZIndex(5);
        backButton.addListener(new ChangeListener(){
            public void changed(ChangeEvent event, Actor actor){
                clickSound.play(0.4f);
                levelTransition.startFade(() -> {
                    game.setScreen(new MenuScreen(game));
                });

            }
        });


        Texture playerTexture = new Texture("my/frog/FrogIdle_0.png");
        player = new Player(playerTexture, leftButton, rightButton, upButton, playerX, playerY, coinLabel, 3);
        player.setPosition(30, 600);
        Player.JUMP = 652;
        stage.addActor(player);
        player2 = new Player(playerTexture, leftButton, rightButton, upButton, playerX2, playerY2, coinLabel, 3);
        if(MyGame.isMultiPlayer && (isServer || isClient)) {
            stage.addActor(player2);
        }

        player.setZIndex(2);


        slimeTexture = new Texture("slime_walk_0.png");
        slimeTextureRegion = new TextureRegion(slimeTexture);
        slime = new SlimeEnemy(slimeTexture, minX1, 185, false, minX1, maxX1);
        slime2 = new SlimeEnemy(slimeTexture, minX2, 313, false, minX2, maxX2 );
        stage.addActor(slime);
        stage.addActor(slime2);

        TmxMapLoader tml = new TmxMapLoader();
        TiledMap tiledMap = tml.load("maps/level4.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        TileMapActor tileMapActor = new TileMapActor(tiledMap, (OrthogonalTiledMapRenderer) tiledMapRenderer);
        stage.addActor(tileMapActor);
        tileMapActor.setZIndex(1);

        maxMapSize = 160*32;
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

        Gdx.input.setInputProcessor(UIStage);
        orthographicCamera = (OrthographicCamera) stage.getCamera();
        tiledMapRenderer.setView(orthographicCamera);


        UIStage.addActor(coinLabel);
//        UIStage.addActor(timeLabel);
        UIStage.addActor(levelLabel);
        UIStage.addActor(healthLabel);
        UIStage.addActor(backButton);

        createCoins();

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

        skyStage.act(delta);
        skyStage.draw();

        stage.act(delta);
        stage.draw();

        UIStage.act(delta);
        UIStage.draw();

        if(isServer){
            healthLabel.setText("Health:" + player.getCurrentHealth());
        }else if(isClient){
            healthLabel.setText("Health:" + player2.getCurrentHealth());
        }
        levelTransition.render(delta);
        if (levelTransition.isTransitionComplete()) {
            Gdx.input.setInputProcessor(stage);
        }
        if(Player.isScreenShaking){
            shakeCamera(5,0.3f);
            Player.isScreenShaking = false;
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

        if(deathCount>1){
            levelTransition.startFade(() -> {
                game.setScreen(new Level4Screen(game));
                player.setPosition(100,1000);
                music.dispose();
            });
        }

        if(player.getY()<-256 && player.getX()<140*32){
            player.die();
            deathCount++;
        }
        if(player2.getY()<-256 && player2.getX()<140*32 ){
            player2.die();
            deathCount++;
        }

        // Проверка условия завершения игры
        boolean player1Finished = player.getX() > 140*32 && player.getY() < -100;
        boolean player2Finished = player2.getX() > 140*32 && player2.getY() < -100;

        if (!gameFinished) {
            if (MyGame.isMultiPlayer) {
                if ((player1Finished) || (player2Finished)) {
                    finishGame();
                }
            } else {
                if (player1Finished) {
                    finishGame();
                }
            }
        }
    }
    private void finishGame(){
        gameFinished = true;
        music.dispose();
        player.remove();
        winSound.play();
        finishButton.setVisible(true);
        settingsButton.setVisible(false);
        upButton.setVisible(false);
        rightButton.setVisible(false);
        leftButton.setVisible(false);
        healthLabel.setVisible(false);
        levelLabel.setVisible(false);
        backButton.setVisible(true);
        coinLabel.setFontScale(2);
        coinLabel.setPosition(SCREEN_WIDTH/2F-coinLabel.getWidth()-50,450);
        MyGame.newScore = 0;
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

    public void shakeCamera(float intensity, float duration) {
        cameraShake.shake(intensity, duration);
    }
    private void updateCamera(Camera camera){
        if(MyGame.isMultiPlayer) {
            if (isServer) {
                playerX = player.getX() + player.getWidth() / 2;
                playerY = player.getY() + player.getWidth() / 2;
                cameraShake.update(Gdx.graphics.getDeltaTime());
                targetPosition = new Vector3();
                targetPosition.set(MathUtils.clamp(playerX, SCREEN_WIDTH / 4F, maxMapSize - SCREEN_WIDTH / 4F), playerY + 100, 0);
                camera.position.y = camera.position.y - 10;
                camera.position.lerp(targetPosition, lerpSpeed);
                tiledMapRenderer.setView((OrthographicCamera) camera);
            }
            if (isClient) {
                playerX2 = player2.getX() + player2.getWidth() / 2;
                playerY2 = player2.getY() + player2.getWidth() / 2;
                cameraShake.update(Gdx.graphics.getDeltaTime());
                targetPosition = new Vector3();
                targetPosition.set(MathUtils.clamp(playerX2, SCREEN_WIDTH / 4F, maxMapSize - SCREEN_WIDTH / 4F), playerY2 + 100, 0);
                camera.position.y = camera.position.y - 10;
                camera.position.lerp(targetPosition, lerpSpeed);
                tiledMapRenderer.setView((OrthographicCamera) camera);
            }
        }else{
            playerX = player.getX() + player.getWidth() / 2;
            playerY = player.getY() + player.getWidth() / 2;
            cameraShake.update(Gdx.graphics.getDeltaTime());
            targetPosition = new Vector3();
            targetPosition.set(MathUtils.clamp(playerX, SCREEN_WIDTH / 4F, maxMapSize - SCREEN_WIDTH / 4F), playerY + 100, 0);
            camera.position.y = camera.position.y - 10;
            camera.position.lerp(targetPosition, lerpSpeed);
            tiledMapRenderer.setView((OrthographicCamera) camera);
        }
    }
    private void createCoins(){
        coinTexture = new Texture("spinning coin_0.png");
        coins = new ArrayList<>();
        coins.add(new Coin(229, 404,coinTexture));
        coins.add(new Coin(229+35, 404,coinTexture));
        coins.add(new Coin(229+35+35, 404,coinTexture));
        coins.add(new Coin(229+35+35+35, 404,coinTexture));
        coins.add(new Coin(229+35+35+35+35, 404,coinTexture));
        coins.add(new Coin(229+35+35+35+35+35, 404,coinTexture));

        coins.add(new Coin(528, 340,coinTexture));

        coins.add(new Coin(576, 276,coinTexture));
        coins.add(new Coin(576+35, 276,coinTexture));
        coins.add(new Coin(576+35+35, 276,coinTexture));


        coins.add(new Coin(690, 212,coinTexture));
        coins.add(new Coin(690+35, 212,coinTexture));
        coins.add(new Coin(690+35+35, 212,coinTexture));
        coins.add(new Coin(690+35+35+35, 212,coinTexture));
        coins.add(new Coin(690+35+35+35+35, 212,coinTexture));
        coins.add(new Coin(690+35+35+35+35+35, 212,coinTexture));


        coins.add(new Coin(997, 212,coinTexture));
        coins.add(new Coin(997+35, 212,coinTexture));
        coins.add(new Coin(997+35+35, 212,coinTexture));
        coins.add(new Coin(997+35+35+35, 212,coinTexture));
        coins.add(new Coin(997+35+35+35+35, 212,coinTexture));
        coins.add(new Coin(997+35+35+35+35+35, 212,coinTexture));
        coins.add(new Coin(997+35+35+35+35+35+35, 212,coinTexture));

        coins.add(new Coin(1876, 320+20,coinTexture));
        coins.add(new Coin(1876+35, 320+20,coinTexture));
        coins.add(new Coin(1876+35+35, 320+20,coinTexture));
        coins.add(new Coin(1876+35+35+35, 320+20,coinTexture));
        coins.add(new Coin(1876+35+35+35+35, 320+20,coinTexture));
        coins.add(new Coin(1876+35+35+35+35+35, 320+20,coinTexture));
        coins.add(new Coin(1876+35+35+35+35+35+35, 320+20,coinTexture));

        coins.add(new Coin(334, 628,coinTexture));
        coins.add(new Coin(334+35, 628,coinTexture));
        coins.add(new Coin(334+35+35, 628,coinTexture));

        coins.add(new Coin(517, 660,coinTexture));
        coins.add(new Coin(517+35, 660,coinTexture));
        coins.add(new Coin(517+35+35, 660,coinTexture));
        coins.add(new Coin(517+35+35+35, 660,coinTexture));

        coins.add(new Coin(756, 628,coinTexture));
        coins.add(new Coin(756+35, 628,coinTexture));
        coins.add(new Coin(756+35+35, 628,coinTexture));
//РАССТОЯНИЕ МЕЖДУ МОНЕТАМИ == 30-40
//ВЫСОТА МОНЕТЫ ОТ ПОЛА == 20

        for (Coin coin : coins) {
            stage.addActor(coin);
        }
    }
    public static float getElapsedTime(){
        return elapsedTime;
    }

    @Override
    public void dispose() {

    }
    class Sky extends Actor {
        private Texture texture;
        private float layerWidth, layerHeight;
        private float parallaxSpeed;

        public Sky(Texture texture, float layerWidth, float layerHeight) {
            this.texture = texture;
            texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            this.layerWidth = SCREEN_WIDTH;
            this.layerHeight = SCREEN_HEIGHT;
            this.parallaxSpeed = parallaxSpeed;
            setBounds(0, 0, layerWidth, layerHeight);


//            Texture farMountainsTexture = new Texture("far_mountains.png");
//            Texture farForestTexture = new Texture("far-forest.png");
//            Texture forestTexture = new Texture("forest.png");
//
//            Sky farMountains = new Sky(farMountainsTexture, 1600, 480, 0.1f);
//            Sky farForest = new Sky(farForestTexture, 1600, 480, 0.1f);
//            Sky forest = new Sky(forestTexture,800, 480, 0.3f);
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
