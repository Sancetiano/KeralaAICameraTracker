# Kerala AI Camera Tracker

The Kerala AI Camera Tracker is a mobile application designed to help users track AI cameras placed on roads in Kerala. The app notifies users when they are near an AI camera and triggers an alert to notify them about the presence of the camera.

<div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px;">
<div style="text-align: center;">
        <h3>TRACKER ON/OFF</h3>
        <img src="https://github.com/rameshvoltella/KeralaAICameraTracker/blob/main/appfiles/one.jpeg?raw=true" alt="Tracker OFF" style="width: 40%; height: 40%;">
        <img src="https://github.com/rameshvoltella/KeralaAICameraTracker/blob/main/appfiles/teo.jpeg?raw=true" alt="Tracker ON" style="width: 40%; height: 40%;">
    </div>
<div style="text-align: center;">
        <h3>MAP VIEWS</h3>
     <img src="https://github.com/Sancetiano/KeralaAICameraTracker/blob/main/appfiles/three.jpeg?raw=true" alt="Location List" style="width: 40%; height: 40%;">
    <img src="https://github.com/Sancetiano/KeralaAICameraTracker/blob/main/appfiles/eight.jpeg?raw=true" alt="MAP VIEW" style="width: 40%; height: 40%;">

</div>
<div style="text-align: center;">
        <h3>NOTIFICATION ALERT</h3>
        <img src="https://github.com/Sancetiano/KeralaAICameraTracker/blob/main/appfiles/five.jpeg?raw=true" alt="NOTIFICATION 1" style="width: 40%; height: 40%;">

    <img src="https://github.com/Sancetiano/KeralaAICameraTracker/blob/main/appfiles/six.png?raw=true" alt="NOTIFICATION 2" style="width: 40%; height: 40%;">

</div>
<div style="text-align: center;">
        <h3>SETTINGS AND LOCATION LIST</h3>
        <img src="https://github.com/Sancetiano/KeralaAICameraTracker/tree/main/appfiles/three.jpeg?raw=true" alt="NOTIFICATION 1" style="width: 40%; height: 40%;">
    <img src="https://github.com/Sancetiano/KeralaAICameraTracker/tree/main/appfiles/sev.jpeg?raw=true" alt="NOTIFICATION 2" style="width: 40%; height: 40%;">

</div>
</div>

## Features

- **Dynamic Camera Radar**: Automatically scans and monitors the nearest 20 AI cameras in your vicinity.
- **Multi-Distance Proximity Alerts**: Choose when to be notified with configurable alert distances (250m, 500m, 750m, 1000m, 1500m, 2000m).
- **Voice Guidance**: Professional Text-to-Speech feedback for tracking status ("Live tracking enabled") and camera proximity warnings.
- **Pass Notifications**: Get peace of mind with a speed summary notification immediately after passing a camera point.
- **High-Priority Lock Screen Alerts**: Notifications are configured to wake up your screen and show alerts even when the device is locked.
- **Smart Battery Saving**: Background location check-in frequency dynamically adjusts based on your distance to the nearest camera (from 5 seconds to 15 minutes).
- **OSM Map Interface**: Integrated OpenStreetMap for visualizing camera locations and user position with auto-zoom functionality.
- **In-App Navigation**: Quickly jump to Google Maps navigation for any specific camera location with a single click.
- **Speedometer**: Real-time speed tracking with color-coded warnings for overspeeding.

## How It Works

1. **Location Tracking**: The app grabs your current location and starts sorting through the nearest cameras faster than you can say "cheese"!
2. **Camera Radar**: Once we've got our eyes on the road, we narrow down the next 20 cameras in your vicinity to ensure peak app performance.
3. **Frequent Check-ins**: The app uses a dynamic scheduling logic. If you are within 2km of a camera, it checks every 5 seconds for high accuracy. As you move further away, the frequency scales up to 15 minutes to save battery.
4. **Geo-Fencing Alert**: When your vehicle enters a configured radius of a camera, the app sends out a friendly voice or sound alert.
5. **U-Turn Protection**: The "Passed Camera" notification only triggers if you actually graze the 100-meter core coordinates of the camera, preventing false alerts if you simply turn around before reaching it.
6. **OSM to the Rescue**: The app uses OSMDroid to pinpoint camera locations, navigate with ease, and calculate distances accurately without relying on expensive proprietary map APIs.

## Security & Privacy

- **Local Processing**: All location data and proximity calculations are processed **exclusively on your device**. Your coordinates are never uploaded to any server.
- **No Data Harvesting**: The app does not require a user account, email, or any personal identification to function.
- **Offline Camera Data**: The MVD AI camera location list is stored locally within the app assets.
- **Permission Transparency**: The app requests background location access solely to provide alerts while your phone is in your pocket or the screen is off.

## Prerequisites

To build or contribute to this project, ensure your local environment meets the following requirements:

- **Java Development Kit (JDK)**: Version 17 is required.
- **Android Studio**: Android Studio Hedgehog | 2023.1.1 or newer is recommended. The latest version of Android Studio is optimal as it have agentic ai capabilities.
- **Kotlin**: Version 1.9.22.
- **Gradle**: The project uses Kotlin DSL for Gradle builds.
- **KSP**: The project is migrated to KSP (Kotlin Symbol Processing) for faster build times and future-proofing.

## Build Warnings

> [!IMPORTANT]
> **Avoid Multiple Editors**: Do not have the project open in two IDEs or code editors (e.g., Android Studio and VS Code) simultaneously. This often leads to `IOException` or "Access Denied" errors during the build process as both editors may attempt to lock the same `R.jar` or intermediate build files.

## Installation

### Android

1. Download the APK file from here [DOWNLOAD](https://github.com/Sancetiano/KeralaAICameraTracker/releases/tag/v2.1)
2. Enable installation from "Unknown Sources" in your device settings.
3. Install the APK file on your Android device.
4. **Crucial**: Ensure location permission is set to **"Allow all the time"** in the app settings for background tracking to work correctly.

## Contributing

I welcome contributions from the community to improve the Kerala AI Camera Tracker app.

1. Fork the repository.
2. Clone the repository to your local machine (`git clone https://github.com/YOUR_USERNAME/KeralaAICameraTracker.git`).
3. Create a new branch (`git checkout -b feature/improvement`).
4. Make your changes and commit them (eg. `git commit -am 'Add feature/improvement'`).
5. Push to the branch (`git push origin feature/improvement`).
6. Create a new Pull Request.
