
# W3-T6 - Full Week 3 integration test
# Auth -> Profile Skills -> LMI Import -> Recommendations -> Skill Gap
# Only run on the local Docker development environment.

param(
    [string]$BaseUrl = 'http://localhost:8080'
)

$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
$script:Passed = 0
$script:DbUser = ''
$script:DbName = ''
$script:DbReady = $false

# Prevent this test from targeting a remote backend.
if ($BaseUrl -notmatch '^http://(localhost|127\.0\.0\.1):8080$') {
    throw 'Chi duoc chay test voi Backend local tai cong 8080.'
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        $Body = $null,
        [string]$Token = ''
    )

    $headers = @{ Accept = 'application/json' }

    if ($Token) {
        $headers.Authorization = "Bearer $Token"
    }

    $options = @{
        Uri = "$BaseUrl$Path"
        Method = $Method
        Headers = $headers
        UseBasicParsing = $true
        ErrorAction = 'Stop'
    }

    if ($null -ne $Body) {
        $options.ContentType = 'application/json; charset=utf-8'
        $json = ConvertTo-Json -InputObject $Body -Depth 10 -Compress
        $options.Body = [Text.Encoding]::UTF8.GetBytes($json)
    }

    try {
        $response = Invoke-WebRequest @options
        $status = [int]$response.StatusCode
        $content = [string]$response.Content
    }
    catch {
        if ($null -eq $_.Exception.Response) {
            throw "Khong ket noi duoc $Method $Path : $($_.Exception.Message)"
        }

        $status = [int]$_.Exception.Response.StatusCode
        $content = [string]$_.ErrorDetails.Message

        if (-not $content) {
            try {
                $stream = $_.Exception.Response.GetResponseStream()

                if ($stream) {
                    $reader = New-Object System.IO.StreamReader($stream)
                    $content = $reader.ReadToEnd()
                    $reader.Dispose()
                }
            }
            catch {
                $content = ''
            }
        }
    }

    $data = $null

    if ($content) {
        try {
            $data = ConvertFrom-Json -InputObject $content -ErrorAction Stop
        }
        catch {
            $data = $null
        }
    }

    return [PSCustomObject]@{
        Status = $status
        Data = $data
        Text = $content
    }
}

function Assert-Status {
    param(
        [string]$Name,
        $Result,
        [int]$Expected
    )

    if ($Result.Status -ne $Expected) {
        $sample = [string]$Result.Text

        if ($sample.Length -gt 250) {
            $sample = $sample.Substring(0, 250)
        }

        throw "FAIL $Name : expected HTTP $Expected, got $($Result.Status). $sample"
    }

    $script:Passed++
    Write-Host ("PASS {0:D2} - {1} (HTTP {2})" -f $script:Passed, $Name, $Expected)
}

function Assert-Condition {
    param(
        [string]$Name,
        [bool]$Ok
    )

    if (-not $Ok) {
        throw "FAIL $Name"
    }

    $script:Passed++
    Write-Host ("PASS {0:D2} - {1}" -f $script:Passed, $Name)
}

function Invoke-DbSql {
    param([string]$Sql)

    $output = @(
        & docker compose exec -T postgres psql `
            -X -qAt -v ON_ERROR_STOP=1 `
            -U $script:DbUser `
            -d $script:DbName `
            -c $Sql
    )

    if ($LASTEXITCODE -ne 0) {
        throw 'Lenh SQL kiem thu that bai.'
    }

    return ($output -join "`n").Trim()
}

Write-Host '===== W3-T6: FULL INTEGRATION TEST ====='
Write-Host "Backend: $BaseUrl"

# FIX: Read Docker output first, without Select-Object pipeline.
# Check the exit code immediately after the native Docker command.

$dbUserOutput = @(docker compose exec -T postgres printenv POSTGRES_USER)

if ($LASTEXITCODE -ne 0 -or $dbUserOutput.Count -eq 0) {
    throw 'Khong doc duoc POSTGRES_USER tu Docker.'
}

$script:DbUser = ([string]$dbUserOutput[0]).Trim()

$dbNameOutput = @(docker compose exec -T postgres printenv POSTGRES_DB)

if ($LASTEXITCODE -ne 0 -or $dbNameOutput.Count -eq 0) {
    throw 'Khong doc duoc POSTGRES_DB tu Docker.'
}

$script:DbName = ([string]$dbNameOutput[0]).Trim()

if ([string]::IsNullOrWhiteSpace($script:DbUser) -or
    [string]::IsNullOrWhiteSpace($script:DbName)) {
    throw 'Thieu thong tin PostgreSQL.'
}

Write-Host "PostgreSQL user: $script:DbUser"
Write-Host "PostgreSQL database: $script:DbName"

$dbCheck = Invoke-DbSql 'SELECT 1;'

if ($dbCheck -ne '1') {
    throw 'Khong truy van duoc PostgreSQL.'
}

$script:DbReady = $true
Write-Host 'PostgreSQL connection: OK'

# Unique temporary candidate, admin identity and test region.
$id = [Guid]::NewGuid().ToString('N')
$email = "w3-candidate-$id@example.invalid"
$adminEmail = "w3-admin-$id@example.invalid"
$password = 'W3!' + $id.Substring(0, 16) + 'aA9'
$region = 'W3T6' + $id.Substring(0, 8).ToUpperInvariant()
$snapshotDate = '2026-10-01'

try {
    Write-Host '===== STAGE 1: AUTH, PROFILE AND TAXONOMY ====='

    $health = Invoke-Api 'GET' '/api/health'
    Assert-Status 'Backend health' $health 200
    Assert-Condition 'Health UP' ($health.Data.status -eq 'UP')

    $skills = Invoke-Api 'GET' '/api/v1/skills?query=Java'
    Assert-Status 'Public skills dictionary' $skills 200

    $javaSkill = @(
        $skills.Data | Where-Object { $_.code -eq 'SK-JAVA' }
    ) | Select-Object -First 1

    Assert-Condition 'Java skill exists' (
        $null -ne $javaSkill -and [long]$javaSkill.id -gt 0
    )

    $occupations = Invoke-Api 'GET' '/api/v1/occupations?page=0&size=20'
    Assert-Status 'Public occupations' $occupations 200

    Assert-Condition 'At least 10 occupations' (
        @($occupations.Data.content).Count -ge 10
    )

    $targetOccupation = @(
        $occupations.Data.content |
            Where-Object { $_.code -eq 'SOC-15-1252' }
    ) | Select-Object -First 1

    Assert-Condition 'Target occupation exists' (
        $null -ne $targetOccupation -and
        [long]$targetOccupation.id -gt 0
    )

    $register = Invoke-Api 'POST' '/api/v1/auth/register' @{
        email = $email
        password = $password
        fullName = 'W3 Test Candidate'
    }
    Assert-Status 'Register candidate' $register 201

    $login = Invoke-Api 'POST' '/api/v1/auth/login' @{
        email = $email
        password = $password
    }
    Assert-Status 'Login candidate' $login 200

    $token = [string]$login.Data.accessToken

    Assert-Condition 'Candidate JWT returned' (
        -not [string]::IsNullOrWhiteSpace($token)
    )

    $me = Invoke-Api 'GET' '/api/v1/auth/me' $null $token
    Assert-Status 'Current candidate identity' $me 200

    Assert-Condition 'Candidate identity matches' (
        $me.Data.email -eq $email
    )

    $profile = Invoke-Api 'PUT' '/api/v1/profile' @{
        headline = 'Junior Java Developer'
        yearsOfExperience = 1
        preferredLocation = $region
        workMode = 'HYBRID'
        targetIndustry = 'IT'
        expectedSalaryMin = 12000000
        expectedSalaryMax = 20000000
    } $token

    Assert-Status 'Create candidate profile' $profile 200

    $add = Invoke-Api 'POST' '/api/v1/profile/skills' @{
        skillId = [long]$javaSkill.id
        proficiencyLevel = 'INTERMEDIATE'
        yearsExperience = 1.0
    } $token

    Assert-Status 'Add Java skill' $add 201

    $getSkills = Invoke-Api 'GET' '/api/v1/profile/skills' $null $token
    Assert-Status 'List candidate skills' $getSkills 200

    Assert-Condition 'Candidate has Java skill' (
        @($getSkills.Data | Where-Object {
            $_.skillCode -eq 'SK-JAVA'
        }).Count -eq 1
    )

    $duplicate = Invoke-Api 'POST' '/api/v1/profile/skills' @{
        skillId = [long]$javaSkill.id
        proficiencyLevel = 'INTERMEDIATE'
        yearsExperience = 1.0
    } $token

    Assert-Status 'Reject duplicate skill' $duplicate 409

    $update = Invoke-Api 'PUT' "/api/v1/profile/skills/$($javaSkill.id)" @{
        proficiencyLevel = 'ADVANCED'
        yearsExperience = 2.0
    } $token

    Assert-Status 'Update Java skill' $update 200

    Assert-Condition 'Java proficiency updated' (
        $update.Data.proficiencyLevel -eq 'ADVANCED'
    )

    Write-Host '===== STAGE 2: LMI IMPORT ====='

    # LMI import requires ROLE_ADMIN.
    # Create and promote a disposable local database user.
    # Login again to obtain an ADMIN JWT.

    $adminRegister = Invoke-Api 'POST' '/api/v1/auth/register' @{
        email = $adminEmail
        password = $password
        fullName = 'W3 Temporary Admin'
    }

    Assert-Status 'Register temporary admin identity' $adminRegister 201

    $promotedId = Invoke-DbSql (
        "UPDATE users SET role = 'ROLE_ADMIN' " +
        "WHERE email = '$adminEmail' AND role = 'ROLE_CANDIDATE' " +
        "RETURNING id;"
    )

    Assert-Condition 'Temporary admin promoted in local DB' (
        $promotedId -match '^\d+$'
    )

    $adminLogin = Invoke-Api 'POST' '/api/v1/auth/login' @{
        email = $adminEmail
        password = $password
    }

    Assert-Status 'Login temporary admin' $adminLogin 200

    $adminToken = [string]$adminLogin.Data.accessToken

    Assert-Condition 'Admin JWT returned' (
        -not [string]::IsNullOrWhiteSpace($adminToken) -and
        $adminLogin.Data.user.role -eq 'ROLE_ADMIN'
    )

    $lmiItem = @{
        occupationCode = 'SOC-15-1252'
        region = $region
        demandScore = 97.3
        growthRatePct = 17.5
        openJobPostingsCount = 123
        competitiveIndex = 2.3
        snapshotDate = $snapshotDate
    }

    $lmiRequest = @{
        items = @($lmiItem)
    }

    $unauthorized = Invoke-Api 'POST' '/api/v1/admin/lmi/import' $lmiRequest

    Assert-Status 'LMI import requires authentication' $unauthorized 401

    $forbidden = Invoke-Api 'POST' '/api/v1/admin/lmi/import' $lmiRequest $token

    Assert-Status 'Candidate cannot import LMI' $forbidden 403

    $import = Invoke-Api 'POST' '/api/v1/admin/lmi/import' $lmiRequest $adminToken

    Assert-Status 'Admin imports LMI' $import 200

    Assert-Condition 'LMI record inserted' (
        [int]$import.Data.totalRecordsProcessed -eq 1 -and
        [int]$import.Data.recordsInserted -eq 1
    )

    $storedScore = Invoke-DbSql (
        "SELECT demand_score FROM market_demand_indicators " +
        "WHERE region = '$region' " +
        "AND snapshot_date = DATE '$snapshotDate' " +
        "AND occupation_id = " +
        "(SELECT id FROM occupations WHERE code = 'SOC-15-1252');"
    )

    Assert-Condition 'Imported LMI persisted in PostgreSQL' (
        [math]::Abs([double]$storedScore - 97.3) -lt 0.01
    )

    # Import the same natural key with a different score.
    $lmiItem.demandScore = 92.2

    $upsert = Invoke-Api 'POST' '/api/v1/admin/lmi/import' $lmiRequest $adminToken

    Assert-Status 'Admin upserts existing LMI' $upsert 200

    Assert-Condition 'LMI record updated rather than duplicated' (
        [int]$upsert.Data.totalRecordsProcessed -eq 1 -and
        [int]$upsert.Data.recordsUpdated -eq 1
    )

    $updatedScore = Invoke-DbSql (
        "SELECT demand_score FROM market_demand_indicators " +
        "WHERE region = '$region' " +
        "AND snapshot_date = DATE '$snapshotDate' " +
        "AND occupation_id = " +
        "(SELECT id FROM occupations WHERE code = 'SOC-15-1252');"
    )

    Assert-Condition 'Upsert value persisted in PostgreSQL' (
        [math]::Abs([double]$updatedScore - 92.2) -lt 0.01
    )

    Write-Host '===== STAGE 3: RECOMMENDATIONS ====='

    $recommendations = Invoke-Api 'GET' '/api/v1/recommendations?limit=50' $null $token

    Assert-Status 'Get personalized recommendations' $recommendations 200

    Assert-Condition 'Recommendation list is not empty' (
        @($recommendations.Data).Count -ge 1
    )

    $targetRecommendation = @(
        $recommendations.Data |
            Where-Object { $_.occupationCode -eq 'SOC-15-1252' }
    ) | Select-Object -First 1

    Assert-Condition 'Target occupation is recommended' (
        $null -ne $targetRecommendation -and
        [long]$targetRecommendation.occupationId -eq [long]$targetOccupation.id
    )

    Assert-Condition 'Recommendation uses newly imported LMI' (
        [math]::Abs(
            [double]$targetRecommendation.scoreBreakdown.demandScore - 92.2
        ) -lt 0.05
    )

    Assert-Condition 'Recommendation score is valid' (
        [double]$targetRecommendation.finalScore -ge 0 -and
        [double]$targetRecommendation.finalScore -le 100
    )

    Write-Host '===== STAGE 4: SKILL GAP ====='

    $gap = Invoke-Api 'GET' (
        "/api/v1/recommendations/$($targetOccupation.id)/skill-gap"
    ) $null $token

    Assert-Status 'Get occupation skill gap' $gap 200

    Assert-Condition 'Skill gap occupation matches' (
        [long]$gap.Data.occupationId -eq [long]$targetOccupation.id
    )

    Assert-Condition 'Skill gap percentage is valid' (
        [double]$gap.Data.matchPercentage -ge 0 -and
        [double]$gap.Data.matchPercentage -le 100
    )

    $gapFields = @($gap.Data.PSObject.Properties.Name)

    Assert-Condition 'Skill gap provides expected categories' (
        'matchedSkills' -in $gapFields -and
        'missingEssentialSkills' -in $gapFields -and
        'missingOptionalSkills' -in $gapFields
    )

    Write-Host '===== STAGE 5: NEGATIVE TEST ====='

    $deleteSkill = Invoke-Api 'DELETE' (
        "/api/v1/profile/skills/$($javaSkill.id)"
    ) $null $token

    Assert-Status 'Delete Java skill' $deleteSkill 204

    $noSkills = Invoke-Api 'GET' '/api/v1/recommendations?limit=10' $null $token

    Assert-Status 'Recommendations reject candidate without skills' $noSkills 400

    Write-Host "===== FULL W3-T6 PASSED: $script:Passed CHECKS ====="
}
finally {
    # Clean up only randomly named records created by this test.
    if ($script:DbReady) {
        Write-Host '===== CLEANUP TEMPORARY TEST DATA ====='

        try {
            Invoke-DbSql (
                "DELETE FROM market_demand_indicators " +
                "WHERE region = '$region' " +
                "AND snapshot_date = DATE '$snapshotDate' " +
                "AND occupation_id = " +
                "(SELECT id FROM occupations WHERE code = 'SOC-15-1252');"
            ) | Out-Null

            Invoke-DbSql (
                "DELETE FROM users " +
                "WHERE email IN ('$email', '$adminEmail');"
            ) | Out-Null

            Write-Host 'Temporary test data cleaned.'
        }
        catch {
            Write-Warning "Cleanup chua hoan tat: $($_.Exception.Message)"
        }
    }
}
