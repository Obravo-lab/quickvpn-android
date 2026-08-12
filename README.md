# QuickVPN — Client Android

Application Android officielle de [QuickVPN](https://quickvpn.fr), VPN hors Europe (WireGuard, 50 Mbps, sans logs).

## Fonctionnalités
- Création de compte / connexion (même compte que le site web)
- Dashboard : statut abonnement, accès VPN, configuration
- Récupération de la configuration WireGuard depuis l'API QuickVPN
- Connexion VPN intégrée (moteur WireGuard, à venir)
- Abonnement via Google Play Billing (à venir)
- Français / English

## Architecture
- Kotlin + Jetpack Compose (Material 3, thème sombre)
- API REST : `https://quickvpn.fr/api/v1/index.php` (Bearer token, 90 j renouvelable)
- Token stocké chiffré (EncryptedSharedPreferences)
- Navigation Compose

## Structure
```
app/src/main/java/fr/quickvpn/android/
  core/network/     # Retrofit + DTO (contrat API)
  core/security/    # TokenStore chiffré
  ui/screens/       # Onboarding, Auth, Dashboard
  ui/navigation/    # Routes + factory ViewModel
  ui/theme/         # Thème sombre (vert #198754, fond #0A0A0F)
```

## Build
Prérequis : JDK 17, Android SDK (platform 35).  
`./gradlew assembleDebug` (APK dans `app/build/outputs/apk/debug/`).

## Licence
GPLv2 — voir `LICENSE`. Le client est open-source ; le backend reste propriétaire.
