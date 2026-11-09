
#!/usr/bin/env bash

# W2-T6: Khoi tao file .env an toan cho Linux/macOS.
# Khong ghi de mat khau PostgreSQL trong .env da ton tai.

set -euo pipefail
umask 077

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TEMPLATE="$ROOT/.env.example"
ENV_FILE="$ROOT/.env"

if [[ ! -f "$TEMPLATE" ]]; then
    echo "LOI: Khong tim thay .env.example." >&2
    exit 1
fi

CREATING=0
SOURCE_FILE="$ENV_FILE"

if [[ ! -f "$ENV_FILE" ]]; then
    CREATING=1
    SOURCE_FILE="$TEMPLATE"
fi

JWT_COUNT=0
PASSWORD_COUNT=0
CURRENT_JWT=""

# Doc bien moi truong dang van ban, khong chay lenh trong .env.
while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line%$'\r'}"

    case "$line" in
        JWT_SECRET=*)
            JWT_COUNT=$((JWT_COUNT + 1))
            CURRENT_JWT="${line#JWT_SECRET=}"
            ;;
        POSTGRES_PASSWORD=*)
            PASSWORD_COUNT=$((PASSWORD_COUNT + 1))
            ;;
    esac
done < "$SOURCE_FILE"

if [[ "$JWT_COUNT" -ne 1 ]]; then
    echo "LOI: Can dung mot dong JWT_SECRET." >&2
    exit 1
fi

if [[ "$CREATING" -eq 1 && "$PASSWORD_COUNT" -ne 1 ]]; then
    echo "LOI: File mau can dung mot dong POSTGRES_PASSWORD." >&2
    exit 1
fi

JWT_BYTES=$(printf '%s' "$CURRENT_JWT" | LC_ALL=C wc -c | tr -d '[:space:]')

CHANGE_JWT=0

if [[ "$CREATING" -eq 1 ]] ||
   [[ -z "$CURRENT_JWT" ]] ||
   [[ "$CURRENT_JWT" == your_* ]] ||
   [[ "$CURRENT_JWT" == default_* ]] ||
   [[ "$JWT_BYTES" -lt 32 ]]; then
    CHANGE_JWT=1
fi

# Khong thay doi file .env neu JWT_SECRET da hop le.
if [[ "$CREATING" -eq 0 && "$CHANGE_JWT" -eq 0 ]]; then
    echo "File .env da ton tai va JWT_SECRET hop le."
    echo "Khong thay doi cau hinh hien tai."
    exit 0
fi

if ! command -v openssl >/dev/null 2>&1; then
    echo "LOI: Can cai dat OpenSSL de sinh khoa bi mat." >&2
    exit 1
fi

if [[ "$CHANGE_JWT" -eq 1 ]]; then
    NEW_JWT="$(openssl rand -hex 32)"
fi

if [[ "$CREATING" -eq 1 ]]; then
    NEW_PASSWORD="$(openssl rand -hex 24)"
fi

TEMP_FILE="$(mktemp "${ENV_FILE}.tmp.XXXXXXXX")"
trap 'rm -f "$TEMP_FILE"' EXIT

while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line%$'\r'}"

    case "$line" in
        JWT_SECRET=*)
            if [[ "$CHANGE_JWT" -eq 1 ]]; then
                printf 'JWT_SECRET=%s\n' "$NEW_JWT"
            else
                printf '%s\n' "$line"
            fi
            ;;
        POSTGRES_PASSWORD=*)
            if [[ "$CREATING" -eq 1 ]]; then
                printf 'POSTGRES_PASSWORD=%s\n' "$NEW_PASSWORD"
            else
                printf '%s\n' "$line"
            fi
            ;;
        *)
            printf '%s\n' "$line"
            ;;
    esac
done < "$SOURCE_FILE" > "$TEMP_FILE"

mv "$TEMP_FILE" "$ENV_FILE"

if [[ "$CREATING" -eq 1 ]]; then
    echo "Da tao file .env thanh cong."
    echo "Da sinh mat khau PostgreSQL ngau nhien."
else
    echo "Da cap nhat JWT_SECRET khong hop le."
    echo "Da giu nguyen cac bien moi truong khac."
fi

echo "JWT_SECRET: 64 ky tu hexadecimal."
echo "Khong hien thi gia tri bi mat."
echo "Khong commit file .env len Git."
