package ru.myitschool.platformer;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class SolidActor extends Actor {

    private Polygon hitBox;

    public SolidActor(float x, float y, float width, float height) {
        float[] vertices = {
            0, 0,
            0, height,
            width, height,
            width, 0
        };
        hitBox = new Polygon(vertices);
        hitBox.setPosition(x, y);

        setSize(width, height);
        setPosition(x, y);
    }

    public Polygon getHitBox() {
        return hitBox;
    }
}
