

# Define the URL of the MSI file
$nodeUrl = "https://nodejs.org/dist/v20.10.0/node-v20.10.0-x64.msi"
$nodeAppName="node.exe"

$sbtUrl = "https://github.com/sbt/sbt/releases/download/v1.9.7/sbt-1.9.7.msi"
$sbtAppName="sbt.exe"

$javaUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.8.1+1/OpenJDK17U-jdk_x64_windows_hotspot_17.0.8.1_1.msi"
# $javaUrl = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.1+12/OpenJDK21U-jdk_x64_windows_hotspot_21.0.1_12.msi"
$javaAppName="java.exe"


# Define the local path where you want to save the MSI file

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


Write-Host "Select an option for local deployment:"
Write-Host "1. Deploy JeMPI from Scratch (With all installations...)."
Write-Host "2. Build and Reboot."
Write-Host "3. Restart JeMPI."
Write-Host "4. Down the JeMPI."
Write-Host "5. Destroy JeMPI (This process will wipe all data)."

# Get user input
$choice = Read-Host "Enter the number of your choice"

# Process the user's choice
switch ($choice) {
    '1' {
        Write-Host "Installing required softwares"
        installApp $nodeUrl $nodeAppName
        installApp $sbtUrl $sbtAppName
        installApp $javaUrl $javaAppName
        # Add your code for Option 1

        wsl -d Ubuntu /mnt/d/Jembi/Ethiopia/JeMPI/devops/windows/deploy-local-wsl.sh
        Write-Host "Running file: start.ps1"
        .\start.ps1 -Wait

        Write-Host "Script completed."
        Write-Host "Running file: start-ui.ps1"
        .\start-ui.ps1 -Wait

        Write-Host "Script completed."
    }
    '2' {
        cd run--base-docker-wsl
        Write-Host "Build and Reboot"
        Write-Host "Running file: start.ps1"
        .\start.ps1 -Wait

        Write-Host "Script completed."
        Write-Host "Running file: start-ui.ps1"
        .\start-ui.ps1 -Wait

        Write-Host "Script completed."
        # Add your code for Option 2
    }
    '3' {
        cd run--base-docker-wsl
        Write-Host "Down the JeMPI."
        Write-Host "Running file: stop.ps1"
        .\stop.ps1 -Wait
        Write-Host "Build and Reboot"
        Write-Host "Running file: start.ps1"
        .\start.ps1 -Wait

        Write-Host "Script completed."
        Write-Host "Running file: start-ui.ps1"
        .\start-ui.ps1 -Wait

        Write-Host "Script completed."
        # Add your code for Option 3
    }
    '4' {
        cd run--base-docker-wsl
        Write-Host "Down the JeMPI."
        Write-Host "Running file: stop.ps1"
        .\stop.ps1 -Wait
        # Add your code for Option 3
    }
    '5' {
        Write-Host "Down the JeMPI."
        Write-Host "Running file: stop.ps1"
        .\stop.ps1 -Wait        # Add your code for Option 3
        wsl -d Ubuntu /mnt/d/Jembi/Ethiopia/JeMPI/devops/windows/base-docker-wsl/b-swarm-3-leave.sh
    }
    default {
        Write-Host "Invalid choice. Please enter a valid option."
    }
}



