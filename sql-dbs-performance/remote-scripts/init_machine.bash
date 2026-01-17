#!/bin/bash
set -euo pipefail

# Create user and set up passwordless sudo to simplify admin tasks
useradd --create-home --shell "/bin/bash" --groups sudo "_user_placeholder_"
echo "_user_placeholder_ ALL=(ALL) NOPASSWD: ALL" | EDITOR='tee -a' visudo

# Create SSH directory for sudo user and move keys over
home_directory="$(eval echo ~_user_placeholder_)"
mkdir --parents "${home_directory}/.ssh"
cp /root/.ssh/authorized_keys "$home_directory/.ssh"
chmod 0700 "$home_directory/.ssh"
chmod 0600 "$home_directory/.ssh/authorized_keys"
chown --recursive "_user_placeholder_":"_user_placeholder_" "$home_directory/.ssh"

# SSH hardening; files are processed in lexical order, lower-numbered ones taking precedence
ssh_hardening_file="/etc/ssh/sshd_config.d/00-hardening.conf"

cat > "$ssh_hardening_file" <<EOF
# Authentication
PermitRootLogin no
PasswordAuthentication no
ChallengeResponseAuthentication no
KerberosAuthentication no
GSSAPIAuthentication no
PubkeyAuthentication yes
AuthenticationMethods publickey

# Authorization
AllowUsers _user_placeholder_

# Reduce attack surface
AllowAgentForwarding no
AllowTcpForwarding no
X11Forwarding no
PermitTunnel no
# allow interactive sessions
PermitTTY yes

# Session hygiene
ClientAliveInterval 300
# If two keepalive probes are sent and no response is received, SSH daemon will terminate the session;
# with the config, a dead session lives for at most ~10 minutes
ClientAliveCountMax 2
# After the TCP connection is established, the client has 30 seconds to successfully authenticate
LoginGraceTime 30
MaxAuthTries 5
EOF

chmod 0644 "$ssh_hardening_file"

if sshd -t -q; then systemctl restart ssh; fi

# Install docker & allow non-sudo access
apt-get update
apt-get install ca-certificates curl
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to apt sources:
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
apt-get update

# Finally, install docker:
apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y

# Allow non-root access to docker;
# the docker group is created automatically during Docker installation on most Linux distributions
usermod -aG docker _user_placeholder_
# limit docker logs size
echo '{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "5"
  }
}' > /etc/docker/daemon.json
# restart docker so that changes can take an effect
systemctl restart docker