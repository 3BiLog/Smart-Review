#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
PROJECT_NAME="smartreview-payment"

# Keys needed by the payment API (skip VITE_* frontend vars)
ENV_KEYS=(
  PAYOS_CLIENT_ID
  PAYOS_API_KEY
  PAYOS_CHECKSUM_KEY
  FIREBASE_PROJECT_ID
  FIREBASE_CLIENT_EMAIL
  FIREBASE_PRIVATE_KEY
  API_BASE_URL
  APP_RETURN_URL
)

get_env_value() {
  local key="$1"
  local line
  line=$(grep -E "^${key}=" .env.local | tail -n 1 || true)
  if [ -z "$line" ]; then
    return 1
  fi
  printf '%s' "${line#*=}"
}

push_env() {
  local key="$1"
  local value="$2"
  local env_name="$3"
  printf '%s' "$value" | vercel env add "$key" "$env_name" --force --yes 2>/dev/null || \
    printf '%s' "$value" | vercel env add "$key" "$env_name" --force
}

echo "==> Linking Vercel project: $PROJECT_NAME"
vercel link --yes --project "$PROJECT_NAME" 2>/dev/null || \
  vercel link --yes --project "$PROJECT_NAME" --name "$PROJECT_NAME"

echo "==> Uploading environment variables"
for key in "${ENV_KEYS[@]}"; do
  value="$(get_env_value "$key")" || continue
  echo "  - $key"
  push_env "$key" "$value" production
  push_env "$key" "$value" preview
  push_env "$key" "$value" development
done

echo "==> Deploying to production"
vercel deploy --prod --yes

echo "==> Done"
