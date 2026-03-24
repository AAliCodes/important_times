import os
import shutil

# 1. The exact path to the stubbornly broken file
manifest_path = "app/src/main/AndroidManifest.xml"

# 2. The completely clean, package-free manifest
clean_manifest = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <application>
        </application>

</manifest>
"""

# 3. Forcefully overwrite the file
try:
    with open(manifest_path, "w") as f:
        f.write(clean_manifest)
    print(f"✅ Successfully overwrote {manifest_path}")
except Exception as e:
    print(f"❌ Could not write to file: {e}")

# 4. Manually delete the Gradle build cache folders (The real culprit)
build_dir = "app/build"
if os.path.exists(build_dir):
    shutil.rmtree(build_dir)
    print("✅ Deleted the stubborn app/build cache directory!")
else:
    print("✅ No app/build cache directory found.")