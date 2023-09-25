$kafka1IP                             = '192.168.88.252'
$postgresql_server                    = '192.168.88.252:5432'

$JeMPI_Apps                           = "..\..\..\..\..\JeMPI_Apps"

$async_receiver_folder                = '.\app_data\async_receiver'
$etl_folder                           = '.\app_data\etl'
$controller_folder                    = '.\app_data\controller'

$def_kafka_bootstrap_servers          = "-DKAFKA_BOOTSTRAP_SERVERS=" + $kafka1IP + ":9094"
$def_postgresql_server                = "-DPOSTGRESQL_SERVER=" + $postgresql_server
$def_postgresql_user                  = "-DPOSTGRESQL_USER=`"postgres`""
$def_postgresql_password              = "-DPOSTGRESQL_PASSWORD=`"postgres`""
$def_postgresql_notifications_db      = "-DPOSTGRESQL_DATABASE=`"notifications`""

$async_receiver_jar                   = "-jar " + $JeMPI_Apps + "\JeMPI_AsyncReceiver\target\AsyncReceiver-1.0-SNAPSHOT-spring-boot.jar"
$def_async_reveiver_log4j_level       = "-DLOG4J2_LEVEL=DEBUG"
$def_async_receiver_kafka_client_id   = "-DKAFKA_CLIENT_ID=client-id-syncrx"

$etl_jar                              = "-jar " + $JeMPI_Apps + "\JeMPI_ETL\target\ETL-1.0-SNAPSHOT-spring-boot.jar"
$def_etl_log4j_level                  = "-DLOG4J2_LEVEL=DEBUG"
$def_etl_kafka_application_id         = "-DKAFKA_APPLICATION_ID=app-id-etl"

$controller_jar                       = "-jar " + $JeMPI_Apps + "\JeMPI_Controller\target\Controller-1.0-SNAPSHOT-spring-boot.jar"
$def_controller_log4j_level           = "-DLOG4J2_LEVEL=DEBUG"
$def_controller_kafka_application_id  = "-DKAFKA_APPLICATION_ID=app-id-ctrl"
$def_controller_kafka_client_id       = "-DKAFKA_CLIENT_ID=client-id-ctrl"
$def_controller_http_server_port      = "-DHTTP_SERVER_PORT=50000"


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
                              -ArgumentList $def_async_reveiver_log4j_level, `
                                            $def_kafka_bootstrap_servers, `
                                            $def_async_receiver_kafka_client_id, `
                                            $async_receiver_jar `
                              -WindowStyle Normal `
                              -WorkingDirectory $async_receiver_folder `
                              -Debug `
                              -Verbose `
                              -PassThru
$async_handle | Export-Clixml -Path (Join-Path './' 'async_handle.xml')

#
# start etl
#
if (Test-path $etl_folder) {
  Write-Host ${etl_folder}' exists'   
} else {
  New-Item $etl_folder -ItemType Directory
  Write-Host 'Folder Created successfully'
}
$etl_handle = Start-Process -FilePath java `
                            -ArgumentList $def_etl_log4j_level, `
                                          $def_kafka_bootstrap_servers, `
                                          $def_etl_kafka_application_id, `
                                          $etl_jar `
                            -WindowStyle Normal `
                            -WorkingDirectory $etl_folder `
                            -Debug `
                            -Verbose `
                            -PassThru
$etl_handle | Export-Clixml -Path (Join-Path './' 'etl_handle.xml')


#
# start controller
#
if (Test-path $controller_folder) {
  Write-Host ${controller_folder}' exists'   
} else {
  New-Item $controller_folder -ItemType Directory
  Write-Host 'Folder Created successfully'
}
$controller_handle = Start-Process -FilePath java `
                                   -ArgumentList $def_controller_log4j_level, `
                                                 $def_postgresql_server, `
                                                 $def_postgresql_user, `
                                                 $def_postgresql_password, `
                                                 $def_postgresql_notifications_db, `
                                                 $def_kafka_bootstrap_servers, `
                                                 $def_controller_kafka_application_id, `
                                                 $def_controller_kafka_client_id, `
                                                 $def_controller_http_server_port, `
                                                 $controller_jar `
                                   -WindowStyle Normal `
                                   -WorkingDirectory $controller_folder `
                                   -Debug `
                                   -Verbose `
                                   -PassThru
#                                   -RedirectStandardError 'controller-stderr.txt' `
#                                   -RedirectStandardOutput 'controller-stdout.txt'
$controller_handle | Export-Clixml -Path (Join-Path './' 'controller_handle.xml')
