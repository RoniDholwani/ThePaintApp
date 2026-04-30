package com.example.rdthepaintapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 5000; // 4 seconds
    private static final String PREFS_NAME = "app_prefs";

    private ConstraintLayout splashContainer;
    private ImageView logoImage;
    private TextView appTitleText;
    private TextView appSubtitleText;
    private TextView taglineText;
    private TextView developerCreditText;
    private TextView developerNameText;
    private View dividerLine;
    private ProgressBar loadingProgress;
    private View colorBlob1, colorBlob2, colorBlob3, colorBlob4, colorBlob5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try {
            initializeViews();
            setupAnimations();
            startColorBlobAnimations();

            // Redirect to MainActivity after delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    // Add cross-fade animation between activities
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    // Finish splash activity so user can't go back to it
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    // Fallback: just finish if MainActivity fails
                    finish();
                }
            }, SPLASH_DURATION);
        } catch (Exception e) {
            e.printStackTrace();
            // If splash fails, go directly to MainActivity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initializeViews() {
        splashContainer = findViewById(R.id.splashContainer);
        appTitleText = findViewById(R.id.appTitleText);
        appSubtitleText = findViewById(R.id.appSubtitleText);
        taglineText = findViewById(R.id.taglineText);
        developerCreditText = findViewById(R.id.developerCreditText);
        developerNameText = findViewById(R.id.developerNameText);
        dividerLine = findViewById(R.id.dividerLine);
        loadingProgress = findViewById(R.id.loadingProgress);
        colorBlob1 = findViewById(R.id.colorBlob1);
        colorBlob2 = findViewById(R.id.colorBlob2);
        colorBlob3 = findViewById(R.id.colorBlob3);
        colorBlob4 = findViewById(R.id.colorBlob4);
        colorBlob5 = findViewById(R.id.colorBlob5);
        logoImage = findViewById(R.id.logoImage);

        // Apply saved theme to splash screen
        boolean isDarkTheme = getSavedThemePreference();
        applySplashTheme(isDarkTheme);
    }

    private void applySplashTheme(boolean isDark) {
        if (splashContainer != null) {
            if (isDark) {
                splashContainer.setBackgroundColor(Color.parseColor("#121212"));
                if (appTitleText != null) appTitleText.setTextColor(Color.parseColor("#FFFFFF"));
                if (appSubtitleText != null) appSubtitleText.setTextColor(Color.parseColor("#E0E0E0"));
                if (taglineText != null) taglineText.setTextColor(Color.parseColor("#B0B0B0"));
                if (developerCreditText != null) developerCreditText.setTextColor(Color.parseColor("#808080"));
                if (developerNameText != null) developerNameText.setTextColor(Color.parseColor("#B0B0B0"));
            } else {
                splashContainer.setBackgroundColor(Color.parseColor("#FAFAFA"));
                if (appTitleText != null) appTitleText.setTextColor(Color.parseColor("#1A1A1A"));
                if (appSubtitleText != null) appSubtitleText.setTextColor(Color.parseColor("#424242"));
                if (taglineText != null) taglineText.setTextColor(Color.parseColor("#757575"));
                if (developerCreditText != null) developerCreditText.setTextColor(Color.parseColor("#9E9E9E"));
                if (developerNameText != null) developerNameText.setTextColor(Color.parseColor("#616161"));
            }
        }
    }

    private boolean getSavedThemePreference() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean("dark_theme", false);
    }

    private void setupAnimations() {
        // Logo scale and fade animation
        if (logoImage != null) {
            AnimationSet logoAnimationSet = new AnimationSet(true);
            ScaleAnimation scaleAnimation = new ScaleAnimation(
                    0.5f, 1.0f, 0.5f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            scaleAnimation.setDuration(1000);
            scaleAnimation.setInterpolator(this, android.R.interpolator.overshoot);

            AlphaAnimation fadeInAnimation = new AlphaAnimation(0f, 1f);
            fadeInAnimation.setDuration(1000);

            logoAnimationSet.addAnimation(scaleAnimation);
            logoAnimationSet.addAnimation(fadeInAnimation);
            logoAnimationSet.setFillAfter(true);
            logoImage.startAnimation(logoAnimationSet);
        }

        // App Title animation
        if (appTitleText != null) {
            AnimationSet titleAnimationSet = new AnimationSet(true);
            AlphaAnimation titleFadeIn = new AlphaAnimation(0f, 1f);
            titleFadeIn.setDuration(800);
            titleFadeIn.setStartOffset(300);

            ScaleAnimation titleScale = new ScaleAnimation(
                    0.8f, 1.0f, 0.8f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            titleScale.setDuration(800);
            titleScale.setStartOffset(300);

            titleAnimationSet.addAnimation(titleFadeIn);
            titleAnimationSet.addAnimation(titleScale);
            titleAnimationSet.setFillAfter(true);
            appTitleText.setVisibility(View.VISIBLE);
            appTitleText.startAnimation(titleAnimationSet);
        }

        // App Subtitle animation
        if (appSubtitleText != null) {
            Animation subtitleAnimation = new AlphaAnimation(0f, 1f);
            subtitleAnimation.setDuration(600);
            subtitleAnimation.setStartOffset(500);
            subtitleAnimation.setFillAfter(true);
            appSubtitleText.setVisibility(View.VISIBLE);
            appSubtitleText.startAnimation(subtitleAnimation);
        }

        // Tagline animation
        if (taglineText != null) {
            Animation taglineAnimation = new AlphaAnimation(0f, 1f);
            taglineAnimation.setDuration(600);
            taglineAnimation.setStartOffset(800);
            taglineAnimation.setFillAfter(true);
            taglineText.setVisibility(View.VISIBLE);
            taglineText.startAnimation(taglineAnimation);
        }

        // Divider line fade in animation
        if (dividerLine != null) {
            Animation dividerFadeIn = new AlphaAnimation(0f, 1f);
            dividerFadeIn.setDuration(800);
            dividerFadeIn.setStartOffset(1000);
            dividerFadeIn.setFillAfter(true);
            dividerLine.setVisibility(View.VISIBLE);
            dividerLine.startAnimation(dividerFadeIn);
        }

        // Developer credit animation (fades in last with elegant slide up)
        if (developerCreditText != null) {
            AnimationSet creditAnimationSet = new AnimationSet(true);

            AlphaAnimation creditFadeIn = new AlphaAnimation(0f, 1f);
            creditFadeIn.setDuration(1000);
            creditFadeIn.setStartOffset(2500);

            TranslateAnimation creditSlideUp = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0f
            );
            creditSlideUp.setDuration(1000);
            creditSlideUp.setStartOffset(2500);
            creditSlideUp.setInterpolator(this, android.R.interpolator.decelerate_cubic);

            creditAnimationSet.addAnimation(creditFadeIn);
            creditAnimationSet.addAnimation(creditSlideUp);
            creditAnimationSet.setFillAfter(true);
            developerCreditText.startAnimation(creditAnimationSet);
        }

        if (developerNameText != null) {
            AnimationSet nameAnimationSet = new AnimationSet(true);

            AlphaAnimation nameFadeIn = new AlphaAnimation(0f, 1f);
            nameFadeIn.setDuration(800);
            nameFadeIn.setStartOffset(2700);

            TranslateAnimation nameSlideUp = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0.8f,
                    Animation.RELATIVE_TO_SELF, 0f
            );
            nameSlideUp.setDuration(800);
            nameSlideUp.setStartOffset(2700);
            nameSlideUp.setInterpolator(this, android.R.interpolator.decelerate_cubic);

            nameAnimationSet.addAnimation(nameFadeIn);
            nameAnimationSet.addAnimation(nameSlideUp);
            nameAnimationSet.setFillAfter(true);
            developerNameText.startAnimation(nameAnimationSet);
        }
    }

    private void startColorBlobAnimations() {
        // Animate color blobs with random delays and durations
        if (colorBlob1 != null) animateBlob(colorBlob1, 2000, 0, 100, 100);
        if (colorBlob2 != null) animateBlob(colorBlob2, 2500, 500, -80, 80);
        if (colorBlob3 != null) animateBlob(colorBlob3, 3000, 1000, 120, -100);
        if (colorBlob4 != null) animateBlob(colorBlob4, 2800, 1500, -100, -80);
        if (colorBlob5 != null) animateBlob(colorBlob5, 2200, 800, 80, -120);
    }

    private void animateBlob(View blob, int duration, int startDelay, float translateX, float translateY) {
        if (blob == null) return;

        AnimationSet blobAnimationSet = new AnimationSet(true);

        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.3f, 1.0f, 1.3f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(duration);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(Animation.REVERSE);

        TranslateAnimation translateAnimation = new TranslateAnimation(
                0, translateX, 0, translateY
        );
        translateAnimation.setDuration(duration);
        translateAnimation.setRepeatCount(Animation.INFINITE);
        translateAnimation.setRepeatMode(Animation.REVERSE);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.3f, 0.7f);
        alphaAnimation.setDuration(duration / 2);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        blobAnimationSet.addAnimation(scaleAnimation);
        blobAnimationSet.addAnimation(translateAnimation);
        blobAnimationSet.addAnimation(alphaAnimation);
        blobAnimationSet.setStartOffset(startDelay);

        blob.startAnimation(blobAnimationSet);
    }
}