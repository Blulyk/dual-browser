$ErrorActionPreference = "Stop"

$toolsRoot = Join-Path $env:LOCALAPPDATA "DualBrowser"
$jdkHome = Join-Path $toolsRoot "jdk-21"
$studioJdk = "C:\Program Files\Android\Android Studio\jbr"

if (Test-Path (Join-Path $studioJdk "bin\java.exe")) {
    $jdkHome = $studioJdk
} elseif (-not (Test-Path (Join-Path $jdkHome "bin\java.exe"))) {
    $jdkArchive = Join-Path $env:TEMP "dual-browser-jdk-21.zip"
    $jdkExpanded = Join-Path $env:TEMP "dual-browser-jdk-21"
    if (-not (Test-Path $jdkArchive)) {
        & curl.exe -fL "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk" -o $jdkArchive
        if ($LASTEXITCODE -ne 0) { throw "JDK download failed" }
    }
    if (Test-Path $jdkExpanded) {
        $resolvedExpanded = (Resolve-Path $jdkExpanded).Path
        $resolvedTemp = (Resolve-Path $env:TEMP).Path
        if (-not $resolvedExpanded.StartsWith($resolvedTemp, [StringComparison]::OrdinalIgnoreCase)) {
            throw "Refusing to clean a JDK staging directory outside TEMP"
        }
        Remove-Item $resolvedExpanded -Recurse -Force
    }
    Expand-Archive $jdkArchive $jdkExpanded
    $expandedHome = Get-ChildItem $jdkExpanded -Directory | Select-Object -First 1
    New-Item -ItemType Directory -Force $toolsRoot | Out-Null
    Move-Item $expandedHome.FullName $jdkHome
}

$env:JAVA_HOME = $jdkHome
$env:Path = "$jdkHome\bin;$env:Path"

$sdkRoot = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$cmdline = Join-Path $sdkRoot "cmdline-tools\latest\bin\sdkmanager.bat"

if (-not (Test-Path $cmdline)) {
    $archive = Join-Path $env:TEMP "android-commandlinetools.zip"
    $expanded = Join-Path $env:TEMP "android-commandlinetools"
    if (-not (Test-Path $archive)) {
        & curl.exe -fL "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip" -o $archive
        if ($LASTEXITCODE -ne 0) { throw "Android command-line tools download failed" }
    }
    Remove-Item $expanded -Recurse -Force -ErrorAction SilentlyContinue
    Expand-Archive $archive $expanded
    New-Item -ItemType Directory -Force (Join-Path $sdkRoot "cmdline-tools\latest") | Out-Null
    Copy-Item (Join-Path $expanded "cmdline-tools\*") (Join-Path $sdkRoot "cmdline-tools\latest") -Recurse -Force
}

$yes = (1..200 | ForEach-Object { "y" }) -join "`n"
$yes | & $cmdline --sdk_root=$sdkRoot --licenses | Out-Null
& $cmdline --sdk_root=$sdkRoot "platform-tools" "platforms;android-36" "build-tools;36.0.0"

$sdkProperty = $sdkRoot.Replace("\", "/").Replace(":", "\:")
Set-Content -Path "local.properties" -Value "sdk.dir=$sdkProperty" -Encoding Ascii

if (-not (Test-Path "gradlew.bat")) {
    $gradleArchive = Join-Path $env:TEMP "gradle-8.13-bin.zip"
    $gradleHome = Join-Path $env:TEMP "gradle-8.13"
    if (-not (Test-Path $gradleHome)) {
        & curl.exe -fL "https://services.gradle.org/distributions/gradle-8.13-bin.zip" -o $gradleArchive
        if ($LASTEXITCODE -ne 0) { throw "Gradle download failed" }
        Expand-Archive $gradleArchive $env:TEMP -Force
    }
    & (Join-Path $gradleHome "bin\gradle.bat") wrapper --gradle-version 8.13
}

Write-Output "JAVA_HOME=$env:JAVA_HOME"
Write-Output "ANDROID_SDK_ROOT=$sdkRoot"
