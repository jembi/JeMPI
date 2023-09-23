$kafka1IP = '192.168.88.252'
$JeMPI_Apps = "..\..\..\..\..\JeMPI_Apps"
$kafka_bootstrap_servers = "-DKAFKA_BOOTSTRAP_SERVERS=" + $kafka1IP + ":9094"

$async_receiver_folder = '.\app_data\async_receiver'

$async_receiver_jar = "-jar " + $JeMPI_Apps + "\JeMPI_AsyncReceiver\target\AsyncReceiver-1.0-SNAPSHOT-spring-boot.jar"
$async_reveiver_log4j_level = "-DLOG4J2_LEVEL=`"DEBUG`""
$async_receiver_kafka_client_id = "-DKAFKA_CLIENT_ID=client-id-syncrx"



$scriptpath = $MyInvocation.MyCommand.Path
$dir = Split-Path $scriptpath
Set-Location $dir

# 
# build apps
#
Push-Location ..\..\..\JeMPI_Apps
  mvn clean  
  mvn package
Pop-Location


#
# start async receiver
#
if (Test-path $async_receiver_folder\csv) {
  Write-Host ${async_receiver_folder}'\csv exists'   
} else {
  New-Item $async_receiver_folder\csv -ItemType Directory
  Write-Host 'Folder Created successfully'
}
$async_handle = Start-Process -FilePath java `
                              -ArgumentList $async_reveiver_log4j_level, `
                                            $kafka_bootstrap_servers, `
                                            $async_receiver_kafka_client_id, `
                                            $async_receiver_jar `
                              -WindowStyle Normal `
                              -WorkingDirectory $async_receiver_folder `
                              -Debug `
                              -Verbose `
                              -PassThru
$async_handle | Export-Clixml -Path (Join-Path './' 'async_handle.xml')
