$async_handle = Import-Clixml -Path (Join-Path './' 'async_handle.xml')
$async_handle | Stop-Process

$etl_handle = Import-Clixml -Path (Join-Path './' 'etl_handle.xml')
$etl_handle | Stop-Process

$controller_handle = Import-Clixml -Path (Join-Path './' 'controller_handle.xml')
$controller_handle | Stop-Process