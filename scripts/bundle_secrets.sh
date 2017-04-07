#!/usr/bin/env bash

mv ../secrets.zip.enc ../secrets.zip.enc.`date +"%m-%d-%y"`.bak
zip -r ../secrets.zip ../src/main/resources/application-staging.properties ../src/main/resources/application-production.properties
travis encrypt-file ../secrets.zip
mv secrets.zip.enc ..
echo ""
echo "secrets.zip created. Make sure  to update the decryption line in ../deploy.sh"
