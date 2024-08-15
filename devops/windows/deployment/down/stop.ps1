﻿$script_path = $MyInvocation.MyCommand.Path
$script_dir = Split-Path $script_path
Set-Location $script_dir

$async_handle = Import-Clixml -Path (Join-Path './' 'async_handle.xml')
$async_handle | Stop-Process

$etl_handle = Import-Clixml -Path (Join-Path './' 'etl_handle.xml')
$etl_handle | Stop-Process

$controller_handle = Import-Clixml -Path (Join-Path './' 'controller_handle.xml')
$controller_handle | Stop-Process

$linker_handle = Import-Clixml -Path (Join-Path './' 'linker_handle.xml')
$linker_handle | Stop-Process

$api_handle = Import-Clixml -Path (Join-Path './' 'api_handle.xml')
$api_handle | Stop-Process
