$async_handle = Import-Clixml -Path (Join-Path './' 'async_handle.xml')
$async_handle | Stop-Process