$script_path = $MyInvocation.MyCommand.Path
$script_dir = Split-Path $script_path
Set-Location $script_dir

$serveUIPort = 3000


# BUILD UI
npm install -g yarn serve 
Push-Location ..\..\..\JeMPI_Apps\JeMPI_UI
  yarn install --frozen-lockfile
  yarn build
Pop-Location

# PRODUCTION SERVE
Push-Location ..\..\..\JeMPI_Apps\JeMPI_UI
  # Serve the built project
  $serveCommand = "serve -s build -l $serveUIPort"
  Invoke-Expression -Command $serveCommand
Pop-Location