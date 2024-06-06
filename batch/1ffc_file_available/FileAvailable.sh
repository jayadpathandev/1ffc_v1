#!/bin/bash

# Sample Command:
# $ ./FileAailable.sh './config.properties' ACH

# Retrieve the parameter
# Configuration file location path (./config.properties)
readonly CONFIG_FILE_PATH="$1"

# Check if the correct number of arguments is provided (expected 2)
if [ $# -ne 2 ]; then
    echo "Error: Invalid number of arguments."
    echo "Usage: $0 <CONFIG_FILE_PATH> <FILE_TYPE>"
    exit 1
fi

# Check if config file exists
if [ ! -f "$CONFIG_FILE_PATH" ]; then
    echo "ERROR: Configuration file not found."
	echo "Usage: $0 <CONFIG_FILE_PATH_PATH> <FILE_TYPE>"
    exit 1
fi

# Reading all the properties file and defining as a variable
echo "Reading configuration file..."
while IFS='=' read -r key value; do
    # Normalize key and assign value to variable
    normalized_key=$(echo "$key" | tr '.' '_')
    normalized_val=$(echo "$value" | tr -d '\\')
    eval "$normalized_key=\"$normalized_val\""
done < "$CONFIG_FILE_PATH"
echo "Configuration file loaded successfully."

# Retrieve the parameter
INPUT_FILE_TYPE="$2"

# Validate the input file type
if ! echo "$file_types_list" | grep -qw "$INPUT_FILE_TYPE"; then
    echo "Error: Invalid file type provided."
    echo "Usage: Please provide one of the following <FILE_TYPE>: $file_types_list"
	echo "Usage: $0 <CONFIG_FILE_PATH> <FILE_TYPE>"
    exit 1
fi
echo "Processing file of type: $INPUT_FILE_TYPE"

# Getting access token
echo "Requesting access token... started"
GET_TOKEN_RESP=$(curl -s -X POST --location "$nls_api_base_url/api/v$nls_api_version/$auth_endpoint" \
--header 'Content-Type: application/json' \
--header 'Accept: text/plain' \
--data "{
  \"username\": \"$nls_api_client_id\",
  \"password\": \"$nls_api_client_secret\"
}")

# Extracting access token
ACCESS_TOKEN=$(echo "$GET_TOKEN_RESP" | sed -nE 's/.*"accessToken":"([^\"]*)",".*/\1/p')

# Check if access token extraction was successful
if [ -z "$ACCESS_TOKEN" ]; then
    echo "ERROR: Failed to extract access token..." >&2
    exit 1
fi

echo "Requesting access token... completed"

# Getting status of File
echo "Checking file availability..."
STATUS_CODE=$(curl -s -X GET -I --location "$nls_api_base_url/api/v$nls_api_version/$file_check_endpoint?filetype=$INPUT_FILE_TYPE" \
--header 'accept: */*' \
--header "Authorization: Bearer $ACCESS_TOKEN" | awk 'NR==1 {print $2}')

#echo "Checking file availability..... completed"

echo "==================================================================="
# Check status code and print appropriate message
if [ "$STATUS_CODE" = "204" ]; then
    # Print message if files are ready to retrieve
    echo "$STATUS_CODE: Customer Billing System notified the $INPUT_FILE_TYPE file availability."
	echo "==================================================================="
else
    # Print message if files are not ready to retrieve
    echo "$STATUS_CODE: $INPUT_FILE_TYPE file not ready to retrieve."
	echo "==================================================================="
	exit 1
fi
