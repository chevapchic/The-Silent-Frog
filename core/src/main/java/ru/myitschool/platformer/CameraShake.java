package ru.myitschool.platformer;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

public class CameraShake {

    private OrthographicCamera camera;
    private float shakeIntensity;
    private float shakeDuration;
    private float currentShakeTime;
    private float originalCameraX, originalCameraY;
    private boolean isShaking = false;

    public CameraShake(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void shake(float intensity, float duration) {
        if (!isShaking) {
            shakeIntensity = intensity;
            shakeDuration = duration;
            currentShakeTime = 0;
            originalCameraX = camera.position.x;
            originalCameraY = camera.position.y;
            isShaking = true;
        }
    }

    public void update(float delta) {
        if (isShaking) {
            if (currentShakeTime < shakeDuration) {

                float xOffset = MathUtils.random(-shakeIntensity, shakeIntensity);
                float yOffset = MathUtils.random(-shakeIntensity, shakeIntensity);


                camera.position.x = originalCameraX + xOffset;
                camera.position.y = originalCameraY + yOffset;

                currentShakeTime += delta;

                camera.update();
            } else {

                camera.position.x = originalCameraX;
                camera.position.y = originalCameraY;
                camera.update();

                isShaking = false;
            }
        }
    }

    public boolean isShaking() {
        return isShaking;
    }
}
