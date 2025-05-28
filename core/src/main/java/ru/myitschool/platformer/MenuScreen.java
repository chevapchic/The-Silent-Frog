package ru.myitschool.platformer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import static ru.myitschool.platformer.MyGame.SCREEN_HEIGHT;
import static ru.myitschool.platformer.MyGame.SCREEN_WIDTH;
import static ru.myitschool.platformer.MyGame.isLevel2Available;
import static ru.myitschool.platformer.MyGame.isLevel3Available;
import static ru.myitschool.platformer.MyGame.isLevel4Available;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class MenuScreen implements Screen{
    private Game game;
    private ImageButton playButton;
    private ImageButton quitButton;
    private ImageButton L1Button;
    public  ImageButton L2Button;
    private ImageButton L3Button;
    private ImageButton L4Button;
    private ImageButton aboutButton;
    private ImageButton frogButton;
    private ImageButton mdButton;


    private Stage stage;
    private FitViewport viewport;
    private Music music;
    private Texture skyTexture;
    private float parallax = 0.5F;
    private float scrollX = 0;
    private Stage skyStage;
    private FitViewport skyViewport;
    private Label title;
    private Label aboutLabel;
    private LevelTransition levelTransition;
    private Sound clickSound;
    public static int skin;
    public static int skin1;
    private TextureRegionDrawable L2ButAvailUp;
    private TextureRegionDrawable L2ButAvailDown;
    private TextureRegionDrawable L3ButAvailUp;
    private TextureRegionDrawable L3ButAvailDown;
    private TextureRegionDrawable L4ButAvailUp;
    private TextureRegionDrawable L4ButAvailDown;


    //сеть

    Vector3 touch;

    public MenuScreen(Game game) {
        this.game = game;
        levelTransition = new LevelTransition();
    }
    @Override
    public void show() {



        viewport = new FitViewport(SCREEN_WIDTH/2F, SCREEN_HEIGHT/2F);
        skyViewport = new FitViewport(SCREEN_WIDTH/2F, SCREEN_HEIGHT/2F);
        stage = new Stage(viewport);

        Gdx.input.setInputProcessor(stage);


        clickSound = Gdx.audio.newSound(Gdx.files.internal("sound/clickSound.wav"));
        skyTexture = new Texture(Gdx.files.internal("SKY.png"));
        skyTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        TextureRegionDrawable L1ButUp = new TextureRegionDrawable(new Texture("buttons/Level1Button.png"));
        TextureRegionDrawable L1ButDown = new TextureRegionDrawable(new Texture("buttons/Level1Button.png"));
        L1Button = new ImageButton(L1ButUp, L1ButDown);
        L1ButUp.setMinHeight(L1Button.getHeight() * 2F);
        L1ButUp.setMinWidth(L1Button.getWidth() * 2F);
        L1ButDown.setMinHeight(L1Button.getHeight() * 1.9F);
        L1ButDown.setMinWidth(L1Button.getWidth() * 1.9F);
        L1Button.setSize(L1Button.getWidth()* 2F, L1Button.getHeight() * 2F);
        L1Button.setPosition(100+10, 200);
        L1Button.setVisible(false);
        L1Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play(0.4F);
                L1Button.setVisible(false);
                L2Button.setVisible(false);
                L3Button.setVisible(false);
                L4Button.setVisible(false);
                frogButton.setVisible(true);
                mdButton.setVisible(true);
                MyGame.newScore = 0;

            }
        });

        TextureRegionDrawable L2ButUp = new TextureRegionDrawable(new Texture("buttons/Level2ButtonLocked.png"));
        TextureRegionDrawable L2ButDown = new TextureRegionDrawable(new Texture("buttons/Level2ButtonLocked.png"));
        L2ButAvailUp = new TextureRegionDrawable(new Texture("buttons/Level2Button.png"));
        L2ButAvailDown = new TextureRegionDrawable(new Texture("buttons/Level2Button.png"));
        L2Button = new ImageButton(L2ButUp, L2ButDown);
        L2ButAvailUp.setMinHeight(L2Button.getHeight() * 2F);
        L2ButAvailUp.setMinWidth(L2Button.getWidth() * 2F);
        L2ButAvailDown.setMinHeight(L2Button.getHeight() * 1.9F);
        L2ButAvailDown.setMinWidth(L2Button.getWidth() * 1.9F);
        L2ButUp.setMinHeight(L2Button.getHeight() * 2F);
        L2ButUp.setMinWidth(L2Button.getWidth() * 2F);
        L2ButDown.setMinHeight(L2Button.getHeight() * 1.9F);
        L2ButDown.setMinWidth(L2Button.getWidth() * 1.9F);
        L2Button.setSize(L2Button.getWidth()* 2F, L2Button.getHeight() * 2F);
        L2Button.setPosition(250+10, 200);
        L2Button.setVisible(false);
        L2Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play(0.4F);
                if(MyGame.isLevel2Available) {
                    levelTransition.startFade(() -> {
                        game.setScreen(new Level2Screen(game));
                        music.dispose();
                    });
                }
            }
        });

        TextureRegionDrawable L3ButUp = new TextureRegionDrawable(new Texture("buttons/Level3ButtonLocked.png"));
        TextureRegionDrawable L3ButDown = new TextureRegionDrawable(new Texture("buttons/Level3ButtonLocked.png"));
        L3ButAvailUp = new TextureRegionDrawable(new Texture("buttons/Level3Button.png"));
        L3ButAvailDown = new TextureRegionDrawable(new Texture("buttons/Level3Button.png"));
        L3Button = new ImageButton(L3ButUp, L3ButDown);
        L3ButAvailUp.setMinHeight(L3Button.getHeight() * 2F);
        L3ButAvailUp.setMinWidth(L3Button.getWidth() * 2F);
        L3ButAvailDown.setMinHeight(L3Button.getHeight() * 1.9F);
        L3ButAvailDown.setMinWidth(L3Button.getWidth() * 1.9F);
        L3ButUp.setMinHeight(L3Button.getHeight() * 2F);
        L3ButUp.setMinWidth(L3Button.getWidth() * 2F);
        L3ButDown.setMinHeight(L3Button.getHeight() * 1.9F);
        L3ButDown.setMinWidth(L3Button.getWidth() * 1.9F);
        L3Button.setSize(L3Button.getWidth()* 2F, L3Button.getHeight() * 2F);
        L3Button.setPosition(400+10, 200);
        L3Button.setVisible(false);
        L3Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play(0.4F);
                if(isLevel3Available) {
                    levelTransition.startFade(() -> {
                        game.setScreen(new Level3Screen(game));
                        music.dispose();
                        Player.JUMP = 650;
                    });
                }
            }
        });

        TextureRegionDrawable L4ButUp = new TextureRegionDrawable(new Texture("buttons/Level4ButtonLocked.png"));
        TextureRegionDrawable L4ButDown = new TextureRegionDrawable(new Texture("buttons/Level4ButtonLocked.png"));
        L4ButAvailUp = new TextureRegionDrawable(new Texture("buttons/Level4Button.png"));
        L4ButAvailDown = new TextureRegionDrawable(new Texture("buttons/Level4Button.png"));
        L4Button = new ImageButton(L4ButUp, L4ButDown);
        L4ButAvailUp.setMinHeight(L4Button.getHeight() * 2F);
        L4ButAvailUp.setMinWidth(L4Button.getWidth() * 2F);
        L4ButAvailDown.setMinHeight(L4Button.getHeight() * 1.9F);
        L4ButAvailDown.setMinWidth(L4Button.getWidth() * 1.9F);
        L4ButUp.setMinHeight(L4Button.getHeight() * 2F);
        L4ButUp.setMinWidth(L4Button.getWidth() * 2F);
        L4ButDown.setMinHeight(L4Button.getHeight() * 1.9F);
        L4ButDown.setMinWidth(L4Button.getWidth() * 1.9F);
        L4Button.setSize(L4Button.getWidth()* 2F, L4Button.getHeight() * 2F);
        L4Button.setPosition(100+10, 120);
        L4Button.setVisible(false);
        L4Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play(0.4F);
                if(isLevel4Available) {
                    levelTransition.startFade(() -> {
                        game.setScreen(new Level4Screen(game));
                        music.dispose();
                    });
                }
            }
        });

        TextureRegionDrawable playButUp = new TextureRegionDrawable(new Texture("buttons/playButton.png"));
        TextureRegionDrawable playButDown = new TextureRegionDrawable(new Texture("buttons/playButton.png"));
        playButton = new ImageButton(playButUp, playButDown);
        playButUp.setMinHeight(playButton.getHeight() * 2F);
        playButUp.setMinWidth(playButton.getWidth() *2F);
        playButDown.setMinHeight(playButton.getHeight() *1.9F);
        playButDown.setMinWidth(playButton.getWidth() * 1.9F);
        playButton.setSize(playButton.getWidth()* 2F, playButton.getHeight() * 2F);
        playButton.setPosition(250, 150);
        playButton.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play(0.4F);
                playButton.setVisible(false);
                title.setVisible(false);
                L1Button.setVisible(true);
                L2Button.setVisible(true);
                L3Button.setVisible(true);
                L4Button.setVisible(true);
                aboutButton.setVisible(false);
            }
        });

        TextureRegionDrawable aboutButUp = new TextureRegionDrawable(new Texture("buttons/AboutButton.png"));
        TextureRegionDrawable aboutButDown = new TextureRegionDrawable(new Texture("buttons/AboutButton.png"));
        aboutButton = new ImageButton(aboutButUp, aboutButDown);
        aboutButUp.setMinHeight(aboutButton.getHeight() * 2F);
        aboutButUp.setMinWidth(aboutButton.getWidth() * 2F);
        aboutButDown.setMinHeight(aboutButton.getHeight() * 1.9F);
        aboutButDown.setMinWidth(aboutButton.getWidth() * 1.9F);
        aboutButton.setSize(aboutButton.getWidth()* 2F, aboutButton.getHeight() * 2F);
        aboutButton.setPosition(250, 83);
        aboutButton.addListener(new ChangeListener() {
            boolean isAboutMenuActive = false;
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(!isAboutMenuActive){
                    clickSound.play(0.4F);
                    playButton.setVisible(false);
                    title.setVisible(false);
                    L1Button.setVisible(false);
                    L2Button.setVisible(false);
                    L3Button.setVisible(false);
                    L4Button.setVisible(false);
                    quitButton.setVisible(false);
                    isAboutMenuActive =true;
                    aboutLabel.setVisible(true);
                    aboutButton.setPosition(250,30);
                }
                else{
                    clickSound.play(0.4F);
                    playButton.setVisible(true);
                    title.setVisible(true);
                    quitButton.setVisible(true);
                    isAboutMenuActive = false;
                    aboutLabel.setVisible(false);
                    aboutButton.setPosition(250,83);
                }
            }
        });

        TextureRegionDrawable quitButUp = new TextureRegionDrawable(new Texture("buttons/exitButton.png"));
        TextureRegionDrawable quitButDown = new TextureRegionDrawable(new Texture("buttons/exitButton.png"));
        quitButton = new ImageButton(quitButUp, quitButDown);
        quitButUp.setMinHeight(quitButton.getHeight() * 2F);
        quitButUp.setMinWidth(quitButton.getWidth() * 2F);
        quitButDown.setMinHeight(quitButton.getHeight() * 1.9F);
        quitButDown.setMinWidth(quitButton.getWidth() * 1.9F);
        quitButton.setSize(quitButton.getWidth()* 2F, quitButton.getHeight() * 2F);
        quitButton.setPosition(260, 30);

        TextureRegionDrawable frogButUp = new TextureRegionDrawable(new Texture("buttons/frogButton.png"));
        TextureRegionDrawable frogButDown = new TextureRegionDrawable(new Texture("buttons/frogButton.png"));
        frogButton = new ImageButton(frogButUp, frogButDown);
        frogButUp.setMinHeight(frogButton.getHeight() * 2F);
        frogButUp.setMinWidth(frogButton.getWidth() * 2F);
        frogButDown.setMinHeight(frogButton.getHeight() * 1.9F);
        frogButDown.setMinWidth(frogButton.getWidth() * 1.9F);
        frogButton.setSize(frogButton.getWidth()*2F, frogButton.getHeight() *2F);
        frogButton.setPosition(L1Button.getX()+70, L1Button.getY()-15);
        frogButton.setVisible(false);
        frogButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                skin = 0;
                skin1 = 0;
                levelTransition.startFade(() -> {
                    game.setScreen(new LevelScreen(game));
                    music.dispose();
                });
                frogButton.setVisible(false);
                mdButton.setVisible(false);
            }
        });
        TextureRegionDrawable mdButUp = new TextureRegionDrawable(new Texture("buttons/maskDudeButton.png"));
        TextureRegionDrawable mdButDown = new TextureRegionDrawable(new Texture("buttons/maskDudeButton.png"));
        mdButton = new ImageButton(mdButUp, mdButDown);
        mdButUp.setMinHeight(mdButton.getHeight() * 2F);
        mdButUp.setMinWidth(mdButton.getWidth() * 2F);
        mdButDown.setMinHeight(mdButton.getHeight() * 1.9F);
        mdButDown.setMinWidth(mdButton.getWidth() * 1.9F);
        mdButton.setSize(mdButton.getWidth()*2F, mdButton.getHeight() *2F);
        mdButton.setPosition(frogButton.getX()+160, L1Button.getY()-15);
        mdButton.setVisible(false);
        mdButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                skin = 1;
                skin1 = 1;
                levelTransition.startFade(() -> {
                    game.setScreen(new LevelScreen(game));
                    music.dispose();
                });
                frogButton.setVisible(false);
                mdButton.setVisible(false);
            }
        });

        FileHandle fontFile = Gdx.files.internal("my/alagard-12px-unicode.ttf");
        FileHandle fontFile2 = Gdx.files.internal("my/DynaPuff.ttf");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator generator2 = new FreeTypeFontGenerator(fontFile2);
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        FreeTypeFontGenerator.FreeTypeFontParameter params2 = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = 70;
        params2.size = 25;
        BitmapFont bitmapFont = generator.generateFont(params);
        BitmapFont bitmapFont2 = generator2.generateFont(params2);
        Label.LabelStyle labelStyle = new Label.LabelStyle(bitmapFont, Color.WHITE);
        Label.LabelStyle labelStyle2 = new Label.LabelStyle(bitmapFont2, Color.WHITE);
        title = new Label("The Silent Frog", labelStyle);
        title.setPosition(60,260);

        aboutLabel = new Label("This game was created by a hardworking" +
            "\n ninth grader who wanted" +
            "to make\n a beautiful and aesthetic game. \n Enjoy it!", labelStyle2);
        aboutLabel.setPosition(60, 150);
        aboutLabel.setAlignment(Align.center);
        aboutLabel.setVisible(false);

        MenuScreen.Sky sky = new MenuScreen.Sky(skyTexture, SCREEN_WIDTH, SCREEN_HEIGHT);
        sky.setZIndex(0);
        skyStage = new Stage(skyViewport);
        skyStage.addActor(sky);

        stage.addActor(playButton);
        stage.addActor(quitButton);
        stage.addActor(title);
        stage.addActor(L1Button);
        stage.addActor(L2Button);
        stage.addActor(L3Button);
        stage.addActor(L4Button);
        stage.addActor(aboutButton);
        stage.addActor(aboutLabel);
        stage.addActor(frogButton);
        stage.addActor(mdButton);

        music = Gdx.audio.newMusic(Gdx.files.internal("sound/Menu.mp3"));
        music.setVolume(0.04F*2);
        music.setLooping(true);
        music.play();


    }

    @Override
    public void render(float delta) {
        Color color = new Color(0.3f,0.3f,0.7f,1F);
        ScreenUtils.clear(color);
        scrollX += 20 * delta;
        skyStage.act(delta);
        skyStage.draw();

        stage.act(delta);
        stage.draw();

        if(isLevel2Available){
            L2Button.getStyle().imageUp = L2ButAvailUp;
            L2Button.getStyle().imageDown = L2ButAvailDown;
        }
        if(isLevel3Available){
            L3Button.getStyle().imageUp = L3ButAvailUp;
            L3Button.getStyle().imageDown = L3ButAvailDown;
        }
        if(isLevel4Available){
            L4Button.getStyle().imageUp = L4ButAvailUp;
            L4Button.getStyle().imageDown = L4ButAvailDown;
        }
        levelTransition.render(delta);
        if (levelTransition.isTransitionComplete()) {
            Gdx.input.setInputProcessor(stage);
        }

        if(quitButton.isPressed()){
            stage.dispose();
            clickSound.play(0.4F);
        }


    }


    @Override
    public void resize(int width, int height) {

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

    @Override
    public void dispose() {

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

