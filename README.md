# מסע הסוחרים (Merchant Voyage): אפליקציית אנדרואיד

המשחק עטוף באפליקציה (WebView) והקובץ של המשחק נמצא בתוכה, ולכן הוא עובד גם בלי אינטרנט.
(האינטרנט משמש רק לגופן. בלעדיו תופיע גופן ברירת מחדל.)

## דרך א׳: בלי מחשב, דרך GitHub (בערך 5 דקות)
1. היכנס ל־github.com (אפשר להירשם בחינם) ולחץ New repository.
2. לחץ "uploading an existing file" והעלה את **כל** תוכן התיקייה הזו (כולל התיקייה `.github`).
3. עבור ללשונית **Actions** ← "Build Android APK" ← Run workflow. לאחר כמה דקות תהיה ירוקה.
4. פתח את הריצה והורד מ־Artifacts את `sea-merchant-apk`. בפנים יש `app-debug.apk`.
5. העבר לטלפון, פתח אותו ואשר "התקנה ממקור לא ידוע".

## דרך ב׳: Android Studio
פתח את התיקייה ב־Android Studio ← Build ← Build APK(s). הקובץ ייווצר ב־`app/build/outputs/apk/debug/app-debug.apk`.

## מה בפנים
- `app/src/main/assets/index.html`: המשחק עצמו.
- `MainActivity.java`: מסך מלא, מסך שנשאר דלוק, וכפתור "חזרה" שפותח את חלון היציאה של המשחק.
