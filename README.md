# מסע הסוחרים · Merchant Voyage

שני משחקים (דפדפן, עברית, RTL) וקבצי בנייה לאנדרואיד ולווינדוס.

| תיקייה | מה בפנים |
|---|---|
| `games/sea-merchant` | מסע הסוחרים (המשחק המקורי) |
| `games/sea-merchant-2` | מסע הסוחרים 2 · נמלים ומלונות |
| `android` | פרויקט אנדרואיד (WebView), משותף לשני המשחקים |
| `windows` | עטיפת Electron למחשב |

## קבצי אנדרואיד (כרגע: משחק 2 בלבד, APK + AAB)
ה־workflow `Build Android (APK + AAB)` בונה לכל משחק קובץ APK וקובץ AAB,  כרגע רק משחק 2. להוספת המשחק המקורי: `game: [sea-merchant, sea-merchant-2]` ב־workflow. הקבצים:
`sea-merchant-apk`, `sea-merchant-aab`, `sea-merchant-2-apk`, `sea-merchant-2-aab`.

1. העלה את התיקייה ל־GitHub. הבנייה תרוץ אוטומטית (או: Actions ← Run workflow).
2. הורד את הקבצים מ־Artifacts בסוף הריצה.

### חתימה (חובה ל־Google Play)
בלי סודות ה־APK יוצא כ־debug וה־AAB לא חתום. כדי לחתום, הוסף ב־Settings ← Secrets and variables ← Actions:
`KEYSTORE_BASE64` (הקובץ upload.jks אחרי `base64 -w0`), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
**אל תעלה את upload.jks או קובץ סיסמאות ל־git.** `.gitignore` חוסם אותם.

### הגדרות כל משחק
`games/<משחק>/game.env`: מזהה האפליקציה, שם, `VERSION_CODE` (להעלות בכל גרסה חדשה ב־Play).

## הערות
- גרסת האפליקציה של משחק 2 היא בלי מצב אונליין (כפתור זמן אמת מוסתר). מצב תור אחרי תור על מכשיר אחד עובד. הגרסה המלאה עם אונליין רצה בתוך claude.ai.
- דף הפרטיות: `android/docs/privacy.html`.
