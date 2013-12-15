#!/bin/bash
rm -rf bin2
ant release
adb install -r bin2/mongo-explorer-release.apk
