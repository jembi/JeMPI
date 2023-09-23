
# 
# build apps
#

cd Z:\devops\windows\windows-apps

$workingDir = Get-Location

Write-Output (Get-Location)
pushd ..\..\..\JeMPI_Apps
  mvn clean  
  mvn package
popd
$AsyncReceiverFolder = '.\app_data\async_receiver'

#
# start async receiver7
#
if (Test-path $AsyncReceiverFolder\app\csv) {
  Write-Host "$AsyncReceiverFolder\app\csv Folder Exists"
} else { 
  New-Item $AsyncReceiverFolder\app\csv -ItemType Directory
  Write-Host 'Folder Created successfully'
}

Start-Process -FilePath java `
              -ArgumentList "-DLOG4J2_LEVEL=`"DEBUG`"", `
                            "-DKAFKA_BOOTSTRAP_SERVERS=192.168.88.252:9094", `
                            "-DKAFKA_CLIENT_ID=client-id-syncrx", `
                            "-jar ..\..\..\..\..\..\JeMPI_Apps\JeMPI_AsyncReceiver\target\AsyncReceiver-1.0-SNAPSHOT-spring-boot.jar" `
              -WindowStyle Normal `
              -WorkingDirectory .\$AsyncReceiverFolder\app `
              -RedirectStandardError error.txt `
              -RedirectStandardOutput log.txt `
              -Debug `
              -Verbose

Get-Job


