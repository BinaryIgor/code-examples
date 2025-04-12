## Configure Bitcoin Core

Download core (check out and choose version at https://bitcoin.org/bin/):
```
curl --output "bitcoin-core.tar.gz" https://bitcoin.org/bin/bitcoin-core-27.0/bitcoin-27.0-x86_64-linux-gnu.tar.gz
```

Unpack it:
```
tar xzf bitcoin-core.tar.gz
```

Remember to verify the download (https://bitcoincore.org/en/download/); as they say:
> Don't Trust, Verify.


As of 2025-04, on a 4 years old laptop with:
* Ubuntu 24 OS
* 1 TB external SSD
* 32 GB of RAM, Intel® Core™ i7-9750HF CPU @ 2.60GHz × 12

syncing 1 day of blockchain's history took on average 30 seconds, so time to sync the whole history:
```
24 * 60 = 1440 minutes in a day
1440 * 2 = 2880 days per day
2880 / 30 = 96 months per day
96 / 12 = 8 years per day
```
So, given Bitcoin's 16 years history, it should take about two days on a machine with a similar setup.

## Networking

1. Port forwarding - router or VPN (preferable)
2. Ufw to set up some basic rules

## Disable suspend on a laptop

```
sudo nano /etc/systemd/logind.conf

HandleLidSwitch=ignore
HandleLidSwitchExternalPower=ignore
HandleLidSwitchDocked=ignore
LidSwitchIgnoreInhibited=no

reboot!
```

## Firewall

```
sudo ufw allow from 192.168.1.0/24 (your network mask) to any proto tcp
sudo ufw deny 22/tcp
# allow all TCP, but only on a particular interface
sudo ufw allow in on proton0 proto tcp from any to any port 8333:65535
sudo ufw enable
sudo ufw status verbose
```

## SSH server

Install:
```
sudo apt update
sudo apt install openssh-server

sudo systemctl enable ssh
sudo systemctl start ssh

sudo systemctl status ssh
```

Configure:
```
sudo nano /etc/ssh/sshd_config

# Disable password authentication (safe to leave it on the local network with a firewall)
PasswordAuthentication no

# Enable public key authentication (not needed if on the local network)
PubkeyAuthentication yes

# Disable root login
PermitRootLogin no

# Allow only specific users (optional but recommended)
AllowUsers yourusername
```
Restart:
```
sudo systemctl restart ssh
```

Default of ufw is that disables all incoming traffic and enables all outgoing.

## bitcoin-cli cmds

```
bitcoin-cli getblockcount 
bitcoin-cli getpeerinfo
bitcoin-cli getnettotals
bitcoin-cli getnetworkinfo
bitcoin-cli getmempoolinfo
bitcoin-cli getrawtransaction txid true
```

## Cron jobs

```
crontab -l
crontab <path-to-your-crontab>
crontab -l
```