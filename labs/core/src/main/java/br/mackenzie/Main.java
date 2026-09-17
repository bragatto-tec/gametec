package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class Main implements ApplicationListener {
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite bucketSprite;

    Texture fundo;                      
    float fundoOffsetY = 0f;            
    float velocidadeFundo = 1.5f;      

    Texture spriteParadoTexture;        
    Texture spriteMovendoTexture;       
    Animation<TextureRegion> animacaoNave; 
    TextureRegion frameAtual;
    float stateTimeNave;                
    boolean movendo;

    Vector2 touchPos;
    Texture dropTexture;               
    Array<Sprite> dropSprites;
    float dropTimer;

    Rectangle bucketRectangle;
    Rectangle dropRectangle;

    Texture tiroTexture;                
    Array<Sprite> tiroSprites;
    Rectangle tiroRectangle;
    float tiroWidth = 0.15f, tiroHeight = 0.4f;
    float velocidadeTiro = 2.2f;
    float tiroTimer;
    float intervaloTiro = 0.3f;       

    Sound dropSound;
    Music music;

    @Override
    public void create() {
        fundo = new Texture("tela_fundo.jpg");
        dropTexture = new Texture("drop.png");
        tiroTexture = new Texture("tiro.png");

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        spriteParadoTexture = new Texture("sprite1.png");
        spriteMovendoTexture = new Texture("sprite2.png");

        TextureRegion[] framesNave = new TextureRegion[2];
        framesNave[0] = new TextureRegion(spriteParadoTexture);
        framesNave[1] = new TextureRegion(spriteMovendoTexture);
        animacaoNave = new Animation<>(0.1f, framesNave);

        frameAtual = framesNave[0];
        bucketSprite = new Sprite(frameAtual);
        bucketSprite.setSize(1, 1);

        touchPos = new Vector2();

        dropSprites = new Array<>();
        tiroSprites = new Array<>();

        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();
        tiroRectangle = new Rectangle();

        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));

        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        if (bucketSprite == null) {
            return;
        }

        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();
        movendo = false;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta);
            movendo = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta);
            movendo = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            bucketSprite.translateY(speed * delta);
            movendo = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            bucketSprite.translateY(-speed * delta);
            movendo = true;
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenter(touchPos.x, touchPos.y);
            movendo = true;
        }
    }

    private void atirar() {
        Sprite tiroSprite = new Sprite(tiroTexture);
        tiroSprite.setSize(tiroWidth, tiroHeight);
        tiroSprite.setPosition(
            bucketSprite.getX() + bucketSprite.getWidth() / 2f - tiroWidth / 2f,
            bucketSprite.getY() + bucketSprite.getHeight()
        );
        tiroSprites.add(tiroSprite);
    }

    private void logic() {
        if (bucketSprite == null) {
            return;
        }

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));
        bucketSprite.setY(MathUtils.clamp(bucketSprite.getY(), 0, worldHeight - bucketHeight));

        float delta = Gdx.graphics.getDeltaTime();

        fundoOffsetY -= velocidadeFundo * delta;

        if (movendo) {
            stateTimeNave += delta;
            frameAtual = animacaoNave.getKeyFrame(stateTimeNave, true);
        } else {
            frameAtual = animacaoNave.getKeyFrames()[0];
        }
        bucketSprite.setRegion(frameAtual);

        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        tiroTimer += delta;
        if (tiroTimer > intervaloTiro) {
            tiroTimer = 0;
            atirar();
        }

        for (int i = tiroSprites.size - 1; i >= 0; i--) {
            Sprite tiroSprite = tiroSprites.get(i);
            tiroSprite.translateY(velocidadeTiro * delta);
            if (tiroSprite.getY() > worldHeight) {
                tiroSprites.removeIndex(i);
            }
        }

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-2f * delta);
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            boolean atingidoPorTiro = false;
            for (int j = tiroSprites.size - 1; j >= 0; j--) {
                Sprite tiroSprite = tiroSprites.get(j);
                tiroRectangle.set(tiroSprite.getX(), tiroSprite.getY(),
                    tiroSprite.getWidth(), tiroSprite.getHeight());
                if (dropRectangle.overlaps(tiroRectangle)) {
                    tiroSprites.removeIndex(j);
                    atingidoPorTiro = true;
                    break;
                }
            }
            if (atingidoPorTiro) {
                dropSprites.removeIndex(i);
                continue;
            }

            if (dropSprite.getY() < -dropHeight) {
                dropSprites.removeIndex(i);
            } else if (bucketRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i);
                dropSound.play();
                bucketSprite = null;
                break;
            }
        }

        dropTimer += delta;
        if (dropTimer > 1f) {
            dropTimer = 0;
            createDroplet();
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        float y1 = fundoOffsetY % worldHeight;
        if (y1 > 0) y1 -= worldHeight;

        spriteBatch.draw(fundo, 0, y1, worldWidth, worldHeight);
        spriteBatch.draw(fundo, 0, y1 + worldHeight, worldWidth, worldHeight);

        for (Sprite tiroSprite : tiroSprites) {
            tiroSprite.draw(spriteBatch);
        }

        if (bucketSprite != null) {
            bucketSprite.draw(spriteBatch);
        }

        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    private void createDroplet() {
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        fundo.dispose();
        dropTexture.dispose();
        tiroTexture.dispose();
        spriteParadoTexture.dispose();
        spriteMovendoTexture.dispose();
        spriteBatch.dispose();
        dropSound.dispose();
        music.dispose();
    }
}
