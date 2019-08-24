package com.example.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.myapplication.utils.PuzzleAnimImpl;
import com.example.myapplication.view.CutImageView;

import static com.example.myapplication.utils.PuzzleAnimImpl.MODE_BOTTOM_TO_TOP;
import static com.example.myapplication.utils.PuzzleAnimImpl.MODE_LEFT_TO_RIGHT;
import static com.example.myapplication.utils.PuzzleAnimImpl.MODE_OPPOSITE;
import static com.example.myapplication.utils.PuzzleAnimImpl.MODE_RIGHT_TO_LEFT;
import static com.example.myapplication.utils.PuzzleAnimImpl.MODE_TOP_TO_BOTTOM;


public class CutActivity extends AppCompatActivity {
    ImageView pictureBottomView;
    CutImageView pictureTopView;
    int currentImgIndex = 1;
    int currentAnimIndex = 0;
    int[] resIds = new int[]{
            R.mipmap.pic_1,
            R.mipmap.pic_2,
            R.mipmap.pic_3
    };
    PuzzleAnimImpl[] anims = new PuzzleAnimImpl[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);
        pictureBottomView = findViewById(R.id.bottom_img);
        pictureTopView = findViewById(R.id.top_img);
        Button button = findViewById(R.id.start);

        final PuzzleAnimImpl.AnimCallback callback = new PuzzleAnimImpl.AnimCallback() {
            @Override
            public void onAnimationEnd() {
                currentAnimIndex = (currentAnimIndex + 1) % anims.length;
                anims[currentAnimIndex].initClipArea(pictureTopView, false);
                pictureTopView.setImageResource(resIds[currentImgIndex]);
                pictureBottomView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        anims[currentAnimIndex].startAnim();
                        currentImgIndex = (currentImgIndex + 1) % resIds.length;
                        pictureBottomView.setImageResource(resIds[currentImgIndex]);
                    }
                }, 1200);
            }
        };

        pictureTopView.post(new Runnable() {
            @Override
            public void run() {
                anims[0] = new PuzzleAnimImpl();
                anims[0].initAnimByMode(pictureTopView, new CutImageView.ClipPoint(0.33f, 0f), new CutImageView.ClipPoint(0.66f, -1f), MODE_OPPOSITE, MODE_OPPOSITE, 2000, callback);

                anims[1] = new PuzzleAnimImpl();
                anims[1].initAnimByMode(pictureTopView, new CutImageView.ClipPoint(0.38f, 0f), new CutImageView.ClipPoint(0.38f, -1f), MODE_RIGHT_TO_LEFT, MODE_LEFT_TO_RIGHT, 2000, callback);

                anims[2] = new PuzzleAnimImpl();
                anims[2].initAnimByMode(pictureTopView, new CutImageView.ClipPoint(0.4f, 0f), new CutImageView.ClipPoint(0.66f, -1f), MODE_BOTTOM_TO_TOP, MODE_TOP_TO_BOTTOM, 2000, callback);

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureTopView.setIsShowSrc(false);
                anims[currentAnimIndex].startAnim();
            }
        });
    }


}
