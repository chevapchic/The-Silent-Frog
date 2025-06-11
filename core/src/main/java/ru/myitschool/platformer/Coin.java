package ru.myitschool.platformer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;

public class Coin extends Actor {
    private int startX;
    private int startY;
    private boolean isTouched;
    private Texture coinTexture;
    private TextureRegion coinTextureRegion;
    private Animation<TextureRegion> animation;
    private float stateTime;
    private com.badlogic.gdx.math.Polygon hitBox;


    public Coin(int startX, int startY, Texture texture){
        this.startX = startX;
        this.startY = startY;
        this.isTouched = isTouched;
        this.coinTexture = texture;
        coinTextureRegion = new TextureRegion(coinTexture);
        animation = createAnimation();
        setSize(coinTexture.getWidth(), coinTexture.getHeight());


        float[] vertices = {
            0, 0,
            0, getHeight(),
            getWidth(), getHeight(),
            getWidth(), 0
        };
        hitBox = new Polygon(vertices);
        setPosition(startX, startY);
    }

    public Animation<TextureRegion> createAnimation(){
        float frameDuration = 1 / 5F;
        Array<TextureRegion> frames = new Array<>();
        frames.add(new TextureRegion(new Texture("spinning coin_0.png")));
        frames.add(new TextureRegion(new Texture("spinning coin_1.png")));
        frames.add(new TextureRegion(new Texture("spinning coin_2.png")));
        frames.add(new TextureRegion(new Texture("spinning coin_3.png")));
        frames.add(new TextureRegion(new Texture("spinning coin_4.png")));
        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }
    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += 2*delta;
        hitBox.setPosition(getX(), getY());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(
            coinTextureRegion,
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

        coinTextureRegion = animation.getKeyFrame(stateTime, true);
    }


    public Polygon getHitBox() {
        return hitBox;
    }
}
