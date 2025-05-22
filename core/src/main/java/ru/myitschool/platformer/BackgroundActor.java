package ru.myitschool.platformer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class BackgroundActor extends Actor {
    private final Texture texture;

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture,0,0,MyGame.SCREEN_WIDTH,MyGame.SCREEN_HEIGHT);
    }

    public BackgroundActor(Texture texture) {
        this.texture = texture;
        setZIndex(0);
    }
}

