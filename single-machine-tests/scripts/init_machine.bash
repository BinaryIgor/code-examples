#!/bin/bash
set -euo pipefail

# Create user and setup passwordless sudo to simplify admin tasks
useradd --create-home --shell "/bin/bash" --groups sudo "_user_placeholder_"
echo "_user_placeholder_ ALL=(ALL) NOPASSWD: ALL" | EDITOR='tee -a' visudo

# Create SSH directory for sudo user and move keys over
home_directory="$(eval echo ~_user_placeholder_)"
mkdir --parents "${home_directory}/.ssh"
cp /root/.ssh/authorized_keys "$home_directory/.ssh"
chmod 0700 "$home_directory/.ssh"
chmod 0600 "$home_directory/.ssh/authorized_keys"
chown --recursive "_user_placeholder_":"_user_placeholder_" "$home_directory/.ssh"

# Disable root SSH login with password
sed --in-place 's/^PermitRootLogin.*/PermitRootLogin no/g' /etc/ssh/sshd_config
sed --in-place 's/^PasswordAuthentication.*/PasswordAuthentication no/g' /etc/ssh/sshd_config
if sshd -t -q; then systemctl restart ssh; fi

# Install docker & allow non sudo access
apt update
# install a few prerequisite packages which let apt use packages over HTTPS:
apt install apt-transport-https ca-certificates curl software-properties-common -y
# Then add the GPG key for the official Docker repository to your system
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
# Add the Docker repository to APT sources
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu focal stable"
# This will also update our package database with the Docker packages from the newly added repo.
# Make sure you are about to install from the Docker repo instead of the default Ubuntu repo:
apt-cache policy docker-ce
# Finally, install Docker:
apt install docker-ce -y

# Allow non root access to a docker
usermod -aG docker _user_placeholder_