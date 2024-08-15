
cd ../../../
$currentPath = $PWD.Path
Write-Host "Current directory: $currentPath"
# Define the URL of the MSI file
$nodeUrl = "https://nodejs.org/dist/v20.10.0/node-v20.10.0-x64.msi"
$nodeAppName="node.exe"

$sbtUrl = "https://github.com/sbt/sbt/releases/download/v1.9.7/sbt-1.9.7.msi"
$sbtAppName="sbt.exe"

# $javaUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.8.1+1/OpenJDK17U-jdk_x64_windows_hotspot_17.0.8.1_1.msi"
$javaUrl = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.1+12/OpenJDK21U-jdk_x64_windows_hotspot_21.0.1_12.msi"
$javaAppName="java.exe"


# Define the local path where you want to save the MSI file


# Start WSL in a new window
# Start-Process wsl.exe -WindowStyle Normal
# Wait for WSL to start

# Display menu options
Write-Host "Select an option for local deployment:"
Write-Host "1. Deploy JeMPI (For Fresh Start)"
Write-Host "2. Build and Reboot."
Write-Host "3. Restart JeMPI."
Write-Host "4. Stop JeMPI."
Write-Host "5. Backup Postgres & Dgraph."
Write-Host "6. Restore Postgres & Dgraph."
Write-Host "7. Destroy JeMPI (This process will wipe all data)."
Write-Host "8. Install Prerequisites."

# Get user input
$choice = Read-Host "Enter the number of your choice"
$wslPath = wsl.exe pwd
Write-Host "Path in WSL:- $wslPath"


function  installApp() {
    param (
        [string]$url,
        [string]$appName
    )

    $localPath = "$env:USERPROFILE\Downloads\$appName"
    Write-Host "$appName is Downloaded at:- $localPath"

    # Download the MSI file
    Invoke-WebRequest -Uri $url -OutFile $localPath

    # Install the MSI file
    # Start-Process -FilePath $appName -ArgumentList "/i", "$localPath", "/quiet" -Wait

    Remove-Item -Path  $localPath
}

function Restore-DgraphDB {
    Write-Host "Are you sure you want to restore the Dgraph database? (yes/no)"
    $dgraphConfirmation = Read-Host

    if ($dgraphConfirmation.ToLower() -eq "yes" -or $dgraphConfirmation.ToLower() -eq "y") {
        Push-Location $currentPath/devops/windows/deployment/backup_restore/
            Write-Host "Starting Dgraph database restore..."
            Start-Process -FilePath "bash" -ArgumentList "dgraph-restore.sh" -NoNewWindow -Wait
            Write-Host "Database Dgraph restore completed."
        Pop-Location 
    }
    else {
        Write-Host "Dgraph Database restore cancelled. Moving ahead without restore."
        # Continue with the rest of your script
    }
}



function Restore-PostgresDB {
    Write-Host "Are you sure you want to restore the Postgres database? (yes/no)"
    $postgresConfirmation = Read-Host

    if ($postgresConfirmation.ToLower() -eq "yes" -or $postgresConfirmation.ToLower() -eq "y") {
        Push-Location $currentPath/devops/windows/deployment/backup_restore/
            Write-Host "Starting Postgres database restore..."
            Start-Process -FilePath "bash" -ArgumentList "postgres-restore.sh" -NoNewWindow -Wait
            Write-Host "Database Postgres restore completed."
        Pop-Location 
    }
    else {
        Write-Host "Postgres Database restore cancelled. Moving ahead without restore."
        # Continue with the rest of your script
    }
}


# Process the user's choice
switch ($choice) {
    '1' {
        Write-Host $wslPath
        Write-Host "Deploying JeMPI "
        wsl -d Ubuntu $wslPath/devops/windows/deployment/deploy-local-wsl.sh  -Wait
        Start-Sleep -Seconds 30

        Push-Location $currentPath/JeMPI_Apps/JeMPI_Configuration
            Write-Host "Current directory: $PWD.path"
            .\create.ps1 reference\config-reference.json
        Pop-Location

        Push-Location $currentPath/devops/windows/run--base-docker-wsl
            Write-Host "start-with-bootstraper.ps1"
            # .\bootstrapper.ps1 -Wait
            .\start-with-bootstraper.ps1 -Wait

            Write-Host "Script completed."
            Write-Host "Running file: start-ui.ps1"
            .\start-ui.ps1 -Wait
        Pop-Location
        Write-Host "Script completed."
    }
    '2' {
        Write-Host "Build and Reboot"
        wsl -d Ubuntu $wslPath/devops/windows/deployment/deploy-local-wsl.sh
        Push-Location $currentPath/JeMPI_Apps/JeMPI_Configuration
            .\create.ps1 reference\config-reference.json
        Pop-Location

        Push-Location $currentPath/devops/windows/run--base-docker-wsl
            Write-Host "start-with-bootstraper.ps1"
            .\start.ps1 -Wait

            Write-Host "Script completed."
            Write-Host "Running file: start-ui.ps1"
            .\start-ui.ps1 -Wait

            Write-Host "Script completed."
        Pop-Location
    }
    '3' {
        Push-Location $currentPath/devops/windows/run--base-docker-wsl
            Write-Host "Down the JeMPI."
            Write-Host "Running file: stop.ps1"
            .\stop.ps1 -Wait
            Write-Host "Build and Reboot"
            Push-Location $currentPath/devops/windows/base-docker-wsl
                wsl -d Ubuntu $wslPath/devops/windows/base-docker-wsl/z-stack-2-reboot-hub-images.sh
            Pop-Location

            Write-Host "Running file: start.ps1"
            .\start.ps1 -Wait

            Write-Host "Script completed."
            Write-Host "Running file: start-ui.ps1"
            .\start-ui.ps1 -Wait

            Write-Host "Script completed."
        Pop-Location
    }
    '4' {
        Push-Location $currentPath/devops/windows/run--base-docker-wsl
            Write-Host "Down the JeMPI."
            Write-Host "Running file: stop.ps1"
            .\stop.ps1 -Wait
        Pop-Location
    }
    '5' {
        Write-Host "Database Backup."
        Push-Location $currentPath/devops/windows/deployment/backup_restore/
           wsl -d Ubuntu $wslPath/devops/windows/deployment/backup_restore/dgraph-backup.sh
           wsl -d Ubuntu $wslPath/devops/windows/deployment/backup_restore/postgres-backup.sh
        Pop-Location
        
    }
    '6' {
        Write-Host "Restore Backup."
        Restore-PostgresDB
        Restore-DgraphDB
    }
    '7' {
        $confirmation = Read-Host "Are you sure, Do you want to Destroy? (Ctrl+Y for Yes, any other key for No)"
        if ($confirmation -eq [char]25) {
            # Proceed with the script logic
            Write-Host "Proceeding with the script..."
            Write-Host "Down the JeMPI."
            Write-Host "Running file: stop.ps1"
            Push-Location $currentPath/devops/windows/run--base-docker-wsl
                .\stop.ps1 -Wait  
            Pop-Location
            wsl -d Ubuntu $wslPath/devops/windows/deployment/common/swarm-leave.sh
        } else {
             Write-Host "Exiting the script..."
            exit
        }
        
    }
    '8' {
        Push-Location $currentPath/devops/windows/run--base-docker-wsl
            Write-Host "Installing required softwares"
            installApp $nodeUrl $nodeAppName
            installApp $sbtUrl $sbtAppName
            installApp $javaUrl $javaAppName
            Push-Location $currentPath/devops/windows/deployment/common
                Write-Host "Current directory: $PWD.path"
                .\allow_port.ps1 -Wait
            Pop-Location
        Pop-Locations
    }
    default {
        Write-Host "Invalid choice. Please enter a valid option."
    }
}


