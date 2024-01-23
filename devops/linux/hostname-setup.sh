#!/bin/bash

# Get the IP address and print it
ip_address=$(hostname -I | awk '{print $1}')

echo "The IP address is: $ip_address"

# Get and print the system device name
device_name=$(uname -n)


file_path="/etc/hosts"
search_string=$device_name
host_ip="$ip_address     $device_name"
echo "Hostname : $host_ip"

# Check if the string is present in the file
if grep -q "$search_string" "$file_path"; then
    # Replace the string using sed
    echo "s/$search_string/$host_ip/g"
    sudo sed -i "/$search_string/c\\$host_ip" "$file_path"
    echo "String replaced successfully."
else
    echo "$host_ip" | sudo tee -a "$file_path"
    
    echo "New line added successfully."
fi

echo "Updated hosts file"
cat /etc/hosts