package step.learning.android_spd_222;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static final int FIELD_WIDTH = 16;
    private static final int FIELD_HEIGHT = 24;
    private static final int STEP_SPEED = 25;       // Швидкість збільшується на 25 одиниць
    private static final long TIME_SPEED = 20000;   // Швидкість збільшується через 20 секунд
    private TextView[][] gameField;
    private LinkedList<Vector2> snake = new LinkedList<>();
    private final Handler handler = new Handler();
    private int fieldColor;
    private int snakeColor;
    private Direction moveDirection;
    private boolean isPlaying;
    private static final String food = new String( Character.toChars(0x1F34E) );
    private static final String foodBonus = new String( Character.toChars(0x1F34C) );
    private Vector2 foodPosition;
    private Vector2 bonusPosition;
    private static final Random _random = new Random();
    private int foodsquantity = 0;
    private TextView tvFoodsquantity;
    private int speedGame = 700;
    private Date timeGame;
    private long timeGame1;
    private long timeGame2;
    private int sizeSnake;
    private int stepBonus;
    @Override
    protected void onPause() { // подія деактивації
        super.onPause();
        isPlaying = false;
    }
    @Override
    protected void onResume() { // подія активації після паузи
        super.onResume();
        if(!isPlaying) {
            isPlaying = true;
            step();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.main).setOnTouchListener(new OnSwipeListener(this) {
            @Override
            public void onSwipeBottom() {
                //Toast.makeText(GameActivity.this, "Bottom", Toast.LENGTH_SHORT).show();
                if( moveDirection != GameActivity.Direction.top ) { moveDirection = Direction.bottom; }
            }
            public void onSwipeTop() {
                //Toast.makeText(GameActivity.this, "Top", Toast.LENGTH_SHORT).show();
                if( moveDirection != Direction.bottom ) { moveDirection = Direction.top; }
            }
            public void onSwipeRight() {
                //Toast.makeText(GameActivity.this, "Right", Toast.LENGTH_SHORT).show();
                if( moveDirection != Direction.left ) { moveDirection = Direction.right; }
            }
            public void onSwipeLeft() {
                //Toast.makeText(GameActivity.this, "Left", Toast.LENGTH_SHORT).show();
                if( moveDirection != Direction.right ) { moveDirection = Direction.left; }
            }
        });
        fieldColor = getResources().getColor(R.color.game_field, getTheme());
        snakeColor = getResources().getColor(R.color.game_snake, getTheme());
        tvFoodsquantity = findViewById( R.id.textView_foods_quantity );
        initField();
        newGame();
    }
    private void step() {
        if (!isPlaying) return;
        Vector2 head = snake.getFirst();
        Vector2 newHead = new Vector2(head.x, head.y);
        switch (moveDirection) {
            case bottom: newHead.y += 1; break;
            case left: newHead.x -= 1; break;
            case right: newHead.x += 1; break;
            case top: newHead.y -= 1; break;
        }

        if ( isCellInSnake( newHead ) ) {
            gameOver();
            return;
        }
        /*if(newHead.x < 0 || newHead.x >= FIELD_WIDTH || newHead.y < 0 || newHead.y >= FIELD_HEIGHT) {
            gameOver();
            return;
        }*/

        if (newHead.x < 0) newHead.x = FIELD_WIDTH - 1;
        if (newHead.x > FIELD_WIDTH - 1) newHead.x = 0;
        if (newHead.y < 0) newHead.y = FIELD_HEIGHT - 1;
        if (newHead.y > FIELD_HEIGHT - 1) newHead.y = 0;

        if ( newHead.x == foodPosition.x && newHead.y == foodPosition.y ) {
            // видовження - це не прибирати хвіст
            // перенесення їжі, але не на змійку
            gameField[foodPosition.x][foodPosition.y].setText( "" );
            do {
                foodPosition = Vector2.random();
            } while ( isCellInSnake( foodPosition ) );
            gameField[foodPosition.x][foodPosition.y].setText( food );
            //speedGame -= 25;              // як варіант можно також зробити пришвидчення після кожного годування
            foodsquantity++;
            tvFoodsquantity.setText(String.valueOf(foodsquantity));
        }
        else {
            Vector2 tail = snake.getLast();
            snake.remove(tail);
            gameField[tail.x][tail.y].setBackgroundColor( fieldColor );
        }
        snake.addFirst(newHead);
        gameField[newHead.x][newHead.y].setBackgroundColor( snakeColor );
        /*
        + реалізувати збігання бонуса декілька разів при заході голови змійки на лінію бонусу
        - реалізувати зменьшення тіла змії після поєдання бонуса
        - як варіант реалізувати зменьшення швидкості після поєдання бонуса
        */
        if (sizeSnake == snake.size()) {
            if(bonusPosition != null) {
                gameField[bonusPosition.x][bonusPosition.y].setText( "" );
            }
            do {
                bonusPosition = Vector2.random();
                stepBonus = _random.nextInt(4);
            } while ( isCellInSnake( bonusPosition ) );
            gameField[bonusPosition.x][bonusPosition.y].setText( foodBonus );
            do {
                sizeSnake = _random.nextInt(20);
            } while ( sizeSnake < 4 );
        }
        if ( bonusPosition != null ) {
            if ( ( stepBonus != 0 ) && (newHead.x == bonusPosition.x || newHead.y == bonusPosition.y) ) {
                gameField[bonusPosition.x][bonusPosition.y].setText( "" );
                do {
                    bonusPosition = Vector2.random();
                } while ( isCellInSnake( bonusPosition ) );
                gameField[bonusPosition.x][bonusPosition.y].setText( foodBonus );
                stepBonus--;
            }
            else if (( stepBonus == 0 ) && (newHead.x == bonusPosition.x && newHead.y == bonusPosition.y)) {
                gameField[bonusPosition.x][bonusPosition.y].setText( "" );
                while (snake.size()>3) {
                    Vector2 tail = snake.getLast();
                    snake.remove(snake.getLast());
                    gameField[tail.x][tail.y].setBackgroundColor( fieldColor );
                }
            }
        }

        timeGame1 = timeGame.getTime() + TIME_SPEED;
        timeGame2 = new Date().getTime();
        if ( timeGame1 < timeGame2 ) {
            speedGame -= STEP_SPEED;
            timeGame.setTime(timeGame1);
            //foodsquantity += 10;        // Цей рядок був для перевірки
            //tvFoodsquantity.setText(String.valueOf(foodsquantity));
        }
        handler.postDelayed(this::step, speedGame);
    }
    private boolean isCellInSnake(Vector2 cell) {
        for(Vector2 v : snake) {
            if(v.x == cell.x && v.y == cell.y) return true;
        }
        return false;
    }
    private void initField() {
        LinearLayout field = findViewById(R.id.game_field);

        LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        tvLayoutParams.weight = 1f;
        tvLayoutParams.setMargins(4, 4, 4, 4);

        LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0
        );
        rowLayoutParams.weight = 1f;

        gameField = new TextView[FIELD_WIDTH][FIELD_HEIGHT];

        for (int j = 0; j < FIELD_HEIGHT; j++) {

            LinearLayout row = new LinearLayout(this);
            row.setOrientation( LinearLayout.HORIZONTAL );
            row.setLayoutParams( rowLayoutParams );

            for (int i = 0; i < FIELD_WIDTH; i++) {
                TextView tv = new TextView(this);
                tv.setBackgroundColor( fieldColor );
                //tv.setText( "0" );
                tv.setLayoutParams( tvLayoutParams );
                row.addView( tv );
                gameField[i][j] = tv;
            }
            field.addView( row );
        }
    }
    private void newGame() {
        for (Vector2 v : snake) {
            gameField[v.x][v.y].setBackgroundColor( fieldColor );
        }
        snake.clear();
        if(foodPosition != null) {
            gameField[foodPosition.x][foodPosition.y].setText( "" );
        }
        do {
            sizeSnake = _random.nextInt(10);
        } while ( sizeSnake < 6 );

        snake.add(new Vector2(8,10));
        snake.add(new Vector2(8,11));
        snake.add(new Vector2(8,12));
        snake.add(new Vector2(8,13));
        snake.add(new Vector2(8,14));
        for (Vector2 v : snake) {
            gameField[v.x][v.y].setBackgroundColor( snakeColor );
        }
        //foodPosition = new Vector2(3, 14);
        do {
            foodPosition = Vector2.random();
        } while ( isCellInSnake( foodPosition ) );
        gameField[foodPosition.x][foodPosition.y].setText( food );

        moveDirection = Direction.top;
        isPlaying = true;
        tvFoodsquantity.setText(String.valueOf(foodsquantity));
        timeGame = new Date();
        step();
    }
    private void gameOver() {
        isPlaying = false;
        new AlertDialog.Builder(this)
                .setTitle("Game Over!")
                .setMessage("Play one more time?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> newGame())
                .setNegativeButton("No", (dialog, which) -> finish())
                .show();
    }
    static class Vector2 {
        int x;
        int y;

        public Vector2(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public static Vector2 random() {
            return new Vector2(_random.nextInt(FIELD_WIDTH), _random.nextInt(FIELD_HEIGHT));
        }
    }
    enum Direction {
        bottom,
        left,
        right,
        top
    }
}