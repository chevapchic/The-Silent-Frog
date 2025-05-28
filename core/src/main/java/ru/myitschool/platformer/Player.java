package ru.myitschool.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;


public class Player extends Actor {
    private  TextureRegion playerJumpTextureRegion;
    private TextureRegion playerIdleTextureRegion;
    private TextureRegion playerTextureRegion;
    private Texture playerTexture;
    public static float SPEED = 200;
    public static int JUMP = 552;
    private int maxJumps = 2;
    private int currentJumps;
    private static final float GRAVITY = -0.15F*15000*0.94F;
    private int maxHealth;
    private int currentHealth;
    private float timer = 2F;

    private float frogStateTime = 0;
    private float mdStateTime = 0;
    private float frogHitStateTime = 0;
    private float mdHitStateTime = 0;

    private boolean isStand = false;
    private boolean isMoving = false;
    private boolean facingRight = true;

    private Animation<TextureRegion> frogRunAnimation;
    private Animation<TextureRegion> frogIdleAnimation;
    private Animation<TextureRegion> frogHitAnimation;
    private Animation<TextureRegion> mdRunAnimation;
    private Animation<TextureRegion> mdIdleAnimation;
    private Animation<TextureRegion> mdHitAnimation;

    private Vector2 velocity = new Vector2();
    float playerX;
    float playerY;
    private Polygon hitBox;

    private ImageButton rightButton;
    private ImageButton leftButton;
    private ImageButton upButton;

    public static int score;
    private Label coinLabel;
    private Sound coinSound;

    private long lastCoinPickupTime = 0;
    private final long coinPickupInterval = 100;

    private int newScore;
    private Sound jumpSound;
    private Sound deathSound;

    private float jumpBufferTime;
    private float jumpBufferDuration = 0.15f;

    private boolean isInvincible;
    private float invincibilityTimer;
    private float invincibilityDuration = 1.0f;
    private boolean isDamagedTextureActive = false;
    public static boolean isScreenShaking = false;
    private int skin;//0 - frog, 1 - maskDude
    private boolean touchingGround;


    public Player(Texture texture, ImageButton leftButton, ImageButton rightButton, ImageButton upButton, float playerX, float playerY, Label CoinLabel, int maxHealth) {
        this.playerTextureRegion = new TextureRegion(texture);
        this.leftButton = leftButton;
        this.rightButton = rightButton;
        this.upButton = upButton;
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerTexture = texture;
        this.coinLabel = CoinLabel;
        this.newScore = MyGame.newScore;
        this.maxHealth = maxHealth;
        currentHealth = maxHealth;
        skin = MenuScreen.skin;
        currentJumps = maxJumps;

        jumpSound = Gdx.audio.newSound(Gdx.files.internal("sound/SFX_Jump_50.mp3"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("sound/death.mp3"));
        coinSound = Gdx.audio.newSound(Gdx.files.internal("my/coinSound.ogg"));

        playerJumpTextureRegion = new TextureRegion(new Texture("my/frog/FrogJump.png"));

        setSize(playerTexture.getWidth(), playerTexture.getHeight());

        float[] vertices = {
            0, 0,
            0, getHeight(),
            getWidth(), getHeight(),
            getWidth(), 0
        };
        hitBox = new Polygon(vertices);


        rightButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                velocity.x = SPEED;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                velocity.x = 0;
                super.touchUp(event, x, y, pointer, button);
            }
        });
        leftButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                velocity.x = -SPEED;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                velocity.x = 0;
                super.touchUp(event, x, y, pointer, button);
            }
        });
        upButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                jumpBufferTime = 0;

                return super.touchDown(event, x, y, pointer, button);
            }
        });


        frogIdleAnimation = createFrogIdleAnimation();
        frogRunAnimation = createFrogRunAnimation();
        frogHitAnimation = createFrogHitAnimation();

        mdIdleAnimation = createMdIdleAnimation();
        mdRunAnimation = createMdRunAnimation();
        mdHitAnimation = createMdHitAnimation();
    }


    @Override
    public void act(float delta) {
        super.act(delta);
        if (isInvincible) {
            invincibilityTimer -= delta;
            if (invincibilityTimer <= 0) {
                isInvincible = false;
            }
        }

//        healOverTime(delta);
//        System.out.println("-----------");
//        System.out.println(getX());
//        System.out.println(getY());
//        System.out.println("-----------");

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && isStand) {
            velocity.y = JUMP;
            float volume = 0.15F;
            jumpSound.play(volume);
        }
        if (jumpBufferTime < jumpBufferDuration) {
            jumpBufferTime += delta;
        }
        jump();
        timer += delta;
        velocity.y += GRAVITY*delta;
        moveBy(velocity.x*delta, velocity.y*delta);
        hitBox.setPosition(getX(), getY());
        frogStateTime += 1.2F* delta;
        mdStateTime += 1.2F* delta;
        frogHitStateTime += 1.2F* delta;
        mdHitStateTime += 1.2F* delta;
        checkOverlap();
        setPlayerTextureRegion();

    }
    public void takeDamage(int damage) {
        if (!isInvincible) {
            currentHealth -= damage;
            deathSound.play(0.4f);
            if (currentHealth < 1) {
                currentHealth = 0;
                die();
            }
            setHitTexture();
            setInvincible();
            float a = velocity.x;
            float b = velocity.y;
            velocity.x = 0;
            velocity.y = 0;
            Timer.schedule(new Timer.Task(){
                @Override
                public void run() {
                    velocity.x = a;
                    velocity.y = b;
                }
            }, 0.3f);
        }
    }
    private void jump() {
        if (isStand &&  (jumpBufferTime < jumpBufferDuration)) {
            velocity.y = JUMP;
            jumpSound.play(0.15F);
            jumpBufferTime = jumpBufferDuration;
        }
    }
//    private void healOverTime(float delta) {
//        float healPerSecond = 5f;
//        if (currentHealth < maxHealth) {
//            float healAmount = healPerSecond * delta;
//            currentHealth += healAmount;
//            if (currentHealth > maxHealth) {
//                currentHealth = maxHealth;
//            }
//        }
//    }


    private void setPlayerTextureRegion(){
        if(rightButton.isPressed()){
            facingRight = true;
            isMoving = true;
        }else if(leftButton.isPressed()){
            facingRight= false;
            isMoving = true;
        }else{isMoving = false;}
        if(!isDamagedTextureActive) {
            if (isMoving) {
                if(skin==0){playerTextureRegion = frogRunAnimation.getKeyFrame(frogStateTime, true);}
                else if(skin == 1){playerTextureRegion = mdRunAnimation.getKeyFrame(mdStateTime, true);}
                if (!facingRight && !playerTextureRegion.isFlipX()) {
                    playerTextureRegion.flip(true, false);
                    facingRight = false;
                } else if (facingRight && playerTextureRegion.isFlipX()) {
                    playerTextureRegion.flip(true, false);
                    facingRight = true;
                }
            }

            if (!isMoving && isStand) {
                if(skin==0){ playerTextureRegion = frogIdleAnimation.getKeyFrame(frogStateTime, true);}
                else if(skin == 1){playerTextureRegion = mdIdleAnimation.getKeyFrame(mdStateTime, true);}
                if (!facingRight && !playerTextureRegion.isFlipX()) {playerTextureRegion.flip(true, false);}
                else if (facingRight && playerTextureRegion.isFlipX()) {playerTextureRegion.flip(true, false);}
            }
        }


    }
    private void setHitTexture() {
        if(!isDamagedTextureActive) {
            isDamagedTextureActive = true;
            isScreenShaking = true;
            frogHitStateTime=0;
            mdHitStateTime=0;
            if(skin == 0) {playerTextureRegion = frogHitAnimation.getKeyFrame(frogHitStateTime, true);}
            else if(skin == 1){playerTextureRegion = mdHitAnimation.getKeyFrame(mdHitStateTime, true);}

            if (!facingRight && !playerTextureRegion.isFlipX()) {playerTextureRegion.flip(true, false);}
            else if (facingRight && playerTextureRegion.isFlipX()) {playerTextureRegion.flip(true, false);}

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    if(skin == 0){playerTextureRegion = frogRunAnimation.getKeyFrame(frogStateTime, false);}
                    else if(skin == 1){playerTextureRegion = mdRunAnimation.getKeyFrame(mdHitStateTime, false);}
                    isDamagedTextureActive = false;
                }
            }, 0.3f);

        }
    }
    private void setInvincible() {
        isInvincible = true;
        invincibilityTimer = invincibilityDuration;
    }
    public void setSkin(int skin){
        this.skin = skin;
    }
    public int getSkin() {
        return skin;
    }



    private void checkOverlap() {
        Stage stage = getStage();
        Array<Actor> actors = stage.getActors();
        touchingGround = false;
        for (int i = 0; i < actors.size; i++) {
            Actor actor = actors.get(i);
            if (actor instanceof SolidActor) {
                SolidActor solidActor = (SolidActor) actor;
                Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
                boolean isOverlap = Intersector.overlapConvexPolygons(hitBox, solidActor.getHitBox(), mtv);
                if (isOverlap && mtv.depth > 0.03f) {
                    float nextX = mtv.normal.x * mtv.depth;
                    float nextY = mtv.normal.y * mtv.depth;
                    if (actor instanceof OneWayPlatform) {
                        if (velocity.y > 0) {
                            continue;
                        }
                    }
                    moveBy(nextX, nextY);
                    if (mtv.normal.y > 0.2f) {
                        velocity.y = 0;
                        touchingGround = true;
                    }
                }
            }
            isStand = touchingGround;
            if (actor instanceof Coin) {
                Coin coin = (Coin) actor;
                Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
                boolean isOverlap = Intersector.overlapConvexPolygons(hitBox, coin.getHitBox(), mtv);
                if (isOverlap) {
                    coin.remove();
                    score++;
                    coinLabel.setText("Coins: " + score);
                    playCoinPickup();
                }
            }
            if (actor instanceof SlimeEnemy) {
                SlimeEnemy slimeEnemy = (SlimeEnemy) actor;
                Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
                boolean isOverlap = Intersector.overlapConvexPolygons(hitBox, slimeEnemy.getHitBox(), mtv);
                if (isOverlap) {
                    takeDamage(1);
                }
            }
            if(actor instanceof Trap){
                Trap trap = (Trap) actor;
                Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
                boolean isOverlap = Intersector.overlapConvexPolygons(hitBox, trap.getHitBox(), mtv);
                if (isOverlap) {
                    takeDamage(1);
                }
            }
        }
    }
    public void die(){
        setPosition(100,1000);
        currentHealth = maxHealth;
        velocity.y = 0;
        deathSound.play(0.4F);
    }

    public void playCoinPickup() {
        if (TimeUtils.timeSinceMillis(lastCoinPickupTime) > coinPickupInterval) {
            coinSound.play(0.3f*2,1, 2);
            lastCoinPickupTime = TimeUtils.millis();
        }
    }
    public static int getScore(){
        return score;
    }
    public int getCurrentHealth(){
        return currentHealth;
    }


    private Animation<TextureRegion> createFrogIdleAnimation() {
        float flameDuration = 1 / 11F;
        Array<TextureRegion> frames = new Array<>();
        frames.add(new TextureRegion(new Texture("my/frog/FrogIdle_0.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogIdle_1.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogIdle_2.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogIdle_3.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogIdle_4.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogIdle_5.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogIdle_6.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogIdle_7.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogIdle_8.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogIdle_9.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogIdle_10.png")));
        return new Animation<>(flameDuration, frames, Animation.PlayMode.LOOP);
    }

    private Animation<TextureRegion> createFrogRunAnimation() {
        float flameDuration = 1 / 11F;
        Array<TextureRegion> frames = new Array<>();
        frames.add(new TextureRegion(new Texture("my/frog/FrogRun_0.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogRun_1.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogRun_2.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogRun_3.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogRun_4.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogRun_5.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogRun_6.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogRun_7.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogRun_8.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogRun_9.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogRun_10.png")));
        return new Animation<>(flameDuration, frames, Animation.PlayMode.LOOP);
    }
    private Animation<TextureRegion> createFrogHitAnimation() {
        float flameDuration = 1 / 7F;
        Array<TextureRegion> frames = new Array<>();
        frames.add(new TextureRegion(new Texture("my/frog/FrogHit_0.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogHit_1.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogHit_2.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogHit_3.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogHit_4.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogHit_5.png")));
        frames.add(new TextureRegion(new Texture("my/frog/FrogHit_6.png")));
        return new Animation<>(flameDuration, frames, Animation.PlayMode.LOOP);
    }
    private Animation<TextureRegion> createMdIdleAnimation() {
        float flameDuration = 1 / 11F;
        Array<TextureRegion> frames = new Array<>();
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeIdle_0.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeIdle_1.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeIdle_2.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeIdle_3.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeIdle_4.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeIdle_5.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeIdle_6.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeIdle_7.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeIdle_8.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeIdle_9.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeIdle_10.png")));
        return new Animation<>(flameDuration, frames, Animation.PlayMode.LOOP);
    }

    private Animation<TextureRegion> createMdRunAnimation() {
        float flameDuration = 1 / 11F;
        Array<TextureRegion> frames = new Array<>();
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeRun_0.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeRun_1.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeRun_2.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeRun_3.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeRun_4.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeRun_5.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeRun_6.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeRun_7.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeRun_8.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeRun_9.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeRun_10.png")));

        return new Animation<>(flameDuration, frames, Animation.PlayMode.LOOP);
    }
    private Animation<TextureRegion> createMdHitAnimation() {
        float flameDuration = 1 / 7F;
        Array<TextureRegion> frames = new Array<>();
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeHit_0.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeHit_1.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeHit_2.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeHit_3.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeHit_4.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeHit_5.png")));
        frames.add(new TextureRegion(new Texture("my/maskdude/MaskDudeHit_6.png")));
        return new Animation<>(flameDuration, frames, Animation.PlayMode.LOOP);
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(
            playerTextureRegion,
            getX(),
            getY(),
            getOriginX(),
            getOriginY(),
            getWidth(),
            getHeight(),
            getScaleX(),
            getScaleY(),
            getRotation()
        );


    }

}
