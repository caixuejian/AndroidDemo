package com.example.dinus.androiddemo.contextmenu;

import android.view.View;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class AnimatorUtils {

    public static ObjectAnimator scaleY(View v, float from , float to) {
        return ObjectAnimator.ofFloat(v, "scaleY", from, to);
    }

    public static ObjectAnimator scaleX(View v, float from, float to) {
        return ObjectAnimator.ofFloat(v, "scaleX", from, to);
    }

    public static ObjectAnimator rotate(View v, float fromDegree, float toDegree) {
        return ObjectAnimator.ofFloat(v, "rotation", fromDegree, toDegree);
    }

    public static ObjectAnimator translationRight(View v, float x) {
        return ObjectAnimator.ofFloat(v, "translationX", 0, x);
    }
    public static ObjectAnimator translationLeft(View v, float x) {
        return ObjectAnimator.ofFloat(v, "translationX", x, 0);
    }
    public static ObjectAnimator translationX(View v, float from, float to) {
        return ObjectAnimator.ofFloat(v, "translationX", from, to);
    }
    public static ObjectAnimator translationY(View v, float from, float to) {
        return ObjectAnimator.ofFloat(v, "translationY", from, to);
    }


}
