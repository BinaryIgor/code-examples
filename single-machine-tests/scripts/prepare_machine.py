import requests
from os import environ
import sys
import time

SMALL_MACHINE = "small"
SMALL_MACHINE_SLUG = "s-1vcpu-1gb-amd"

MEDIUM_MACHINE = "medium"
MEDIUM_MACHINE_SLUG = "s-2vcpu-2gb-amd"

LARGE_MACHINE = "large"
LARGE_MACHINE_SLUG = "s-4vcpu-8gb-amd"

EXTRA_LARGE_MACHINE = "x-large"
EXTRA_LARGE_MACHINE_SLUG = "s-8vcpu-16gb-amd"

def print_and_exit(message):
    print(message)
    sys.exit(1)

if len(sys.argv) < 2:
    print_and_exit("Argument with machine size is required!")

machine_size = sys.argv[1]
if machine_size == SMALL_MACHINE:
    machine_slug = SMALL_MACHINE_SLUG
elif machine_size == MEDIUM_MACHINE:
    machine_slug = MEDIUM_MACHINE_SLUG
elif machine_size == LARGE_MACHINE:
    machine_slug = LARGE_MACHINE_SLUG
elif machine_size == EXTRA_LARGE_MACHINE:
    machine_slug = EXTRA_LARGE_MACHINE_SLUG
else:
    print(f"Unkown machine size: {machine_size}")
    sys.exit(1)

SSH_KEY_FINGERPRINT = "a0:3a:d4:d8:52:4a:8b:34:50:fd:20:c7:19:a1:8a:b4"

DIGITAL_OCEAN_URL = "https://api.digitalocean.com/v2"

MACHINE_NAME = f"single-machine-{machine_size}"
VOLUME_NAME = f"single-machine-volume"

REGION = "fra1"
IMAGE = "ubuntu-22-04-x64"

print("Creating machine for deploy...")

API_TOKEN = environ.get("DO_API_TOKEN")

if API_TOKEN is None:
    print("DO_API_TOKEN env variable needs to be supplied with valid digital ocean token")


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
    "size": SMALL_MACHINE_SLUG,
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
    "size_gigabytes": 25,
    "region": REGION,
    "filesystem_type": "ext4"
}

print(droplet_config)
print()

def create_resource(path, resource, data):
    response = requests.post(f'{DIGITAL_OCEAN_URL}/{path}', headers=AUTH_HEADER, json=data)
    if not response.ok:
        print_and_exit(f'''Fail to create {resource}!
        Code: {response.status_code}
        Body: {response.text}''')

    return response.json()[resource]

print("Creating droplet...")

created_droplet = create_resource("droplets", "droplet", droplet_config)

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

print("Droplet created, creating volume...")

created_volume = create_resource("volumes", "volume", volume_config)

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
    print_and_exit(f'''Fail to attach volume!
    Code: {response.status_code}
    Body: {response.text}''')

print("Volume is being attached!")

print("...")

time.sleep(1)

print("Everything should be ready in a few minutes!")
print("...")
print("To check it out, run:")
print(f"ssh single-machine@{droplet_ip}")