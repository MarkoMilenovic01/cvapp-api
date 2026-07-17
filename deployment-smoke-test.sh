#!/usr/bin/env bash

set -u

BASE_URL="${1:-${BASE_URL:-}}"
TEST_EMAIL="${TEST_EMAIL:-}"
TEST_PASSWORD="${TEST_PASSWORD:-}"
FRONTEND_URL="${FRONTEND_URL:-}"

if [[ -z "$BASE_URL" ]]; then
    echo "Usage: $0 https://your-api.example.com"
    echo "Optional: TEST_EMAIL, TEST_PASSWORD and FRONTEND_URL environment variables"
    exit 2
fi

for command in curl python3; do
    if ! command -v "$command" >/dev/null 2>&1; then
        echo "Missing required command: $command"
        exit 2
    fi
done

BASE_URL="${BASE_URL%/}"
BODY_FILE="$(mktemp)"
HEADER_FILE="$(mktemp)"
trap 'rm -f "$BODY_FILE" "$HEADER_FILE"' EXIT

PASSED=0
FAILED=0

pass() {
    PASSED=$((PASSED + 1))
    echo "PASS: $1"
}

fail() {
    FAILED=$((FAILED + 1))
    echo "FAIL: $1"
    if [[ -s "$BODY_FILE" ]]; then
        echo "Response:"
        sed -n '1,20p' "$BODY_FILE"
    fi
}

request() {
    curl --silent --show-error \
        --connect-timeout 15 \
        --max-time 60 \
        --retry 4 \
        --retry-delay 5 \
        --retry-all-errors \
        --output "$BODY_FILE" \
        --dump-header "$HEADER_FILE" \
        --write-out '%{http_code}' \
        "$@"
}

echo "Testing $BASE_URL"

STATUS="$(request "$BASE_URL/actuator/health")" || STATUS="curl-error"
if [[ "$STATUS" == "200" ]] && grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"' "$BODY_FILE"; then
    pass "health endpoint reports UP"
else
    fail "health endpoint (expected HTTP 200 with status UP, received $STATUS)"
fi

STATUS="$(request "$BASE_URL/api/admin/users")" || STATUS="curl-error"
if [[ "$STATUS" == "401" ]]; then
    pass "protected admin endpoint rejects anonymous requests"
else
    fail "anonymous security check (expected HTTP 401, received $STATUS)"
fi

STATUS="$(request \
    --request POST \
    --header 'Content-Type: application/json' \
    --data '{}' \
    "$BASE_URL/api/auth/login")" || STATUS="curl-error"
if [[ "$STATUS" == "400" ]]; then
    pass "public login endpoint is reachable and validates input"
else
    fail "login routing check (expected HTTP 400, received $STATUS)"
fi

if [[ -n "$TEST_EMAIL" || -n "$TEST_PASSWORD" ]]; then
    if [[ -z "$TEST_EMAIL" || -z "$TEST_PASSWORD" ]]; then
        fail "valid-login check requires both TEST_EMAIL and TEST_PASSWORD"
    else
        LOGIN_JSON="$(TEST_EMAIL="$TEST_EMAIL" TEST_PASSWORD="$TEST_PASSWORD" python3 -c \
            'import json, os; print(json.dumps({"email": os.environ["TEST_EMAIL"], "password": os.environ["TEST_PASSWORD"]}))')"

        STATUS="$(request \
            --request POST \
            --header 'Content-Type: application/json' \
            --data "$LOGIN_JSON" \
            "$BASE_URL/api/auth/login")" || STATUS="curl-error"

        TOKEN_PRESENT="$(python3 -c \
            'import json, sys
try:
    print("yes" if json.load(open(sys.argv[1])).get("accessToken") else "no")
except Exception:
    print("no")' "$BODY_FILE")"

        if [[ "$STATUS" == "200" && "$TOKEN_PRESENT" == "yes" ]]; then
            pass "valid credentials return an access token"
        else
            fail "valid-login check (expected HTTP 200 with accessToken, received $STATUS)"
        fi
    fi
else
    echo "SKIP: valid-login check (set TEST_EMAIL and TEST_PASSWORD)"
fi

if [[ -n "$FRONTEND_URL" ]]; then
    STATUS="$(request \
        --request OPTIONS \
        --header "Origin: $FRONTEND_URL" \
        --header 'Access-Control-Request-Method: POST' \
        "$BASE_URL/api/auth/login")" || STATUS="curl-error"

    ALLOWED_ORIGIN="$(awk -F': ' 'tolower($1) == "access-control-allow-origin" {gsub("\r", "", $2); print $2}' "$HEADER_FILE" | tail -n 1)"
    if [[ "$STATUS" == "200" && "$ALLOWED_ORIGIN" == "$FRONTEND_URL" ]]; then
        pass "CORS permits $FRONTEND_URL"
    else
        fail "CORS check for $FRONTEND_URL (HTTP $STATUS, allowed origin: ${ALLOWED_ORIGIN:-missing})"
    fi
else
    echo "SKIP: production CORS check (set FRONTEND_URL)"
fi

echo "Result: $PASSED passed, $FAILED failed"

if (( FAILED > 0 )); then
    exit 1
fi
