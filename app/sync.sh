#!/bin/bash

# Script para automatizar o push para o GitHub via terminal
# Requer variável de ambiente GITHUB_TOKEN e GITHUB_REPO (ex: usuario/repo)

if [ -z "$GITHUB_TOKEN" ]; then
    echo "Erro: A variável GITHUB_TOKEN não está configurada."
    echo "Por favor, adicione-a nas configurações do ambiente."
    exit 1
fi

if [ -z "$GITHUB_REPO" ]; then
    echo "Erro: A variável GITHUB_REPO não está configurada (ex: seu-usuario/seu-repositorio)."
    exit 1
fi

GIT_USER_NAME=${GITHUB_USER_NAME:-"AI Coding Agent"}
GIT_USER_EMAIL=${GITHUB_USER_EMAIL:-"agent@ai-studio.local"}

echo "Configurando Git..."
git config --global user.name "$GIT_USER_NAME"
git config --global user.email "$GIT_USER_EMAIL"

# Configura a URL remota com o token para autenticação silenciosa
REMOTE_URL="https://x-access-token:${GITHUB_TOKEN}@github.com/${GITHUB_REPO}.git"

echo "Preparando arquivos..."
git add .

COMMIT_MSG=${1:-"Auto-sync from Google AI Studio - $(date +'%Y-%m-%d %H:%M:%S')"}

echo "Criando commit: $COMMIT_MSG"
git commit -m "$COMMIT_MSG"

echo "Enviando para o GitHub..."
git push "$REMOTE_URL" main --force

if [ $? -eq 0 ]; then
    echo "Sucesso! Código sincronizado com https://github.com/${GITHUB_REPO}"
else
    echo "Erro ao sincronizar. Verifique o Token e as permissões do repositório."
fi
