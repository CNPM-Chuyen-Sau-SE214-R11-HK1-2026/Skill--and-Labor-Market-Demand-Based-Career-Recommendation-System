# W3-T6 - Stage 1 API integration smoke test (Auth -> Profile Skills -> Taxonomy)
# Full W3 end-to-end (LMI Import -> Recommendations -> Skill Gap) will be
# added after W3-T1 and W3-T3 APIs/DTOs are merged. Do not treat this as full E2E.
param(
    [string]$BaseUrl = 'http://localhost:8080'
)

$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
$script:passed = 0

function Invoke-Api([string]$Method, [string]$Path, $Body = $null, [string]$Token = '') {
    $headers = @{ Accept = 'application/json' }
    if ($Token) { $headers.Authorization = "Bearer $Token" }

    $options = @{
        Uri = "$BaseUrl$Path"
        Method = $Method
        Headers = $headers
        UseBasicParsing = $true
        ErrorAction = 'Stop'
    }
    if ($null -ne $Body) {
        $options.ContentType = 'application/json; charset=utf-8'
        $options.Body = [Text.Encoding]::UTF8.GetBytes((ConvertTo-Json -InputObject $Body -Depth 10 -Compress))
    }

    try {
        $response = Invoke-WebRequest @options
        $status = [int]$response.StatusCode
        $content = [string]$response.Content
    } catch {
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
            } catch { $content = '' }
        }
    }
    $data = $null
    if ($content) {
        try { $data = ConvertFrom-Json -InputObject $content -ErrorAction Stop }
        catch { $data = $null }
    }
    return [PSCustomObject]@{ Status = $status; Data = $data; Text = $content }
}

function Assert-Status([string]$Name, $Result, [int]$Expected) {
    if ($Result.Status -ne $Expected) {
        $sample = $Result.Text
        if ($sample.Length -gt 250) { $sample = $sample.Substring(0, 250) }
        throw "FAIL $Name : expected HTTP $Expected, got $($Result.Status). $sample"
    }
    $script:passed++
    Write-Host ("PASS {0:D2} - {1} (HTTP {2})" -f $script:passed, $Name, $Expected)
}

function Assert-Condition([string]$Name, [bool]$Ok) {
    if (-not $Ok) { throw "FAIL $Name" }
    $script:passed++
    Write-Host ("PASS {0:D2} - {1}" -f $script:passed, $Name)
}

Write-Host '===== W3-T6: STAGE 1 (AVAILABLE APIS) ====='
Write-Host "Backend: $BaseUrl"
Write-Host 'Stage 1 checks Auth, Profile Skills and Taxonomy.'
Write-Host 'LMI Import, Recommendations and Skill Gap are NOT yet tested.'

$health = Invoke-Api 'GET' '/api/health'
Assert-Status 'Backend health' $health 200
Assert-Condition 'Health UP' ($null -ne $health.Data -and $health.Data.status -eq 'UP')

$skills = Invoke-Api 'GET' '/api/v1/skills?query=Java'
Assert-Status 'Public skills dictionary' $skills 200
$javaSkill = @($skills.Data | Where-Object { $_.code -eq 'SK-JAVA' }) | Select-Object -First 1
Assert-Condition 'Seed V3: SK-JAVA is available' ($null -ne $javaSkill -and [long]$javaSkill.id -gt 0)

$occupations = Invoke-Api 'GET' '/api/v1/occupations?page=0&size=20'
Assert-Status 'Public occupations' $occupations 200
Assert-Condition 'Seed occupations are available' ($null -ne $occupations.Data -and @($occupations.Data.content).Count -ge 10)

$id = [Guid]::NewGuid().ToString('N')
$email = "w3-smoke-$id@example.invalid"
$password = 'W3!' + $id.Substring(0, 16) + 'aA9'
$register = Invoke-Api 'POST' '/api/v1/auth/register' @{
    email = $email
    password = $password
    fullName = 'W3 API Smoke Candidate'
}
Assert-Status 'Register test candidate' $register 201

$login = Invoke-Api 'POST' '/api/v1/auth/login' @{ email = $email; password = $password }
Assert-Status 'Login test candidate' $login 200
$token = [string]$login.Data.accessToken
Assert-Condition 'JWT accessToken is returned' (-not [string]::IsNullOrWhiteSpace($token))

$me = Invoke-Api 'GET' '/api/v1/auth/me' $null $token
Assert-Status 'Current candidate identity' $me 200
Assert-Condition 'JWT identifies the test candidate' ($me.Data.email -eq $email)

$profile = Invoke-Api 'PUT' '/api/v1/profile' @{
    headline = 'Junior Java Developer'
    yearsOfExperience = 1
    preferredLocation = 'HCMC'
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
$hasJava = @($getSkills.Data | Where-Object { $_.skillCode -eq 'SK-JAVA' }).Count -eq 1
Assert-Condition 'Candidate owns added Java skill' $hasJava

Assert-Status 'Duplicate skill rejected' (
    Invoke-Api 'POST' '/api/v1/profile/skills' @{
        skillId = [long]$javaSkill.id
        proficiencyLevel = 'INTERMEDIATE'
        yearsExperience = 1.0
    } $token
) 409

$update = Invoke-Api 'PUT' "/api/v1/profile/skills/$($javaSkill.id)" @{
    proficiencyLevel = 'ADVANCED'
    yearsExperience = 2.0
} $token
Assert-Status 'Update Java skill' $update 200
Assert-Condition 'Proficiency updated' ($update.Data.proficiencyLevel -eq 'ADVANCED')

$delete = Invoke-Api 'DELETE' "/api/v1/profile/skills/$($javaSkill.id)" $null $token
Assert-Status 'Delete Java skill' $delete 204

Write-Host "Stage 1 PASSED: $script:passed checks."
Write-Host 'A disposable test account was created in the local database.'
Write-Host 'Full W3 end-to-end and CI integration remain pending.'
