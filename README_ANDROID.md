# Guia de Build Android (APK)

Este projeto está configurado para gerar um APK automaticamente via **GitHub Actions** toda vez que você fizer um `push` para o branch `main`.

## Como obter o APK:
1. Exporte este projeto para o seu GitHub (Menu Settings -> Export to GitHub).
2. No seu repositório do GitHub, vá na aba **Actions**.
3. Selecione o workflow **Build Android APK**.
4. Quando o build terminar, você poderá baixar o APK nos **Artifacts** (no final da página do log do build).

## Desenvolvimento Local:
Se você tiver o Android Studio instalado:
1. `npm install`
2. `npm run build`
3. `npx cap add android` (apenas a primeira vez)
4. `npx cap open android`

## Permissões BYD:
O arquivo `capacitor.config.json` define o ID do pacote como `com.dolphin.lightcontrol`. Se precisar de permissões especiais de hardware, edite o `AndroidManifest.xml` dentro da pasta `android` após gerá-la.
