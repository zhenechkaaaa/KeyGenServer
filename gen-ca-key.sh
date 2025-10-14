#!/bin/bash
set -e

KEY_FILE="src/main/resources/ca-pkcs8.key"

if [ -f "$KEY_FILE" ]; then
  echo "CA key already exists — skipping generation."
  exit 0
fi

mkdir -p src/main/resources

# Генерим обычный PEM RSA ключ
openssl genrsa -out src/main/resources/ca.key 8192

# Конвертируем в PKCS#8
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt \
  -in src/main/resources/ca.key \
  -out src/main/resources/ca-pkcs8.key
