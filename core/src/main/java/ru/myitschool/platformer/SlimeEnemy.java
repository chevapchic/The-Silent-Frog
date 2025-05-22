package ru.myitschool.platformer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class SlimeEnemy extends Actor {
    private Texture slimeTexture;
    private TextureRegion slimeTextureRegion;
    private Polygon hitbox;
    private Animation<TextureRegion> animation;
    private float stateTime = 0;
    private float deathStateTime = 0;
    private int y;

    private Animation<TextureRegion> deathAnimation;
    public boolean isDead = false;
    private Vector2 velocity;
    private int minX;
    private int maxX;
    private boolean facingRight = false;
    private float SLIME_SPEED = 60F;


    public SlimeEnemy(Texture texture, int startX, int startY, boolean isDead, int minX1, int maxX1) {
        this.slimeTexture = texture;
        this.slimeTextureRegion = new TextureRegion(slimeTexture);
        this.minX = minX1;
        this.maxX = maxX1;
        this.y = startY;
        setSize(slimeTexture.getWidth()/1.4F,slimeTexture.getHeight()/1.4F);
        setScale(1,1);


        velocity = new Vector2();

        float[] vertices = {
            0,0,
            0,getHeight(),
            getWidth(),getHeight(),
            getWidth(),0,
        };
        hitbox = new Polygon(vertices);
        animation = createAnimation();



    }

    private Animation<TextureRegion> createAnimation(){
        float flameDuration = 1/6F;

        Array<TextureRegion> frames = new Array<>();
        frames.add(new TextureRegion(new Texture("slime_walk_0.png")));
        frames.add(new TextureRegion(new Texture("slime_walk_1.png")));
        frames.add(new TextureRegion(new Texture("slime_walk_2.png")));
        frames.add(new TextureRegion(new Texture("slime_walk_3.png")));
//        frames.add(new TextureRegion(new Texture("slime1.png")));
//        frames.add(new TextureRegion(new Texture("slime2.png")));
//        frames.add(new TextureRegion(new Texture("slime3.png")));
        return new Animation<>(flameDuration, frames, Animation.PlayMode.LOOP);
    }


    @Override
    public void act(float delta) {
        super.act(delta);

        stateTime+=0.8F*delta;

        if(facingRight&&!slimeTextureRegion.isFlipX()){
            slimeTextureRegion.flip(true,false);
            facingRight = true;
        }else if(!facingRight&&slimeTextureRegion.isFlipX()){
            slimeTextureRegion.flip(true,false);
            facingRight = false;
        }

        if(facingRight){
            velocity.x += SLIME_SPEED*delta;
            if(velocity.x>maxX){
                facingRight = false;
                velocity.x = maxX;
            }
        }
        else{
            velocity.x -= SLIME_SPEED*delta;
            if(velocity.x < minX) {
                facingRight = true;
                velocity.x = minX;
            }
        }




//        System.out.println("-----------");
//        System.out.println(velocity.x);
//        System.out.println(velocity.y);
//        System.out.println("-----------");
        setPosition(velocity.x, y);

        if(isDead){
            if(!deathAnimation.isAnimationFinished(deathStateTime)) {
                deathStateTime+=delta;
            }
        }
        hitbox.setPosition(getX(),getY());

    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(!isDead){
            slimeTextureRegion = animation.getKeyFrame(stateTime, true);
        }
//        else{
//            setWidth(slimeTexture.getWidth()*1.2F);
//            setHeight(slimeTexture.getHeight()*1.2F);
//            setPosition(getX(), getY());
//            slimeTextureRegion = deathAnimation.getKeyFrame(deathStateTime, false);
//
//        }


        batch.draw(
            slimeTextureRegion,
            getX()-getWidth()/2,
            getY()-getHeight()/2,
            getOriginX(),
            getOriginY(),
            getWidth(),
            getHeight(),
            getScaleX()*2.25F,
            getScaleY()*2.25F,
            getRotation()
        );
    }

    public Polygon getHitBox() {
        return hitbox;
    }
}


