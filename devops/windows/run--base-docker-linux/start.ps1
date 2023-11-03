$script_path = $MyInvocation.MyCommand.Path
$script_dir = Split-Path $script_path
Set-Location $script_dir

. $script_dir\local.ps1

Copy-Item ..\..\..\JeMPI_Apps\JeMPI_Configuration\config-api.json ..\..\..\JeMPI_Apps\JeMPI_API\src\main\resources\config-api.json
Copy-Item ..\..\..\JeMPI_Apps\JeMPI_Configuration\config-api.json ..\..\..\JeMPI_Apps\JeMPI_API_KC\src\main\resources\config-api.json


$kafka1_ip                                    = $linux_server_ip
$postgresql_ip                                = $linux_server_ip
$postgresql_port                              = 5432
$dgraph_hosts                                 = $linux_server_ip
$dgraph_ports                                 = '9080'

$api_ip                                       = 'localhost'
$api_http_port                                = 50000

$etl_ip                                       = 'localhost'
$etl_http_port                                = 50001

$controller_ip                                = 'localhost'
$controller_http_port                         = 50002

$linker_ip                                    = 'localhost'
$linker_http_port                             = 50003

$jempi_apps_dir                               = "..\..\..\..\..\JeMPI_Apps"

$async_receiver_folder                        = '.\app_data\async_receiver'
$etl_folder                                   = '.\app_data\etl'
$controller_folder                            = '.\app_data\controller'
$linker_folder                                = '.\app_data\linker'
$api_folder                                   = '.\app_data\api'

$def_kafka_bootstrap_servers                  = "-DKAFKA_BOOTSTRAP_SERVERS=" + $kafka1_ip + ":9094"
$def_postgresql_ip                            = "-DPOSTGRESQL_IP=" + $postgresql_ip
$def_postgresql_port                          = "-DPOSTGRESQL_PORT=" + $postgresql_port 
$def_postgresql_user                          = "-DPOSTGRESQL_USER=`"postgres`""
$def_postgresql_password                      = "-DPOSTGRESQL_PASSWORD=`"postgres`""
$def_postgresql_notifications_db              = "-DPOSTGRESQL_DATABASE=`"notifications`""
$def_dgraph_hosts                             = "-DDGRAPH_HOSTS=" + $dgraph_hosts
$def_dgraph_ports                             = "-DDGRAPH_PORTS=" + $dgraph_ports
$def_etl_ip                                   = "-DETL_IP=" + $etl_ip
$def_etl_http_port                            = "-DETL_HTTP_PORT=" + $etl_http_port
$def_controller_ip                            = "-DCONTROLLER_IP=" + $controller_ip
$def_controller_http_port                     = "-DCONTROLLER_HTTP_PORT=" + $controller_http_port
$def_linker_ip                                = "-DLINKER_IP=" + $linker_ip
$def_linker_http_port                         = "-DLINKER_HTTP_PORT=" + $linker_http_port
$def_api_ip                                   = "-DAPI_IP=" + $api_ip
$def_api_http_port                            = "-DAPI_HTTP_PORT=" + $api_http_port

$async_receiver_jar                           = "-jar " + $jempi_apps_dir + "\JeMPI_AsyncReceiver\target\AsyncReceiver-1.0-SNAPSHOT-spring-boot.jar"
$def_async_reveiver_log4j_level               = "-DLOG4J2_LEVEL=DEBUG"
$def_async_receiver_kafka_client_id           = "-DKAFKA_CLIENT_ID=client-id-syncrx"

$etl_jar                                      = "-jar " + $jempi_apps_dir + "\JeMPI_ETL\target\ETL-1.0-SNAPSHOT-spring-boot.jar"
$def_etl_log4j_level                          = "-DLOG4J2_LEVEL=DEBUG"
$def_etl_kafka_application_id                 = "-DKAFKA_APPLICATION_ID=app-id-etl"

$controller_jar                               = "-jar " + $jempi_apps_dir + "\JeMPI_Controller\target\Controller-1.0-SNAPSHOT-spring-boot.jar"
$def_controller_log4j_level                   = "-DLOG4J2_LEVEL=DEBUG"
$def_controller_kafka_application_id          = "-DKAFKA_APPLICATION_ID=app-id-ctrl"
$def_controller_kafka_client_id               = "-DKAFKA_CLIENT_ID=client-id-ctrl"

$linker_jar                                   = "-jar " + $jempi_apps_dir + "\JeMPI_Linker\target\Linker-1.0-SNAPSHOT-spring-boot.jar"
$def_linker_log4j_level                       = "-DLOG4J2_LEVEL=TRACE" 
$def_linker_kafka_application_id_interactions = "-DKAFKA_APPLICATION_ID_INTERACTIONS=app-id-lnk1"
$def_linker_kafka_application_id_mu           = "-DKAFKA_APPLICATION_ID_MU=app-id-lnk2"
$def_linker_kafka_client_id_notifications     = "-DKAFKA_CLIENT_ID_NOTIFICATIONS=client-id-lnk3"
$def_linker_match_threshold                   = "-DLINKER_MATCH_THRESHOLD=0.65"
$def_linker_match_threshold_margin            = "-DLINKER_MATCH_THRESHOLD_MARGIN=0.1"

$api_jar                                      = "-jar " + $jempi_apps_dir + "\JeMPI_API\target\API-1.0-SNAPSHOT-spring-boot.jar"
$def_api_log4j_level                          = "-DLOG4J2_LEVEL=TRACE" 
$def_api_kafka_application_id                 = "-DKAFKA_APPLICATION_ID=app-id-api"


# 
# build UI apps
#


# BUILD UI
#npm install -g yarn serve 
#Push-Location ..\..\..\JeMPI_Apps\JeMPI_UI
#  yarn install --frozen-lockfile
#  yarn build
#Pop-Location


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
                                            '-server', `
                                            '--enable-preview', `
                                            $async_receiver_jar `
                              -WindowStyle Normal `
                              -WorkingDirectory $async_receiver_folder `
                              -Debug `
                              -Verbose `
                              -PassThru `
                              -RedirectStandardError 'async_stderr.txt'
#                             -RedirectStandardOutput 'async_stdout.txt'
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
                                          $def_etl_ip, `
                                          $def_etl_http_port, `
                                          $def_controller_ip, `
                                          $def_controller_http_port, `
                                          $def_linker_ip, `
                                          $def_linker_http_port, `
                                          '-server', `
                                          '--enable-preview', `
                                          $etl_jar `
                            -WindowStyle Normal `
                            -WorkingDirectory $etl_folder `
                            -Debug `
                            -Verbose `
                            -PassThru `
                            -RedirectStandardError 'etl_stderr.txt'
#                           -RedirectStandardOutput 'etl_stdout.txt'
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
                                                 $def_postgresql_ip, `
                                                 $def_postgresql_port, `
                                                 $def_postgresql_user, `
                                                 $def_postgresql_password, `
                                                 $def_postgresql_notifications_db, `
                                                 $def_kafka_bootstrap_servers, `
                                                 $def_controller_kafka_application_id, `
                                                 $def_controller_kafka_client_id, `
                                                 $def_controller_http_port, `
                                                 $def_etl_ip, `
                                                 $def_etl_http_port, `
                                                 $def_controller_ip, `
                                                 $def_controller_http_port, `
                                                 $def_linker_ip, `
                                                 $def_linker_http_port, `
                                                 '-server', `
                                                 '--enable-preview', `
                                                 $controller_jar `
                                   -WindowStyle Normal `
                                   -WorkingDirectory $controller_folder `
                                   -Debug `
                                   -Verbose `
                                   -PassThru `
                                   -RedirectStandardError 'controller_stderr.txt'
#                                  -RedirectStandardOutput 'controller_stdout.txt'
$controller_handle | Export-Clixml -Path (Join-Path './' 'controller_handle.xml')


#
# start linker
#
if (Test-path $linker_folder) {
  Write-Host ${linker_folder}' exists'   
} else {
  New-Item $linker_folder -ItemType Directory
  Write-Host 'Folder Created successfully'
}
$linker_handle = Start-Process -FilePath java `
                               -ArgumentList $def_linker_log4j_level, `
                                             $def_postgresql_ip, `
                                             $def_postgresql_port, `
                                             $def_postgresql_user, `
                                             $def_postgresql_password, `
                                             $def_postgresql_notifications_db, `
                                             $def_kafka_bootstrap_servers, `
                                             $def_linker_kafka_application_id_interactions, `
                                             $def_linker_kafka_application_id_mu, `
                                             $def_linker_kafka_client_id_notifications, `
                                             $def_dgraph_hosts, `
                                             $def_dgraph_ports, `
                                             $def_linker_http_port, `
                                             $def_linker_match_threshold, `
                                             $def_linker_match_threshold_margin, `
                                             $def_etl_ip, `
                                             $def_etl_http_port, `
                                             $def_controller_ip, `
                                             $def_controller_http_port, `
                                             $def_linker_ip, `
                                             $def_linker_http_port, `
                                             $def_api_ip, `
                                             $def_api_http_port, `
                                             '-server', `
                                             '--enable-preview', `
                                             $linker_jar `
                               -WindowStyle Normal `
                               -WorkingDirectory $linker_folder `
                               -Debug `
                               -Verbose `
                               -PassThru `
                               -RedirectStandardError 'linker_stderr.txt'
#                              -RedirectStandardOutput 'linker_stdout.txt'
$linker_handle | Export-Clixml -Path (Join-Path './' 'linker_handle.xml')


#
# start api
#
if (Test-path $api_folder) {
  Write-Host ${api_folder}' exists'   
} else {
  New-Item $api_folder -ItemType Directory
  Write-Host 'Folder Created successfully'
}
$api_handle = Start-Process -FilePath java `
                            -ArgumentList $def_api_log4j_level, `
                                          $def_postgresql_ip, `
                                          $def_postgresql_port, `
                                          $def_postgresql_user, `
                                          $def_postgresql_password, `
                                          $def_postgresql_notifications_db, `
                                          $def_kafka_bootstrap_servers, `
                                          $def_api_kafka_application_id, `
                                          $def_dgraph_hosts, `
                                          $def_dgraph_ports, `
                                          $def_etl_ip, `
                                          $def_etl_http_port, `
                                          $def_controller_ip, `
                                          $def_controller_http_port, `
                                          $def_linker_ip, `
                                          $def_linker_http_port, `
                                          $def_api_ip, `
                                          $def_api_http_port, `
                                          '-server', `
                                          '--enable-preview', `
                                          $api_jar `
                            -WindowStyle Normal `
                            -WorkingDirectory $api_folder `
                            -Debug `
                            -Verbose `
                            -PassThru `
                            -RedirectStandardError 'api_stderr.txt'
#                            -RedirectStandardOutput 'api_stdout.txt'
$api_handle | Export-Clixml -Path (Join-Path './' 'api_handle.xml')
