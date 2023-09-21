
# 
# build apps
#
pushd ..\..\JeMPI_Apps
#  mvn clean  
#  mvn package
popd


$AsyncReceiverFolder = '.\async_receiver'

#
# start async receiver7
#
if (Test-path $AsyncReceiverFolder\csv) {
  Write-Host "$AsyncReceiverFolder\csv Folder Exists"
} else { 
  New-Item $AsyncReceiverFolder\csv -ItemType Directory
  Write-Host 'Folder Created successfully'
}

Start-Process -FilePath java `
              -ArgumentList "-DLOG4J2_LEVEL=`"DEBUG`"", `
                            "-DKAFKA_BOOTSTRAP_SERVERS=kafka-01:9092", `
                            "-DKAFKA_CLIENT_ID=client-id-syncrx", `
                            "-jar ..\..\..\JeMPI_Apps\JeMPI_AsyncReceiver\target\AsyncReceiver-1.0-SNAPSHOT-spring-boot.jar" `
              -WindowStyle Normal `
              -WorkingDirectory .\$AsyncReceiverFolder `
              -RedirectStandardError error.txt `
              -RedirectStandardOutput log.txt `
              -Debug `
              -Verbose
