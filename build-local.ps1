$ErrorActionPreference = "Stop"

$toolchainRoot = Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..\work\android-toolchain")
$jdkRoot = Join-Path $toolchainRoot "jdk"
$jdk = Get-ChildItem -LiteralPath $jdkRoot -Directory | Select-Object -First 1
$sdkRoot = Join-Path $toolchainRoot "android-sdk"

if (-not $jdk -or -not (Test-Path -LiteralPath (Join-Path $jdk.FullName "bin\java.exe"))) {
    throw "JDK 17 lokal tidak ditemukan di $jdkRoot"
}
if (-not (Test-Path -LiteralPath (Join-Path $sdkRoot "platforms\android-35\android.jar"))) {
    throw "Android SDK Platform 35 lokal tidak ditemukan di $sdkRoot"
}

$env:JAVA_HOME = $jdk.FullName
$env:ANDROID_HOME = $sdkRoot
$env:ANDROID_SDK_ROOT = $sdkRoot
$env:PATH = (Join-Path $env:JAVA_HOME "bin") + ";" + (Join-Path $sdkRoot "platform-tools") + ";" + $env:PATH

& (Join-Path $PSScriptRoot "gradlew.bat") -p $PSScriptRoot assembleDebug --no-daemon
exit $LASTEXITCODE

