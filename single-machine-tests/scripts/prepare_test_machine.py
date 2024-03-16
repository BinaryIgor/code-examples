import requests
from os import environ
import sys

SSH_KEY_FINGERPRINT = "a0:3a:d4:d8:52:4a:8b:34:50:fd:20:c7:19:a1:8a:b4"

DIGITAL_OCEAN_URL = "https://api.digitalocean.com/v2"

MACHINE_NAME = "test-machine"
MACHINE_SLUG = "s-2vcpu-2gb-amd"

REGION = "fra1"
IMAGE = "ubuntu-22-04-x64"

print("Creating test machine...")

API_TOKEN = environ.get("DO_API_TOKEN")

if API_TOKEN is None:
    print("DO_API_TOKEN env variable needs to be supplied with valid digital ocean token")
    sys.exit(1)


AUTH_HEADER = {"Authorization": f"Bearer {API_TOKEN}"}

with open("init_test_machine.bash") as f:
    machine_init = f.read()

droplet_config = {
    "name": MACHINE_NAME,
    "region": REGION,
    "size": MACHINE_SLUG,
    "image": IMAGE,
    "ssh_keys": [SSH_KEY_FINGERPRINT],
    "backups": False,
    "ipv6": True,
    "monitoring": True,
    "user_data": machine_init
}
print()

response = requests.post(f'{DIGITAL_OCEAN_URL}/droplets', headers=AUTH_HEADER, json=droplet_config)
if not response.ok:
    print(f'''Fail to create machine!
    Code: {response.status_code}
    Body: {response.text}''')


print("Creating droplet...")

created_droplet = response.json()['droplet']

print("...")
print(created_droplet)
print("...")

droplet_id = created_droplet["id"]
droplet_ip = "unknown"
droplet_ips = created_droplet["networks"]["v4"]
for a in droplet_ips:
    if a['type'] == 'public':
        droplet_ip = a['ip_address']
        break

print("...")

print("Everything should be ready in a few minutes!")
print("...")
print("To check it out, run:")
print(f"ssh test-machine@{droplet_ip}")