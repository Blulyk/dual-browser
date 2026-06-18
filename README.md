# Dual Browser

Navegador Android local y basado en Chromium WebView, diseñado primero para la AYN Thor y adaptable a dispositivos de una sola pantalla.

## Beta 0.1

- Web principal en la pantalla superior de la Thor.
- Centro de control en la pantalla inferior, con modo web secundario opcional.
- Pestañas normales y privadas que abren Google, barra de búsqueda/URL, restauración de sesión, historial y favoritos locales.
- Descargas, subida de archivos, vídeo fullscreen, cámara, micrófono y geolocalización.
- Controles físicos: B vuelve atrás y L1/R1 cambian de pestaña.
- Sin cuentas, anuncios, telemetría ni servidores.

## Obtener la APK

Descarga `dual-browser-v0.1.0-beta.1.apk` desde [GitHub Releases](../../releases). Comprueba que el SHA-256 coincida con el publicado en la release.

En Android, permite temporalmente instalar aplicaciones desconocidas para la app desde la que abras la APK. Instálala y vuelve a desactivar ese permiso si no lo necesitas.

Con depuración USB también puedes instalarla así:

```powershell
adb install -r dual-browser-v0.1.0-beta.1.apk
```

## Compilar

Requiere Android Studio con SDK Platform 36, Build-Tools 36.0.0 y JDK 21.

```powershell
. .\scripts\bootstrap-android.ps1
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

La APK debug se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Privacidad

Los datos se guardan solo en el dispositivo. Consulta [docs/privacy.md](docs/privacy.md). Los diagnósticos no salen del dispositivo hasta pulsar **Export**.

## Pruebas en AYN Thor

Sigue [docs/testing/ayn-thor-checklist.md](docs/testing/ayn-thor-checklist.md) y adjunta el informe de Diagnostics al reportar problemas de pantallas, ocultando cualquier dato que no quieras compartir.

## Limitaciones conocidas

- La primera beta depende del proveedor Android System WebView instalado y actualizado en el dispositivo.
- La asignación de pantallas puede variar con futuras actualizaciones del firmware de la Thor.
- No hay sincronización, extensiones, bloqueador de anuncios ni gestor propio de contraseñas.

