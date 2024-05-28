package step.learning.android_spd_222;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AnimActivity extends AppCompatActivity {

    private Animation opacityAnimation;
    private Animation sizeAnimation;
    private Animation size2Animation;
    private Animation arcAnimation;
    private Animation arc2Animation;
    private Animation moveAnimation;
    private AnimationSet comboAnimation;
    private boolean isMovePlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        opacityAnimation = AnimationUtils.loadAnimation(this, R.anim.opacity);
        findViewById(R.id.anim_opacity_block).setOnClickListener(this::opacityClick);

        sizeAnimation = AnimationUtils.loadAnimation(this, R.anim.size);
        findViewById(R.id.anim_size_block).setOnClickListener(this::sizeClick);

        size2Animation = AnimationUtils.loadAnimation(this, R.anim.size2);
        findViewById(R.id.anim_size2_block).setOnClickListener(this::size2Click);

        arcAnimation = AnimationUtils.loadAnimation(this, R.anim.arc);
        findViewById(R.id.anim_arc_block).setOnClickListener(this::arcClick);

        arc2Animation = AnimationUtils.loadAnimation(this, R.anim.arc2);
        findViewById(R.id.anim_bell_block).setOnClickListener(this::arc2Click);

        isMovePlaying = false;
        moveAnimation = AnimationUtils.loadAnimation(this, R.anim.move);
        findViewById(R.id.anim_move_block).setOnClickListener(this::moveClick);

        comboAnimation = new AnimationSet(false);
        comboAnimation.addAnimation(opacityAnimation);
        //comboAnimation.addAnimation(sizeAnimation);
        comboAnimation.addAnimation(size2Animation);
        comboAnimation.addAnimation(arc2Animation);

        //comboAnimation = AnimationUtils.loadAnimation(this, R.anim.combo);
        findViewById(R.id.anim_combo_block).setOnClickListener(this::comboClick);
    }

    private void opacityClick(View view) {
        view.startAnimation( opacityAnimation );
    }

    private void sizeClick(View view) {
        view.startAnimation( sizeAnimation );
    }
    private void size2Click(View view) {
        view.startAnimation( size2Animation );
    }
    private void arcClick(View view) {
        view.startAnimation( arcAnimation );
    }
    private void arc2Click(View view) {
        view.startAnimation( arc2Animation );
    }
    private void moveClick(View view) {
        if (isMovePlaying) {
            isMovePlaying = false;
            view.clearAnimation();
        }
        else {
            isMovePlaying = true;
            view.startAnimation( moveAnimation );
        }
    }
    private void comboClick(View view) {
        view.startAnimation( comboAnimation );
    }
}