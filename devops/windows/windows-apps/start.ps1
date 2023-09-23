
# 
# build apps
#

#cd Z:\devops\windows\windows-apps
$scriptpath = $MyInvocation.MyCommand.Path
$dir = Split-Path $scriptpath
cd $dir

pushd ..\..\..\JeMPI_Apps
  mvn clean  
  mvn package
popd
$AsyncReceiverFolder = '.\app_data\async_receiver'

#
# start async receiver
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
              -Debug `
              -Verbose

