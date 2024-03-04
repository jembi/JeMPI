# Define the port number
$portNumber = "5432"

# Define the rule name
$ruleName = "Allow_Port_$portNumber"

# Create an inbound rule to allow traffic on the specified port
New-NetFirewallRule -DisplayName $ruleName -Direction Inbound -Protocol TCP -LocalPort $portNumber -Action Allow

# Create an outbound rule to allow traffic on the specified port
New-NetFirewallRule -DisplayName $ruleName -Direction Outbound -Protocol TCP -LocalPort $portNumber -Action Allow

Write-Host "Firewall rule to allow traffic on port $portNumber has been created."
