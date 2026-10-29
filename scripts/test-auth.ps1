param([string]$BaseUrl = "http://localhost:8080")
$ErrorActionPreference = "Stop"
$BaseUrl = $BaseUrl.TrimEnd('/')
$script:passed = 0
$email = "manual-$([Guid]::NewGuid().ToString('N'))@example.com"
$password = "ManualTest123!"

function Send-Api([string]$Method, [string]$Path, $Body = $null, [string]$Token = "") {
    $headers = @{ Accept = "application/json" }
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $argsMap = @{ Uri = "$BaseUrl$Path"; Method = $Method; Headers = $headers; UseBasicParsing = $true }
    if ($null -ne $Body) {
        $argsMap.ContentType = "application/json; charset=utf-8"
        $argsMap.Body = [Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Compress))
    }
    try {
        $response = Invoke-WebRequest @argsMap
        $content = [string]$response.Content
        $parsed = $null
        if ($content) { $parsed = $content | ConvertFrom-Json }
        return @{ Status = [int]$response.StatusCode; Data = $parsed; Text = $content }
    } catch {
        if ($null -eq $_.Exception.Response) { throw }
        $status = [int]$_.Exception.Response.StatusCode
        $content = [string]$_.ErrorDetails.Message
        return @{ Status = $status; Data = $null; Text = $content }
    }
}
function Check([string]$Name, $Response, [int]$Expected) {
    if ($Response.Status -ne $Expected) { throw "FAIL $Name : expected HTTP $Expected, got $($Response.Status)" }
    $script:passed++
    Write-Host ("PASS {0:D2} {1} -> HTTP {2}" -f $script:passed, $Name, $Response.Status)
}

$body = @{ email = $email; password = $password; fullName = "Viet Khang"; phone = "0901234567" }
$registered = Send-Api POST /api/v1/auth/register $body
Check "Register candidate" $registered 201
if ($registered.Data.role -ne "ROLE_CANDIDATE") { throw "Server assigned an unexpected role" }
$login = Send-Api POST /api/v1/auth/login @{email=$email; password=$password}
Check "Login" $login 200
$token = $login.Data.accessToken
if (-not $token -or $login.Data.expiresIn -ne 86400) { throw "Expected JWT with default 24-hour lifetime" }
$me = Send-Api GET /api/v1/auth/me $null $token
Check "Current account" $me 200
if ($me.Data.id -ne $registered.Data.id -or $me.Data.phone -ne "0901234567") { throw "Current account/phone mismatch" }
Check "Duplicate email" (Send-Api POST /api/v1/auth/register $body) 409
Check "Case-insensitive login" (Send-Api POST /api/v1/auth/login @{email=$email.ToUpperInvariant(); password=$password}) 200
Check "Wrong password" (Send-Api POST /api/v1/auth/login @{email=$email; password="WrongPassword123!"}) 401
Check "Anonymous me" (Send-Api GET /api/v1/auth/me) 401
Check "Invalid JWT" (Send-Api GET /api/v1/auth/me $null "not-a-jwt") 401
Check "Unsigned JWT" (Send-Api GET /api/v1/auth/me $null "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ4In0.") 401
Check "Candidate blocked from admin" (Send-Api GET /api/v1/admin/users $null $token) 403
$elevated = @{email="admin-$email"; password=$password; fullName="Khang"; role="ROLE_ADMIN"}
Check "Role injection rejected" (Send-Api POST /api/v1/auth/register $elevated) 400
Check "Invalid email" (Send-Api POST /api/v1/auth/register @{email="bad-email"; password=$password; fullName="Khang"}) 400
$invalid = Send-Api POST /api/v1/auth/register @{email="short-$email"; password="short"; fullName="Khang"}
Check "Short password" $invalid 400
if ($invalid.Text -match '"rejectedValue"\s*:\s*"short"') { throw "Rejected password was leaked" }
$longUnicode = ([string][char]0x1EAD) * 25
Check "Password exceeding 72 UTF-8 bytes" (Send-Api POST /api/v1/auth/register @{email="unicode-$email"; password=$longUnicode; fullName="Khang"}) 400
$ownership = Send-Api GET '/api/v1/auth/me?userId=999999' $null $token
Check "Identity comes from token" $ownership 200
if ($ownership.Data.id -ne $registered.Data.id) { throw "Identity was controlled by a query parameter" }
foreach ($result in @($registered,$login,$me)) {
    if ($result.Text -match 'passwordHash|password_hash|"password"') { throw "Password field leaked in response" }
}
Write-Host "All $script:passed HTTP checks passed. Test account: $email"
Write-Host "The account remains in your development database; no delete/admin endpoint was added."
