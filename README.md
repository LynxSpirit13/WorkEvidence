# WorkEvidence

An Android-first personal incident journal for documenting workplace events and organizing supporting evidence.

## First prototype
- Save incident notes locally on the device.
- Attach a timestamp and SHA-256 digest to each saved entry.
- View saved entries and share a plain-text export.
- No account, cloud sync, or upload of incident notes.
- This first prototype does **not** record audio or video yet.

## Build an APK
GitHub Actions builds a debug APK when code is pushed to `main`, or when **Build Android APK** is manually started from the Actions tab.

After a successful run:
1. Open **Actions** in this repository.
2. Select the latest **Build Android APK** run.
3. Under **Artifacts**, download `WorkEvidence-debug-apk`.
4. Extract the ZIP and install the APK on your Android phone.

## Privacy and evidence integrity
The repository contains app source code only. Do not commit real workplace notes, recordings, IDs, payslips, or other private evidence. The app stores its journal on the device, but a hash only helps detect later changes; it does not prove that an entry is true or that the device clock was accurate. Keep original evidence files unchanged and make separate backups.

Recording features, if added later, must use Android's visible, permission-based recording behavior and comply with local law and workplace rules. Screen-off/background recording is not guaranteed.
