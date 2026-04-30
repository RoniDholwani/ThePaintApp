package com.example.rdthepaintapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private PaintView paintView;
    private View colorRed, colorGreen, colorBlue, colorBlack;
    private ImageButton btnColorPicker;
    private View colorRedBorder, colorGreenBorder, colorBlueBorder, colorBlackBorder, colorPickerBorder;
    private MaterialButton btnEraser, btnReset, btnSave;
    private ImageButton btnUndo, btnRedo, btnThemeToggle;
    private SeekBar seekBarBrushSize;
    private View brushSizePreviewCircle;
    private TextView brushSizeText;
    private View brushPreviewSmall, brushPreviewLarge;
    private int currentColor = Color.parseColor("#212121");
    private View lastSelectedBorderView = null;
    private boolean isDarkTheme = false;
    private Bitmap savedCanvasBitmap = null;

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_CURRENT_COLOR = "current_color";
    private static final String KEY_IS_ERASER_MODE = "is_eraser_mode";
    private static final String KEY_HAS_CANVAS_DATA = "has_canvas_data";
    private static final String KEY_CANVAS_BITMAP = "canvas_bitmap";
    private static final String KEY_SELECTED_BORDER_ID = "selected_border_id";
    private static final String KEY_BRUSH_SIZE = "brush_size";

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load saved theme preference
        isDarkTheme = getSavedThemePreference();

        // Apply theme
        applyTheme(isDarkTheme);

        setContentView(R.layout.activity_main);

        initializeViews();
        setupListeners();
        setupWindowInsets();
        checkPermissions();

        // Restore canvas state first
        restoreCanvasState();

        // Then restore other UI state
        restoreUIState();

        // Update canvas colors based on current theme
        updateCanvasForTheme(isDarkTheme);

        // Set brush size from saved preference
        restoreBrushSize();

        // Ensure correct border is highlighted
        ensureCorrectBorderHighlight();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCanvasState();
        saveUIState();
    }

    private void saveCanvasState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save canvas bitmap
        Bitmap bitmap = paintView.getBitmapForSaving();
        if (bitmap != null && paintView.hasDrawings()) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            try {
                // Save to file for reliability
                saveCanvasToFile(bitmap);
                editor.putBoolean(KEY_HAS_CANVAS_DATA, true);

                // Also save in preferences for backup
                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                editor.putString(KEY_CANVAS_BITMAP, encoded);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            editor.putBoolean(KEY_HAS_CANVAS_DATA, false);
            editor.remove(KEY_CANVAS_BITMAP);
        }

        editor.apply();
    }

    private void saveUIState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save current color
        editor.putInt(KEY_CURRENT_COLOR, currentColor);
        editor.putBoolean(KEY_IS_ERASER_MODE, paintView.isEraserMode());
        editor.putInt(KEY_BRUSH_SIZE, seekBarBrushSize.getProgress() + 1);

        // Save which border is selected
        if (lastSelectedBorderView != null) {
            int borderId = lastSelectedBorderView.getId();
            editor.putInt(KEY_SELECTED_BORDER_ID, borderId);
        } else {
            editor.putInt(KEY_SELECTED_BORDER_ID, -1);
        }

        editor.apply();
    }

    private void saveCanvasToFile(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(), "canvas_state.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreCanvasState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // First try to restore from file (most reliable)
        if (prefs.getBoolean(KEY_HAS_CANVAS_DATA, false)) {
            boolean restored = restoreCanvasFromFile();

            // If file restore failed, try preferences
            if (!restored) {
                String encoded = prefs.getString(KEY_CANVAS_BITMAP, null);
                if (encoded != null) {
                    try {
                        byte[] byteArray = Base64.decode(encoded, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        if (bitmap != null) {
                            savedCanvasBitmap = bitmap;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void restoreUIState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Restore current color
        int defaultColor = isDarkTheme ? Color.WHITE : Color.parseColor("#212121");
        currentColor = prefs.getInt(KEY_CURRENT_COLOR, defaultColor);
        paintView.setPaintColor(currentColor);

        // Restore eraser mode
        if (prefs.getBoolean(KEY_IS_ERASER_MODE, false)) {
            paintView.setEraserMode();
        }

        // Restore saved canvas bitmap after paintView is ready
        if (savedCanvasBitmap != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (paintView != null && savedCanvasBitmap != null) {
                    paintView.restoreFromBitmap(savedCanvasBitmap);
                }
            }, 100);
        }
    }

    private void restoreBrushSize() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedBrushSize = prefs.getInt(KEY_BRUSH_SIZE, 11);
        seekBarBrushSize.setProgress(savedBrushSize - 1);
        paintView.setBrushSize(savedBrushSize);

        // Update brush previews
        ViewGroup.LayoutParams params = brushSizePreviewCircle.getLayoutParams();
        params.width = savedBrushSize * 2;
        params.height = savedBrushSize * 2;
        brushSizePreviewCircle.setLayoutParams(params);
        brushSizeText.setText("Brush Size: " + savedBrushSize);
        updateBrushPreviews(savedBrushSize);
    }

    private boolean restoreCanvasFromFile() {
        try {
            File file = new File(getCacheDir(), "canvas_state.png");
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    savedCanvasBitmap = bitmap;
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initializeViews() {
        paintView = findViewById(R.id.paintView);

        colorRed = findViewById(R.id.color_red);
        colorGreen = findViewById(R.id.color_green);
        colorBlue = findViewById(R.id.color_blue);
        colorBlack = findViewById(R.id.color_black);
        btnColorPicker = findViewById(R.id.btn_color_picker);

        colorRedBorder = findViewById(R.id.color_red_border);
        colorGreenBorder = findViewById(R.id.color_green_border);
        colorBlueBorder = findViewById(R.id.color_blue_border);
        colorBlackBorder = findViewById(R.id.color_black_border);
        colorPickerBorder = findViewById(R.id.color_picker_border);

        seekBarBrushSize = findViewById(R.id.seekBarBrushSize);
        brushSizePreviewCircle = findViewById(R.id.brushSizePreviewCircle);
        brushSizeText = findViewById(R.id.brushSizeText);
        brushPreviewSmall = findViewById(R.id.brushPreviewSmall);
        brushPreviewLarge = findViewById(R.id.brushPreviewLarge);

        btnEraser = findViewById(R.id.btn_eraser);
        btnReset = findViewById(R.id.btn_reset);
        btnSave = findViewById(R.id.btn_save);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            int bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            int left = insets.getInsets(WindowInsetsCompat.Type.systemBars()).left;
            int right = insets.getInsets(WindowInsetsCompat.Type.systemBars()).right;

            v.setPadding(left, 0, right, bottom);
            return insets;
        });
    }

    private void setupListeners() {
        colorRed.setOnClickListener(v -> {
            currentColor = Color.parseColor("#FF5252");
            paintView.setPaintColor(currentColor);
            removeEraserMode();
            highlightSelectedColor(colorRedBorder);
        });

        colorGreen.setOnClickListener(v -> {
            currentColor = Color.parseColor("#69F0AE");
            paintView.setPaintColor(currentColor);
            removeEraserMode();
            highlightSelectedColor(colorGreenBorder);
        });

        colorBlue.setOnClickListener(v -> {
            currentColor = Color.parseColor("#448AFF");
            paintView.setPaintColor(currentColor);
            removeEraserMode();
            highlightSelectedColor(colorBlueBorder);
        });

        colorBlack.setOnClickListener(v -> {
            currentColor = isDarkTheme ? Color.WHITE : Color.parseColor("#212121");
            paintView.setPaintColor(currentColor);
            removeEraserMode();
            highlightSelectedColor(colorBlackBorder);
        });

        btnColorPicker.setOnClickListener(v -> {
            showColorPickerDialog();
        });

        btnThemeToggle.setOnClickListener(v -> {
            // Save current state before recreating
            saveCanvasState();
            saveUIState();

            // Toggle theme
            isDarkTheme = !isDarkTheme;
            saveThemePreference(isDarkTheme);

            // Recreate activity
            recreate();
        });

        seekBarBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int brushSize = progress + 1;
                paintView.setBrushSize(brushSize);

                ViewGroup.LayoutParams params = brushSizePreviewCircle.getLayoutParams();
                params.width = brushSize * 2;
                params.height = brushSize * 2;
                brushSizePreviewCircle.setLayoutParams(params);

                brushSizeText.setText("Brush Size: " + brushSize);
                updateBrushPreviews(brushSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnEraser.setOnClickListener(v -> {
            paintView.setEraserMode();
            highlightSelectedColor(null);
            Toast.makeText(MainActivity.this, "Eraser mode activated", Toast.LENGTH_SHORT).show();
        });

        btnReset.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Clear Canvas")
                    .setMessage("Are you sure you want to clear the entire canvas?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        paintView.resetCanvas();
                        highlightSelectedColor(colorBlackBorder);
                        Toast.makeText(MainActivity.this, "Canvas cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnSave.setOnClickListener(v -> saveDrawing());

        btnUndo.setOnClickListener(v -> paintView.undo());
        btnRedo.setOnClickListener(v -> paintView.redo());
    }

    private void removeEraserMode() {
        if (paintView.isEraserMode()) {
            paintView.setPaintColor(currentColor);
        }
    }

    private void ensureCorrectBorderHighlight() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isEraserMode = prefs.getBoolean(KEY_IS_ERASER_MODE, false);

        if (!isEraserMode) {
            int savedBorderId = prefs.getInt(KEY_SELECTED_BORDER_ID, -1);
            if (savedBorderId != -1) {
                View border = findViewById(savedBorderId);
                if (border != null) {
                    highlightSelectedColor(border);
                } else {
                    // Default to black if no valid border found
                    highlightSelectedColor(colorBlackBorder);
                }
            } else {
                // Default to black
                highlightSelectedColor(colorBlackBorder);
            }
        }
    }

    private void applyTheme(boolean dark) {
        if (dark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void updateCanvasForTheme(boolean dark) {
        int canvasColor = dark ? Color.parseColor("#1E1E1E") : Color.parseColor("#FAFAFA");
        int eraserBgColor = dark ? Color.parseColor("#1E1E1E") : Color.parseColor("#FAFAFA");

        // Update color black based on theme
        if (!paintView.isEraserMode()) {
            if (dark && currentColor == Color.parseColor("#212121")) {
                currentColor = Color.WHITE;
                paintView.setPaintColor(currentColor);
            } else if (!dark && currentColor == Color.WHITE) {
                currentColor = Color.parseColor("#212121");
                paintView.setPaintColor(currentColor);
            }
        }

        paintView.setCanvasBackground(canvasColor);
        paintView.updateEraserColor(eraserBgColor);

        // Update color black button tint
        if (dark) {
            colorBlack.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        } else {
            colorBlack.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#212121")));
        }
    }

    private void saveThemePreference(boolean isDark) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DARK_THEME, isDark)
                .apply();
    }

    private boolean getSavedThemePreference() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_DARK_THEME, false);
    }

    private void updateBrushPreviews(int size) {
        if (brushPreviewSmall != null && brushPreviewLarge != null) {
            ViewGroup.LayoutParams smallParams = brushPreviewSmall.getLayoutParams();
            smallParams.width = Math.max(4, size / 2);
            smallParams.height = Math.max(4, size / 2);
            brushPreviewSmall.setLayoutParams(smallParams);

            ViewGroup.LayoutParams largeParams = brushPreviewLarge.getLayoutParams();
            largeParams.width = Math.min(40, size + 10);
            largeParams.height = Math.min(40, size + 10);
            brushPreviewLarge.setLayoutParams(largeParams);
        }
    }

    private void highlightSelectedColor(View newBorderView) {
        // Hide all borders first
        if (colorRedBorder != null) colorRedBorder.setVisibility(View.GONE);
        if (colorGreenBorder != null) colorGreenBorder.setVisibility(View.GONE);
        if (colorBlueBorder != null) colorBlueBorder.setVisibility(View.GONE);
        if (colorBlackBorder != null) colorBlackBorder.setVisibility(View.GONE);
        if (colorPickerBorder != null) colorPickerBorder.setVisibility(View.GONE);

        // Show the selected border
        if (newBorderView != null) {
            newBorderView.setVisibility(View.VISIBLE);
            lastSelectedBorderView = newBorderView;
        } else {
            lastSelectedBorderView = null;
        }
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void showColorPickerDialog() {
        Dialog colorDialog = new Dialog(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        colorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        colorDialog.setCancelable(true);
        Objects.requireNonNull(colorDialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);
        mainLayout.setBackgroundColor(Color.WHITE);

        TextView titleText = new TextView(this);
        titleText.setText("Color Picker");
        titleText.setTextSize(24);
        titleText.setTextColor(Color.parseColor("#212121"));
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 30);
        titleText.setLayoutParams(titleParams);
        mainLayout.addView(titleText);

        FrameLayout previewFrame = new FrameLayout(this);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        previewParams.setMargins(0, 0, 0, 30);
        previewFrame.setLayoutParams(previewParams);
        previewFrame.setBackground(createRoundedBackground(20, Color.parseColor("#F5F5F5")));

        LinearLayout previewContent = new LinearLayout(this);
        previewContent.setOrientation(LinearLayout.HORIZONTAL);
        previewContent.setPadding(24, 24, 24, 24);
        previewContent.setGravity(Gravity.CENTER_VERTICAL);

        View colorPreview = new View(this);
        LinearLayout.LayoutParams previewBoxParams = new LinearLayout.LayoutParams(100, 100);
        colorPreview.setLayoutParams(previewBoxParams);
        colorPreview.setBackground(createRoundedBackground(16, currentColor));

        LinearLayout colorInfo = new LinearLayout(this);
        colorInfo.setOrientation(LinearLayout.VERTICAL);
        colorInfo.setPadding(30, 0, 0, 0);

        TextView hexText = new TextView(this);
        hexText.setText(String.format("#%06X", (0xFFFFFF & currentColor)));
        hexText.setTextSize(22);
        hexText.setTextColor(Color.parseColor("#212121"));
        hexText.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView rgbText = new TextView(this);
        int red = Color.red(currentColor);
        int green = Color.green(currentColor);
        int blue = Color.blue(currentColor);
        rgbText.setText(String.format("RGB(%d, %d, %d)", red, green, blue));
        rgbText.setTextSize(14);
        rgbText.setTextColor(Color.parseColor("#757575"));

        colorInfo.addView(hexText);
        colorInfo.addView(rgbText);
        previewContent.addView(colorPreview);
        previewContent.addView(colorInfo);
        previewFrame.addView(previewContent);
        mainLayout.addView(previewFrame);

        ColorWheelView colorWheel = new ColorWheelView(this);
        LinearLayout.LayoutParams wheelParams = new LinearLayout.LayoutParams(400, 400);
        wheelParams.setMargins(0, 0, 0, 30);
        wheelParams.gravity = Gravity.CENTER;
        colorWheel.setLayoutParams(wheelParams);
        mainLayout.addView(colorWheel);

        TextView satLabel = new TextView(this);
        satLabel.setText("Saturation");
        satLabel.setTextSize(14);
        satLabel.setTextColor(Color.parseColor("#424242"));
        satLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        satLabel.setPadding(0, 0, 0, 10);
        mainLayout.addView(satLabel);

        SeekBar saturationBar = new SeekBar(this);
        saturationBar.setMax(100);
        saturationBar.setProgress(100);
        mainLayout.addView(saturationBar);

        TextView briLabel = new TextView(this);
        briLabel.setText("Brightness");
        briLabel.setTextSize(14);
        briLabel.setTextColor(Color.parseColor("#424242"));
        briLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        briLabel.setPadding(0, 20, 0, 10);
        mainLayout.addView(briLabel);

        SeekBar brightnessBar = new SeekBar(this);
        brightnessBar.setMax(100);
        brightnessBar.setProgress(50);
        mainLayout.addView(brightnessBar);

        colorWheel.setOnColorChangeListener(color -> {
            float sat = saturationBar.getProgress() / 100f;
            float bri = brightnessBar.getProgress() / 100f;
            int finalColor = applySaturationAndBrightness(color, sat, bri);

            colorPreview.setBackground(createRoundedBackground(16, finalColor));
            hexText.setText(String.format("#%06X", (0xFFFFFF & finalColor)));
            rgbText.setText(String.format("RGB(%d, %d, %d)",
                    Color.red(finalColor), Color.green(finalColor), Color.blue(finalColor)));
            currentColor = finalColor;
        });

        SeekBar.OnSeekBarChangeListener sliderListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int baseColor = colorWheel.getSelectedColor();
                float sat = saturationBar.getProgress() / 100f;
                float bri = brightnessBar.getProgress() / 100f;
                int finalColor = applySaturationAndBrightness(baseColor, sat, bri);

                colorPreview.setBackground(createRoundedBackground(16, finalColor));
                hexText.setText(String.format("#%06X", (0xFFFFFF & finalColor)));
                rgbText.setText(String.format("RGB(%d, %d, %d)",
                        Color.red(finalColor), Color.green(finalColor), Color.blue(finalColor)));
                currentColor = finalColor;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        saturationBar.setOnSeekBarChangeListener(sliderListener);
        brightnessBar.setOnSeekBarChangeListener(sliderListener);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);
        buttonLayout.setPadding(0, 30, 0, 0);

        MaterialButton btnCancel = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnCancel.setText("Cancel");
        btnCancel.setCornerRadius(40);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        cancelParams.setMargins(0, 0, 10, 0);
        btnCancel.setLayoutParams(cancelParams);
        btnCancel.setOnClickListener(v -> colorDialog.dismiss());

        MaterialButton btnSelect = new MaterialButton(this);
        btnSelect.setText("Apply");
        btnSelect.setCornerRadius(40);
        LinearLayout.LayoutParams selectParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        selectParams.setMargins(10, 0, 0, 0);
        btnSelect.setLayoutParams(selectParams);
        btnSelect.setOnClickListener(v -> {
            paintView.setPaintColor(currentColor);
            removeEraserMode();
            highlightSelectedColor(colorPickerBorder);
            colorDialog.dismiss();
            Toast.makeText(MainActivity.this, "Color applied: " +
                    String.format("#%06X", (0xFFFFFF & currentColor)), Toast.LENGTH_SHORT).show();
        });

        buttonLayout.addView(btnCancel);
        buttonLayout.addView(btnSelect);
        mainLayout.addView(buttonLayout);

        colorDialog.setContentView(mainLayout);
        colorDialog.show();
    }

    private int applySaturationAndBrightness(int color, float saturation, float brightness) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = saturation;
        hsv[2] = brightness;
        return Color.HSVToColor(hsv);
    }

    private GradientDrawable createRoundedBackground(int radius, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(radius);
        drawable.setColor(color);
        return drawable;
    }

    private void saveDrawing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (!checkPermissions()) {
                return;
            }
        }

        Bitmap bitmap = paintView.getBitmap();
        if (bitmap == null) {
            Toast.makeText(this, "Nothing to save! Draw something first.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "ArtStudio_" + timeStamp + ".png";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveBitmapToGallery(bitmap, fileName);
            } else {
                saveBitmapToExternalStorage(bitmap, fileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveBitmapToGallery(Bitmap bitmap, String fileName) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ArtStudio");
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            }

            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);

            Toast.makeText(this, "Image saved to gallery: " + fileName, Toast.LENGTH_LONG).show();
        }
    }

    private void saveBitmapToExternalStorage(Bitmap bitmap, String fileName) throws Exception {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "ArtStudio");

        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);
        FileOutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        outputStream.flush();
        outputStream.close();

        MediaStore.Images.Media.insertImage(getContentResolver(),
                file.getAbsolutePath(), fileName, "ArtStudio Pro Drawing");

        Toast.makeText(this, "Image saved to gallery: " + fileName, Toast.LENGTH_LONG).show();
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. You can now save images.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Cannot save images.", Toast.LENGTH_LONG).show();
            }
        }
    }
}