$ErrorActionPreference = "Stop"

$toolchainPath = Join-Path $PSScriptRoot "..\..\work\android-toolchain"
if (Test-Path -LiteralPath $toolchainPath) {
    $toolchainRoot = (Resolve-Path -LiteralPath $toolchainPath).Path
    $jdkRoot = Join-Path $toolchainRoot "jdk"
    $jdk = Get-ChildItem -LiteralPath $jdkRoot -Directory | Select-Object -First 1
    $sdkRoot = Join-Path $toolchainRoot "android-sdk"
    if ($jdk) { $javaHome = $jdk.FullName }
} else {
    $javaHome = $env:JAVA_HOME
    $sdkRoot = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { $env:ANDROID_SDK_ROOT }
}

if (-not $javaHome -or -not (Test-Path -LiteralPath (Join-Path $javaHome "bin\java.exe"))) {
    throw "JDK 17 tidak ditemukan. Atur JAVA_HOME ke instalasi JDK 17."
}
if (-not $sdkRoot -or -not (Test-Path -LiteralPath (Join-Path $sdkRoot "platforms\android-35\android.jar"))) {
    throw "Android SDK Platform 35 tidak ditemukan. Atur ANDROID_HOME ke instalasi Android SDK."
}

$env:JAVA_HOME = $javaHome
$env:ANDROID_HOME = $sdkRoot
$env:ANDROID_SDK_ROOT = $sdkRoot
$env:PATH = (Join-Path $env:JAVA_HOME "bin") + ";" + (Join-Path $sdkRoot "platform-tools") + ";" + $env:PATH

& (Join-Path $PSScriptRoot "gradlew.bat") -p $PSScriptRoot assembleDebug --no-daemon
exit $LASTEXITCODE

