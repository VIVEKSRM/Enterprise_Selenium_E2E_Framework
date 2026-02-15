# Docker Parallel Execution Management Script (PowerShell)
# Usage: .\docker-parallel.ps1 [command]

param(
    [string]$Command = "help"
)

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$DockerComposeFile = Join-Path $ProjectRoot "docker" "docker-compose.yml"

# Colors for output
$InfoColor = "Cyan"
$SuccessColor = "Green"
$WarnColor = "Yellow"
$ErrorColor = "Red"

# Helper functions
function Log-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor $InfoColor
}

function Log-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor $SuccessColor
}

function Log-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor $WarnColor
}

function Log-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor $ErrorColor
}

# Check if Docker is installed
function Check-Docker {
    try {
        $dockerVersion = & docker --version 2>&1
        $dockerComposeVersion = & docker-compose --version 2>&1
        Log-Success "Docker and Docker Compose are installed"
    }
    catch {
        Log-Error "Docker or Docker Compose is not installed. Please install Docker first."
        exit 1
    }
}

# Start Docker containers
function Start-Docker {
    Log-Info "Starting Selenium Grid containers..."
    & docker-compose -f $DockerComposeFile up -d
    Log-Info "Waiting for containers to be ready..."
    Start-Sleep -Seconds 15

    # Check if hub is ready
    $status = & docker-compose -f $DockerComposeFile ps | Select-String "selenium-hub.*Up"
    if ($status) {
        Log-Success "Selenium Hub is ready"
    }
    else {
        Log-Error "Selenium Hub failed to start"
        & docker-compose -f $DockerComposeFile logs selenium-hub
        exit 1
    }

    Log-Success "All containers started successfully"
    Show-Status
}

# Stop Docker containers
function Stop-Docker {
    Log-Info "Stopping Selenium Grid containers..."
    & docker-compose -f $DockerComposeFile down
    Log-Success "Containers stopped"
}

# Show container status
function Show-Status {
    Log-Info "Container Status:"
    & docker-compose -f $DockerComposeFile ps

    Write-Host ""
    Log-Info "Node Details:"
    Log-Info "Selenium Hub Console: http://localhost:4444"
    Log-Info "VNC Access Points:"
    Log-Info "  Chrome Node 1: localhost:7900 (password: secret)"
    Log-Info "  Chrome Node 2: localhost:7901 (password: secret)"
    Log-Info "  Chrome Node 3: localhost:7902 (password: secret)"
    Log-Info "  Firefox Node 1: localhost:7903 (password: secret)"
    Log-Info "  Firefox Node 2: localhost:7904 (password: secret)"
}

# View logs
function Show-Logs {
    Log-Info "Showing Selenium Hub logs..."
    & docker-compose -f $DockerComposeFile logs -f selenium-hub
}

# Run parallel tests
function Run-Tests {
    param(
        [string]$TestSuite = "testng-docker-parallel.xml"
    )

    Log-Info "Running tests with suite: $TestSuite"

    $testFilePath = Join-Path $ProjectRoot $TestSuite
    if (-not (Test-Path $testFilePath)) {
        Log-Error "Test suite not found: $testFilePath"
        exit 1
    }

    Push-Location $ProjectRoot
    & mvn clean test -Dsuites="$TestSuite" -Dheadless=true
    Pop-Location
}

# Run all parallel tests
function Run-AllParallel {
    Log-Info "Running all parallel tests (class-level parallelization)..."
    Run-Tests "testng-docker-all-parallel.xml"
}

# Check grid status
function Get-GridStatus {
    Log-Info "Checking Selenium Grid status..."
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:4444/wd/hub/status" -UseBasicParsing
        $json = $response.Content | ConvertFrom-Json
        Write-Host ($json | ConvertTo-Json -Depth 10)
    }
    catch {
        Log-Error "Could not connect to Selenium Grid. Is it running?"
    }
}

# Restart containers
function Restart-Docker {
    Log-Warn "Restarting Docker containers..."
    Stop-Docker
    Start-Sleep -Seconds 3
    Start-Docker
}

# Show help
function Show-Help {
    $helpText = @"
Docker Parallel Execution Management Script

Usage: .\docker-parallel.ps1 [command]

Commands:
    start           Start Selenium Grid containers
    stop            Stop Selenium Grid containers
    status          Show container status
    logs            Show Selenium Hub logs
    test            Run parallel tests (testng-docker-parallel.xml)
    test-all        Run all parallel tests (testng-docker-all-parallel.xml)
    grid-status     Check Selenium Grid status
    restart         Restart all containers
    help            Show this help message

Examples:
    # Start Docker Grid
    .\docker-parallel.ps1 -Command start

    # Run tests
    .\docker-parallel.ps1 -Command test

    # Check status
    .\docker-parallel.ps1 -Command status

    # Stop containers
    .\docker-parallel.ps1 -Command stop

"@
    Write-Host $helpText
}

# Main script logic
switch ($Command.ToLower()) {
    "start" {
        Check-Docker
        Start-Docker
    }
    "stop" {
        Stop-Docker
    }
    "status" {
        Show-Status
    }
    "logs" {
        Show-Logs
    }
    "test" {
        Run-Tests "testng-docker-parallel.xml"
    }
    "test-all" {
        Run-AllParallel
    }
    "grid-status" {
        Get-GridStatus
    }
    "restart" {
        Restart-Docker
    }
    "help" {
        Show-Help
    }
    default {
        Log-Error "Unknown command: $Command"
        Show-Help
        exit 1
    }
}

