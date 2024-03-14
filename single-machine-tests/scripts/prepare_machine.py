import requests
from os import environ
import sys
import time

SSH_KEY_FINGERPRINT = "a0:3a:d4:d8:52:4a:8b:34:50:fd:20:c7:19:a1:8a:b4"

DIGITAL_OCEAN_URL = "https://api.digitalocean.com/v2"

MACHINE_NAME = "single-machine"
VOLUME_NAME = f"{MACHINE_NAME}-volume"

SMALL_MACHINE = "s-1vcpu-1gb-amd"
MEDIUM_MACHINE = "s-2vcpu-2gb-amd"
LARGE_MACHINE = "s-4vcpu-8gb-amd"
EXTRA_LARGE_MACHINE = "s-8vcpu-16gb-amd"
REGION = "fra1"
IMAGE = "ubuntu-22-04-x64"

print("Creating machine for deploy...")

API_TOKEN = environ.get("DO_API_TOKEN")

if API_TOKEN is None:
    raise Exception("DO_API_TOKEN env variable needs to be supplied with valid digital ocean token")


AUTH_HEADER = {"Authorization": f"Bearer {API_TOKEN}"}

with open("machine_init.bash") as f:
    machine_init = f.read()


# print("Init script read, its content:")
# print(machine_init)

# To debug user data, run: 
# cat /var/log/cloud-init-output.log | grep userdata 
# ...on the droplet
droplet_config = {
    "name": MACHINE_NAME,
    "region": REGION,
    "size": SMALL_MACHINE,
    "image": IMAGE,
    "ssh_keys": [SSH_KEY_FINGERPRINT],
    "backups": False,
    "ipv6": True,
    "monitoring": True,
    "user_data": machine_init
}
# TODO: describe for what we need this
volume_config = {
    "name": VOLUME_NAME,
    "size_gigabytes": 50,
    "region": REGION,
    "filesystem_type": "ext4"
}

print(droplet_config)
print()

response = requests.post(f'{DIGITAL_OCEAN_URL}/droplets', headers=AUTH_HEADER, json=droplet_config)
if not response.ok:
    print(f'''Fail to create droplet!
     Code: {response.status_code}
     Body: {response.text}''')
    sys.exit(1)


print("Droplet is being created...")
created_droplet = response.json()["droplet"]
print("...")
# print(created_droplet)

droplet_id = created_droplet["id"]
droplet_ip = "unknown"
droplet_ips = created_droplet["networks"]["v4"]
for a in droplet_ips:
    if a['type'] == 'public':
        droplet_ip = a['ip_address']
        break

print("...")

print("Droplet created, creating volume...")

response = requests.post(f'{DIGITAL_OCEAN_URL}/volumes', headers=AUTH_HEADER, json=volume_config)
if not response.ok:
    print(f'''Fail to create volume!
     Code: {response.status_code}
     Body: {response.text}''')
    sys.exit(1)


print("Volume is being created...")
created_volume = response.json()["volume"]
print("...")
print(created_volume)


print("...")

print("Before attaching volume, we need to wait for the droplet creation to finish...")

while True:
    response = requests.get(f'{DIGITAL_OCEAN_URL}/droplets/{droplet_id}', headers=AUTH_HEADER)
    d_status = response.json()["droplet"]['status']
    print(f"Droplet status: {d_status}")
    if d_status == 'active':
        print("Droplet is active, we can now attach its volume!")
        break
    else:
        print("Waiting for droplet to become active...")
        time.sleep(5)



response = requests.post(f'{DIGITAL_OCEAN_URL}/volumes/actions', headers=AUTH_HEADER, json={
    "type": "attach",
    "volume_name": VOLUME_NAME,
    "droplet_id": droplet_id
})
if not response.ok:
    print(f'''Fail to attach volume!
     Code: {response.status_code}
     Body: {response.text}''')
    sys.exit(1)

print("Volume is being attached!")

print("...")

time.sleep(1)

print("Everything should be ready in a few minutes!")
print("...")
print("To check it out, run:")
print(f"ssh single-machine@{droplet_ip}")