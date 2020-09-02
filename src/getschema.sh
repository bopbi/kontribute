#!/bin/sh
TOKEN="5ff9ca6d9bcc1454cb87c7d6fedec353aebf5641"

./gradlew downloadApolloSchema \
  --endpoint="https://api.github.com/graphql" \
  --schema="src/main/kotlin/github/com/schema.json" \
  --header="Authorization: Bearer $TOKEN"