
# W2-T6 - Khoi tao bien moi truong an toan
# Khong ghi de mat khau PostgreSQL da ton tai.

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$template = Join-Path $root ".env.example"
$envFile = Join-Path $root ".env"

function New-RandomHex {
    param([int]$ByteCount)

    $bytes = New-Object byte[] $ByteCount
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()

    try {
        $rng.GetBytes($bytes)
    }
    finally {
        $rng.Dispose()
    }

    return [BitConverter]::ToString($bytes).Replace("-", "").ToLowerInvariant()
}

if (-not (Test-Path -LiteralPath $template)) {
    throw "Khong tim thay file .env.example tai thu muc goc."
}

$creating = -not (Test-Path -LiteralPath $envFile)

if ($creating) {
    $lines = @(Get-Content -LiteralPath $template -Encoding UTF8)
}
else {
    $lines = @(Get-Content -LiteralPath $envFile -Encoding UTF8)
}

$jwtLines = @($lines | Where-Object { $_ -match '^JWT_SECRET=' })

if ($jwtLines.Count -ne 1) {
    throw "File moi truong phai co dung mot dong JWT_SECRET."
}

$currentSecret = $jwtLines[0].Substring("JWT_SECRET=".Length)

$invalidSecret = (
    [string]::IsNullOrWhiteSpace($currentSecret) -or
    $currentSecret -match '^(your_|default_)' -or
    [Text.Encoding]::UTF8.GetByteCount($currentSecret) -lt 32
)

$changeJwt = $creating -or $invalidSecret

if (-not $creating -and -not $changeJwt) {
    Write-Host "File .env da ton tai va JWT_SECRET hop le."
    Write-Host "Khong thay doi cau hinh hien tai."
    exit 0
}

if ($changeJwt) {
    # 32 byte ngau nhien -> 64 ky tu hexadecimal.
    $newJwtSecret = New-RandomHex -ByteCount 32
}

if ($creating) {
    # Mat khau PostgreSQL rieng cho moi truong moi.
    $passwordLines = @(
        $lines | Where-Object { $_ -match '^POSTGRES_PASSWORD=' }
    )

    if ($passwordLines.Count -ne 1) {
        throw "File mau phai co dung mot dong POSTGRES_PASSWORD."
    }

    $newDbPassword = New-RandomHex -ByteCount 24
}

$updatedLines = @(
    foreach ($line in $lines) {
        if ($line -match '^JWT_SECRET=' -and $changeJwt) {
            "JWT_SECRET=$newJwtSecret"
        }
        elseif ($line -match '^POSTGRES_PASSWORD=' -and $creating) {
            "POSTGRES_PASSWORD=$newDbPassword"
        }
        else {
            $line
        }
    }
)

$utf8 = [System.Text.UTF8Encoding]::new($false)
[System.IO.File]::WriteAllLines(
    $envFile,
    [string[]]$updatedLines,
    $utf8
)

if ($creating) {
    Write-Host "Da tao file .env thanh cong."
    Write-Host "Da sinh mat khau PostgreSQL ngau nhien."
}
else {
    Write-Host "Da thay the JWT_SECRET mau/khong hop le."
    Write-Host "Giu nguyen cac bien moi truong khac."
}

Write-Host "JWT_SECRET hop le: 64 ky tu."
Write-Host "Khong hien thi gia tri bi mat."
Write-Host "Khong commit file .env len Git."
