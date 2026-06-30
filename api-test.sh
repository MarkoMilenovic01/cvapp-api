#!/bin/bash

BASE_URL="http://localhost:8080"

echo "1) Logging in as admin..."

LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@cvapp.com","password":"password"}')

echo "$LOGIN_RESPONSE"

TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))")

if [ -z "$TOKEN" ]; then
  echo "Login failed. No access token received."
  exit 1
fi

echo ""
echo "2) Token received."
echo ""
echo "3) Testing admin companies endpoint..."

curl -i "$BASE_URL/api/admin/companies" \
  -H "Authorization: Bearer $TOKEN"

echo ""
echo "Done."
