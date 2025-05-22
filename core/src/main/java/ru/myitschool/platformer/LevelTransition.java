package ru.myitschool.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class LevelTransition {

    private ShapeRenderer shapeRenderer;
    private float fadeAlpha = 0;
    private boolean isFadingIn = false;
    private boolean isFadingOut = false;
    private Runnable onComplete;
    private float fadeDuration = 0.3f;
    private float fadeTimer = 0;
    private boolean transitionComplete = false;

    public LevelTransition() {
        shapeRenderer = new ShapeRenderer();
    }

    public void startFade(Runnable onComplete) {
        if (isFadingIn || isFadingOut || transitionComplete) return;
        this.onComplete = onComplete;
        isFadingIn = true;
        isFadingOut = false;
        transitionComplete = false;
        fadeAlpha = 0;
        fadeTimer = 0;
        Gdx.input.setInputProcessor(null);
    }

    public void render(float delta) {
        if (isFadingIn) {
            fadeTimer += delta;
            fadeAlpha = Math.min(1, fadeTimer / fadeDuration);

            if (fadeTimer >= fadeDuration) {
                isFadingIn = false;
                isFadingOut = true;
                fadeTimer = 0;
                onComplete.run();
            }
        } else if (isFadingOut) {
            fadeTimer += delta;
            fadeAlpha = Math.max(0, 1 - (fadeTimer / fadeDuration));

            if (fadeTimer >= fadeDuration) {
                isFadingOut = false;
                transitionComplete = true;
                Gdx.input.setInputProcessor(null);
            }
        }

        if (isFadingIn || isFadingOut) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, fadeAlpha);
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        }
    }

    public boolean isTransitionComplete() {
        return transitionComplete;
    }
}
