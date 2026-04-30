# 🎨 Art Studio - Professional Paint App

A modern, feature-rich paint application for Android built using Java and XML. Designed with a clean UI and smooth user experience, Art Studio offers powerful drawing tools suitable for both casual users and creative enthusiasts.

---

## 🚀 Overview

Art Studio is a lightweight yet powerful Android drawing app that allows users to create, edit, and save digital artwork with ease. It focuses on performance, simplicity, and a polished Material Design interface.

---

## ✨ Features

### 🎯 Core Drawing Features
- Freehand drawing with smooth brush strokes  
- Adjustable brush size with real-time preview  
- Eraser tool for quick corrections  
- Undo and redo functionality  
- Clear canvas option with confirmation  
- Multiple preset colors (Red, Green, Blue, Black, White)  
- Advanced color picker (HSV color wheel)  
- Anti-aliased smooth stroke rendering  

---

### 🎨 UI/UX Features
- Material Design 3 based modern interface  
- Splash screen with animation  
- Dark mode and Light mode support  
- Smooth transitions and animations  
- Visual feedback for selected tools  
- Responsive layout for multiple screen sizes  

---

### 💾 Data Persistence
- Auto-save canvas on app pause  
- Restore drawing session automatically  
- Save last used brush size and color  
- Theme preference memory  
- File-based canvas backup  

---

### 📤 Export & Sharing
- Save artwork directly to gallery (PNG format)  
- High-quality image export  
- Timestamp-based file naming  
- Uses MediaStore API (Android Q+)  
- Backward compatibility for older Android versions  

---

## ## 🏗️ Project Structure

```
ArtStudio/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/rdthepaintapp/
│   │   │   ├── SplashActivity.java
│   │   │   ├── MainActivity.java
│   │   │   ├── PaintView.java
│   │   │   └── ColorWheelView.java
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── drawable/
│   │   │   ├── mipmap/
│   │   │   ├── values/
│   │   │   └── xml/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🧩 Core Components

| Component           | Description |
|--------------------|------------|
| SplashActivity     | Animated splash screen |
| MainActivity       | Main UI and tool handling |
| PaintView          | Custom canvas for drawing |
| ColorWheelView     | Advanced color picker |

---

## 🔧 Tech Stack

| Category        | Technology |
|----------------|-----------|
| Language       | Java 11 |
| UI Framework   | XML + Material Design 3 |
| Build System   | Gradle (Kotlin DSL) |
| Graphics       | Android Canvas API |
| Animation      | Lottie |

---

## 🚀 Getting Started

### 📋 Prerequisites
- Android Studio (Latest Version Recommended)  
- Android SDK 36  
- JDK 17  
- Gradle 8+  

---

### ⚙️ Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/ArtStudio.git
cd ArtStudio
```
2. Open in Android Studio

3. Let Gradle sync complete

4. Click Run ▶️

###   📱 Device Requirements
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36
- Storage: ~15MB
- Supports both phones and tablets

## 🎯 How to Use
###   🖌️ Drawing
- Select a color
- Adjust brush size
- Start drawing on canvas


### ⚙️ Tools
- Use eraser to remove strokes
- Undo/Redo actions anytime
- Clear canvas for fresh start

### 💾 Saving
- Tap save button
- Image will be stored in gallery

### 🎨 Themes
- Switch between Light & Dark mode
- Preference is saved automatically
  
### 💡 Tips
- Eraser size is double the brush size
- Canvas auto-saves when app is closed
- Use color picker for precise colors
- Smooth strokes improve visual quality

### 🔧 Configuration
```
android {
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.rdthepaintapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}
```
### 📦 Dependencies
- AndroidX Libraries
- Material Components
- Lottie Animation

## 🛠️ Development

###🤝 Contributing
- Fork the repository
- Create a new branch
- Commit your changes
- Push and create Pull Request

### 🧾 Code Guidelines
- Follow clean coding practices
- Keep methods small and readable
- Use meaningful variable names
- Handle exceptions properly

### 🔜 Upcoming Features
- Multiple brush styles
- Shape tools (circle, rectangle, line)
- Layer system
- Paint bucket fill
- Zoom and pan gestures
- Stylus pressure support

### 📊 Performance
- APK Size: ~8MB
- Startup Time: <2 seconds
- Smooth 60 FPS drawing
- Low memory usage

### 👤 Developer

- Ronit Dholwani
- 📧 ronitkailash1006@gmail.com
- 💻 GitHub: [https://github.com/RoniDholwani](https://github.com/RoniDholwani)
- 💻 Linkedin: [https://github.com/RoniDholwani](https://www.linkedin.com/in/ronit-dholwani/)

### 🤝 Support
- Open an issue for bugs or suggestions
- Contact directly for collaboration

<p align="center"> <b>Made with ❤️ by Ronit Dholwani</b><br> <i>Where Creativity Meets Innovation</i> </p> 
