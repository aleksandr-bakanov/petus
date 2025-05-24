#!/bin/zsh

babelish csv2android
babelish csv2strings

# Copy Android strings
echo "Copying Android strings"
for dir in values-*; do
  # Check if it's a directory
  if [ -d "$dir" ]; then
    echo "Copying $dir to ../androidApp/src/main/res/"
    cp -R "$dir" ../androidApp/src/main/res/
  fi
done
cp ../androidApp/src/main/res/values-en/strings.xml ../androidApp/src/main/res/values/strings.xml
rm -r ../androidApp/src/main/res/values-en

# Copy iOS strings
echo "Copying iOS strings"
for dir in *.lproj; do
  # Check if it's a directory
  if [ -d "$dir" ]; then
    echo "Copying $dir to ../iosApp/iosApp/"
    cp -R "$dir" ../iosApp/iosApp/
  fi
done

echo "Clean up"
rm -r *.lproj values-*

echo "✅ Done!"
