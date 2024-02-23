$script_path = $MyInvocation.MyCommand.Path
$script_dir = Split-Path $script_path
Set-Location $script_dir

$linux_server_ip =  ((wsl hostname -I) -split " ")[0]

Write-Host $linux_server_ip


$bootstrapper_folder                          = '.\app_data\bootstrapper'
$def_bootstrapper_kafka_client_id               = "-DKAFKA_CLIENT_ID=client-id-bootstrapper"
$def_bootstrapper_kafka_application_id          = "-DKAFKA_APPLICATION_ID=app-id-bootstrapper"


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

$bootstrapper_jar                             = "-jar " + $jempi_apps_dir + "\JeMPI_Bootstrapper\target\Bootstrapper-1.0-SNAPSHOT-spring-boot.jar"
$def_postgresql_postgres                      = "-DPOSTGRESQL_DATABASE=postgres"
$def_postgresql_user_db                       = "-DPOSTGRESQL_USERS_DB=users_db"
$def_postgresql_notifications_db              = "-DPOSTGRESQL_NOTIFICATIONS_DB=notifications_db"
$def_postgresql_audit_db                      = "-DPOSTGRESQL_AUDIT_DB=audit_db"
$def_postgresql_kc_test_db                    = "-DPOSTGRESQL_KC_TEST_DB=kc_test_db"

 Write-Host "Starting BootStrapper App"

$bootstrapper_handle = Start-Process -FilePath java `
                               -ArgumentList $def_linker_log4j_level, `
                                             $def_postgresql_ip, `
                                             $def_postgresql_port, `
                                             $def_postgresql_user, `
                                             $def_postgresql_password, `
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
                                             $def_postgresql_user_db, `
                                             $def_postgresql_notifications_db, `
                                             $def_postgresql_audit_db, `
                                             $def_postgresql_kc_test_db, `
                                             $def_postgresql_postgres, `
                                             $def_bootstrapper_kafka_application_id, `
                                             '-server', `
                                             '--enable-preview', `
                                             $bootstrapper_jar , `
                                             'data', `
                                             'resetAll' `
                               -WindowStyle Normal `
                               -WorkingDirectory $bootstrapper_folder `
                               -Debug `
                               -Verbose `
                               -PassThru `
                               -RedirectStandardError 'bootstrapper_stderr.txt' `
                               -RedirectStandardOutput 'bootstrapper_stdout.txt'
$bootstrapper_handle | Export-Clixml -Path (Join-Path './' 'bootstapper.xml')
