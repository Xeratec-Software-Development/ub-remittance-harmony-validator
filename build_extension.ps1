# Define variables
$REMOTE_USER = "harmony_admin"
$REMOTE_HOST = "20.119.81.2"
$SSH_PEM = ".\harmony_testcorner3.pem"
$REMOTE_DIR = "/etc/harmony-ap/extensions/lib/"
$FileToCopy = $args[0]

# Maven installation step
# Write-Output "==============================="
# Write-Output "Running Maven Install..."
# Write-Output "==============================="
# & mvn install -Ptomcat -Pdefault-plugins -Pdatabase -PUI "-Dmaven.test.skip=true"

# Stop the harmony service on the remote host
# Write-Output "==============================="
# Write-Output "Stopping harmony..."
# Write-Output "==============================="
# ssh -i ${SSH_PEM} "${REMOTE_USER}@${REMOTE_HOST}" "sudo systemctl stop harmony-ap"

# Copy file to remote directory
Write-Output "==============================="
Write-Output "Copying file to remote..."
Write-Output "==============================="
scp -i ${SSH_PEM} ${FileToCopy} "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}"

# Start the harmony service on the remote host
# Write-Output "==============================="
# Write-Output "Starting harmony..."
# Write-Output "==============================="
# ssh -i ${SSH_PEM} "${REMOTE_USER}@${REMOTE_HOST}" "sudo systemctl start harmony-ap"

# Restart the harmony service on the remote host
Write-Output "==============================="
Write-Output "Restarting harmony..."
Write-Output "==============================="
ssh -i ${SSH_PEM} "${REMOTE_USER}@${REMOTE_HOST}" "sudo systemctl restart harmony-ap"
