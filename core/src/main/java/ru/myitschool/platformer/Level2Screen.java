package ru.myitschool.platformer;

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

import java.util.ArrayList;
import java.util.List;

public class Level2Screen implements Screen{
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

    private int minX1 = 690;
    private int maxX1 = 850;
    private int minX2 = 2883;
    private int maxX2 = 3173;
    private int minX3 = 3795;
    private int maxX3 = 3877;

    private boolean movingRight = false;

    private float SLIME_SPEED = 100;
    private Texture slimeTexture;
    private SlimeEnemy slime;
    private SlimeEnemy slime2;
    private SlimeEnemy slime3;
    private TextureRegion slimeTextureRegion;

    private Sound deathSound;
    private Music music;

    private static float elapsedTime = MyGame.getElapsedTime();
    private boolean timerRunning;
    private Label timeLabel;
    private Label levelLabel;
    private Label healthLabel;
    private String timeString;
    private LevelTransition levelTransition;
    private ImageButton volumeButton;
    private ImageButton settingsButton;
    private ImageButton backToMenuButton;
    private boolean isVolumeButtonHere;
    private boolean isMusicPlaying;
    private CameraShake cameraShake;


    public Level2Screen(Game game) {
        this.game = game;
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

        Level2Screen.Sky sky = new Level2Screen.Sky(skyTexture, SCREEN_WIDTH, SCREEN_HEIGHT);
        sky.setZIndex(0);
        skyStage.addActor(sky);

        deathSound = Gdx.audio.newSound(Gdx.files.internal("sound/death.mp3"));
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
        Label coinLabel = new Label("Coins: ", labelStyle);
        coinLabel.setY(625);
        coinLabel.setX(50);

        timerRunning = true;
        timeLabel = new Label(timeString,labelStyle);
        timeLabel.setPosition(1025, 657);

        levelLabel = new Label("Level 2", labelStyle);
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

        Texture playerTexture = new Texture("my/frog/FrogIdle_0.png");
        player = new Player(playerTexture, leftButton, rightButton, upButton, playerX, playerY, coinLabel, 3);
        player.setPosition(100, 600);
//        player.setPosition(4350, 1000);
        stage.addActor(player);
        player.setZIndex(2);



        slimeTexture = new Texture("slime_walk_0.png");
        slimeTextureRegion = new TextureRegion(slimeTexture);
        slime = new SlimeEnemy(slimeTexture, 718, 377, false, minX1, maxX1);
        slime2 = new SlimeEnemy(slimeTexture, 2883, 505, false, minX2, maxX2 );

        stage.addActor(slime);
        stage.addActor(slime2);



        TmxMapLoader tml = new TmxMapLoader();
        TiledMap tiledMap = tml.load("maps/level2.tmx");
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


        createCoins();



    }

    public void shakeCamera(float intensity, float duration) {
        cameraShake.shake(intensity, duration);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.CLEAR);
        scrollX += 20 * delta;

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
        int minutes = (int) (elapsedTime / 60);
        int seconds = (int) (elapsedTime % 60);
        int milliseconds = (int) ((elapsedTime * 100) % 100);
        timeString = String.format("%02d:%02d:%02d", minutes, seconds, milliseconds);
        timeLabel.setText(timeString);
        MyGame.elapsedTime = elapsedTime;

        updateCamera(stage.getCamera());
        Matrix4 skyProjection = stage.getCamera().combined.cpy();
        skyProjection.translate(-stage.getCamera().position.x * (1 - parallax), -stage.getCamera().position.y * (1 - parallax), 0);

        if(player.getY()<100){
            player.die();
        }
        if(playerX>4380 && playerY < 150){
            levelTransition.startFade(() -> {
                game.setScreen(new Level3Screen(game));
                MyGame.newScore = score;
                MyGame.isLevel3Available = true;
                music.dispose();
            });
            Player.JUMP = 650;
        }



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
    private void updateCamera(Camera camera){
        playerX = player.getX() + player.getWidth()/2;
        playerY = player.getY() + player.getWidth()/2;
        cameraShake.update(Gdx.graphics.getDeltaTime());
        targetPosition = new Vector3();
        targetPosition.set(MathUtils.clamp(playerX, SCREEN_WIDTH/4F , maxMapSize - SCREEN_WIDTH/4F), playerY+100, 0);
        camera.position.y = camera.position.y-10;
        camera.position.lerp(targetPosition, lerpSpeed);
        tiledMapRenderer.setView((OrthographicCamera) camera);

    }
    private void createCoins(){
        coinTexture = new Texture("spinning coin_0.png");
        coins = new ArrayList<>();
//РАССТОЯНИЕ МЕЖДУ МОНЕТАМИ == 30-40
//ВЫСОТА МОНЕТЫ ОТ ПОЛА == 20
        coins.add(new Coin(320, 404, coinTexture));
        coins.add(new Coin(360, 404, coinTexture));
        coins.add(new Coin(400, 404, coinTexture));
        coins.add(new Coin(440, 404, coinTexture));
        coins.add(new Coin(480, 404, coinTexture));
        coins.add(new Coin(520, 404, coinTexture));

        coins.add(new Coin(670, 404, coinTexture));
        coins.add(new Coin(710, 404, coinTexture));
        coins.add(new Coin(750, 404, coinTexture));
        coins.add(new Coin(790, 404, coinTexture));
        coins.add(new Coin(830, 404, coinTexture));
        coins.add(new Coin(870, 404, coinTexture));
        coins.add(new Coin(1020, 404, coinTexture));
        coins.add(new Coin(1060, 404, coinTexture));
        coins.add(new Coin(1100, 404, coinTexture));
        coins.add(new Coin(1140, 404, coinTexture));

        coins.add(new Coin(2495, 564, coinTexture));
        coins.add(new Coin(2535, 564, coinTexture));
        coins.add(new Coin(2575, 564, coinTexture));

        coins.add(new Coin(2678, 564+32, coinTexture));
        coins.add(new Coin(2678+35, 564+32, coinTexture));
        coins.add(new Coin(2678+70, 564+32, coinTexture));

        coins.add(new Coin(2869, 532, coinTexture));
        coins.add(new Coin(2904, 532, coinTexture));
        coins.add(new Coin(2939, 532, coinTexture));
        coins.add(new Coin(2974, 532, coinTexture));
        coins.add(new Coin(3009, 532, coinTexture));
        coins.add(new Coin(3009+35, 532, coinTexture));
        coins.add(new Coin(3009+70, 532, coinTexture));
        coins.add(new Coin(3009+105, 532, coinTexture));
        coins.add(new Coin(3009+140, 532, coinTexture));
        coins.add(new Coin(3009+175, 532, coinTexture));


        coins.add(new Coin(3712+80, 404, coinTexture));
        coins.add(new Coin(3712+120, 404, coinTexture));
        coins.add(new Coin(3712+160, 404, coinTexture));

        //ниже - стрелка из монет
        //формат: X стрелки + n, Y стрелки + m
        coins.add(new Coin(4780, 350, coinTexture));
        coins.add(new Coin(4780+20, 350, coinTexture));
        coins.add(new Coin(4780, 350-20, coinTexture));
        coins.add(new Coin(4780+20, 350-20, coinTexture));
        coins.add(new Coin(4780, 350-40, coinTexture));
        coins.add(new Coin(4780+20, 350-40, coinTexture));
        coins.add(new Coin(4780, 350-60, coinTexture));
        coins.add(new Coin(4780+20, 350-60, coinTexture));
        coins.add(new Coin(4780, 350-80, coinTexture));
        coins.add(new Coin(4780+20, 350-80, coinTexture));
        coins.add(new Coin(4780-20, 350-80, coinTexture));
        coins.add(new Coin(4780+40, 350-80, coinTexture));
        coins.add(new Coin(4780, 350-100, coinTexture));
        coins.add(new Coin(4780+20, 350-100, coinTexture));

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
