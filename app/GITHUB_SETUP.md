# Instruções para Automação GitHub

Para que o script de sincronização automática funcione, você precisa configurar as seguintes variáveis de ambiente nas **Settings** (Configurações) deste ambiente do Google AI Studio:

1.  **`GITHUB_TOKEN`**: Crie um "Personal Access Token (classic)" no GitHub com permissões de `repo`.
2.  **`GITHUB_REPO`**: O caminho do seu repositório (exemplo: `seu-usuario/DolphinController`).
3.  **`GITHUB_USER_NAME`** (Opcional): Seu nome para o commit.
4.  **`GITHUB_USER_EMAIL`** (Opcional): Seu email para o commit.

### Como usar:
Basta me pedir: "**Sincronizar com o GitHub**" ou "**Fazer push agora**".
Eu executarei o script `./sync.sh` e enviarei todas as alterações direto para o seu repositório sem você precisar clicar em botões.
