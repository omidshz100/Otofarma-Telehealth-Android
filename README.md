# Otofarma Telehealth Android

Device companion for Otofarma tablets and phones. A person signs in with the Gmail address and app password created in the admin dashboard, then sees only the apps assigned to them.

This app does not measure ECG or SpO2, pair Bluetooth devices, or install, update, or remove apps silently. It also does not create Gmail accounts. The admin creates those accounts outside this platform and stores the details in the dashboard.

The interface is English. The backend and the dashboard are separate projects and talk to this app only through the API.

- Backend: https://github.com/omidshz100/Otofarma-Telehealth-Backend
- Dashboard: https://github.com/omidshz100/Otofarma-Telehealth-Dashboard

## Requirements

- Android 8.0 or newer (minSdk 26, targetSdk 35)
- An arm64-v8a device or emulator. Other ABIs are not included.
- Android Studio with Android SDK 35 and JDK 17
- The Otofarma API running on a computer the device can reach

The current target is a rugged Android 13 tablet with a Qualcomm QCM6125. A normal arm64 phone is fine for development.

## Server address

The API address is compiled into the app:

```kotlin
// app/src/main/java/com/otofarma/telehealth/data/SessionStore.kt
const val BASE_URL = "http://10.122.37.139:8000"
```

`127.0.0.1` and the emulator address `10.0.2.2` do not reach a phone. Use the computer's LAN address, for example the `en0` address from `ipconfig getifaddr en0` on macOS.

Start the API so it accepts connections from the network:

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

The tablet or phone must be on the same network. If the computer's IP changes, update `BASE_URL` and run the app again from Android Studio. Cleartext HTTP is allowed so a local server can be used without TLS.

## Run

1. Clone this repository and open the folder in Android Studio.
2. Let Gradle sync. The wrapper uses Gradle 8.14.1 and Android Gradle Plugin 8.10.1.
3. Set `BASE_URL` to the computer running the API.
4. Connect the device and press Run. Do not install by copying an APK unless you intend to sideload a build.

Package name: `com.otofarma.telehealth`. Version name `0.2.1`, version code `3`.

## Sign in

The login screen asks for the Gmail address and the **app password** from the dashboard. The Gmail password is not the login password.

An admin account is rejected. Only a device user (`kind` is `device`) can open this app.

## Home

Home lists the apps assigned to the signed-in user. The app reads installed packages on the device, compares each package name, and reports installed or not installed, plus the installed version, to the API. Apps that are not assigned are not reported.

| State | Action |
| --- | --- |
| Not installed, Google Play | **Google Play** opens the store link |
| Not installed, server file | **Download** saves the APK and opens the system installer |
| Installed | **Open** launches that app |
| Catalog version differs from the installed version name | **Update available** |

**Guide** opens the published guide for that app. Sections are Overview, Setup, Usage, and Problems. A section with no published text says that nothing has been published yet. Text and media come from the API, so a guide change does not need a new companion build.

**Refresh** loads the profile and the assigned list again. Opening the app also refreshes, but a non-empty list is kept for 12 seconds so a quick resume does not refetch.

Download uses the system package installer. The first time, Android asks the user to allow this app to install unknown apps. There is no silent install.

## Settings

Settings shows the person's name and role label. The role is a label only. It does not decide which apps appear.

**Passwords** shows:

- Gmail address, with copy
- Gmail password, with copy and edit
- App password, with copy and edit

**Save passwords** writes the changed values to the API. The app password is the Otofarma sign-in password, so the next sign-in uses the new value. The current session stays signed in. The Gmail password stored here is a copy for the device. Saving it does not change the Google account.

The same two passwords can be viewed and changed by an admin on the user page in the dashboard.

**Device** shows the device model and this app's version name. **Log out** clears the session on the device.

## Permissions

- `INTERNET` to call the API and download an APK
- `REQUEST_INSTALL_PACKAGES` to hand a downloaded APK to the system installer
- `QUERY_ALL_PACKAGES` to see whether each assigned app is installed

## What is not in this version

- Phone notification tray. Assignment, guide, and APK changes are stored by the API and shown in the dashboard. Tray delivery needs Firebase later.
- A screen to type a server address. Change `BASE_URL` and run again.
- Forgot-password, sign-up, and Ask AI screens.
