$script_path = $MyInvocation.MyCommand.Path
$script_dir = Split-Path $script_path
Set-Location $script_dir

$config = 'reference\config-reference'

Invoke-Expression -Command "sbt 'run ${config}.json'"
Copy-Item ${config}-api.json .\config-api.json
