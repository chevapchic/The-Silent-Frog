package ru.myitschool.platformer;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class TileMapActor extends Actor {

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;

    public TileMapActor(TiledMap tiledMap, OrthogonalTiledMapRenderer tiledMapRenderer) {
        this.tiledMap = tiledMap;
        this.tiledMapRenderer = tiledMapRenderer;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Получаем Stage и камеру из Stage
        Stage stage = getStage();
        if (stage == null) return; // Если нет Stage, то ничего не рисуем

        OrthographicCamera camera = (OrthographicCamera) stage.getCamera();
        if (camera == null) return; // Если нет камеры, то ничего не рисуем

        // Важно: Устанавливаем матрицу проекции для рендерера тайловой карты!
        tiledMapRenderer.setView(camera); // Use this if it works

        // Отрисовываем тайловую карту
        tiledMapRenderer.render();
    }
}
