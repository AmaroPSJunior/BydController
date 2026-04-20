# Dolphin Light Control - Native Android (Kotlin)

Este projeto foi totalmente migrado para **Kotlin Nativo** utilizando **Jetpack Compose**. 

## Estrutura do Projeto:
- **Linguagem:** Kotlin
- **Arquitetura:** ViewModel + StateFlow (State management robusto)
- **UI:** Jetpack Compose (Declarativa Nativa)
- **Ferramenta de Build:** Gradle
- **CI/CD:** GitHub Actions (Gera APK nativo automaticamente)

## Como baixar o APK:
1. Exporte o projeto para o GitHub.
2. Na aba **Actions**, o workflow "Build Native Android APK" será executado.
3. Baixe o APK nativo nos artefatos do build.

## Configurações Técnicas:
- O código-fonte principal está em `app/src/main/java/com/dolphin/lightcontrol/MainActivity.kt`.
- O build é gerenciado pelos arquivos `build.gradle` na raiz e no diretório `app`.
- A interface segue o tema **Geometric Balance** implementado nativamente em Compose.

## Nota sobre Preview:
Como este é um projeto Android Nativo, o preview no iframe (web) não exibirá a interface. O build deve ser verificado diretamente via APK em um dispositivo Android ou na central multimídia do BYD.
