import requests
from os import environ
import sys
import time

USER_PLACEHOLDER = "_user_placeholder_"

DROPLETS_RESOURCE = "droplets"
FIREWALLS_RESOURCE = "firewalls"
VOLUMES_RESOURCE = "volumes"

ID = "id"
NAME = "name"

SMALL_MACHINE = "small"
SMALL_MACHINE_SLUG = "s-1vcpu-1gb-amd"

MEDIUM_MACHINE = "medium"
MEDIUM_MACHINE_SLUG = "s-2vcpu-2gb-amd"

LARGE_MACHINE = "large"
# 4 CPU, 8 GB RAM + dedicated CPU!
LARGE_MACHINE_SLUG = "c-4"

def print_and_exit(message):
    print(message)
    sys.exit(1)

if len(sys.argv) < 2:
    print_and_exit("Argument with machine size is required!")

single_machine_size = sys.argv[1]
if single_machine_size == SMALL_MACHINE:
    single_machine_slug = SMALL_MACHINE_SLUG
elif single_machine_size == MEDIUM_MACHINE:
    single_machine_slug = MEDIUM_MACHINE_SLUG
elif single_machine_size == LARGE_MACHINE:
    single_machine_slug = LARGE_MACHINE_SLUG
else:
    print(f"Unkown machine size: {single_machine_size}")
    sys.exit(1)

test_machine_slug = MEDIUM_MACHINE_SLUG

SSH_KEY_FINGERPRINT = "a0:3a:d4:d8:52:4a:8b:34:50:fd:20:c7:19:a1:8a:b4"

DIGITAL_OCEAN_URL = "https://api.digitalocean.com/v2"

SINGLE_MACHINE_NAME = "single-machine"
TEST_MACHINE_1_NAME = "test-machine-1"
TEST_MACHINE_2_NAME = "test-machine-2"
test_machine_names = [TEST_MACHINE_1_NAME, TEST_MACHINE_2_NAME]
# single-db volume name needs to be synchronized, if changed!
SINGLE_MACHINE_VOLUME_NAME = "single-machine-volume"
FIREWALL_NAME = "single-machine-test-firewall"

REGION = "fra1"
IMAGE = "ubuntu-22-04-x64"

API_TOKEN = environ.get("DO_API_TOKEN")
if API_TOKEN is None:
    print_and_exit("DO_API_TOKEN env variable needs to be supplied with valid digital ocean token")


AUTH_HEADER = {"Authorization": f"Bearer {API_TOKEN}"}

with open("init_machine.bash") as f:
    init_single_machine = f.read().replace(USER_PLACEHOLDER, "single-machine")

with open("init_machine.bash") as f:
    init_test_machine = f.read().replace(USER_PLACEHOLDER, "test-machine")


# To debug user data, run: 
# cat /var/log/cloud-init-output.log | grep userdata 
# ...on the droplet
single_machine_config = {
    "name": SINGLE_MACHINE_NAME,
    "region": REGION,
    "size": single_machine_slug,
    "image": IMAGE,
    "ssh_keys": [SSH_KEY_FINGERPRINT],
    "backups": False,
    "ipv6": True,
    "monitoring": True,
    "user_data": init_single_machine
}

def test_machine_config(name):
    return {
        "name": name,
        "region": REGION,
        "size": test_machine_slug,
        "image": IMAGE,
        "ssh_keys": [SSH_KEY_FINGERPRINT],
        "backups": False,
        "ipv6": True,
        "monitoring": True,
        "user_data": init_test_machine
    }


# TODO: describe for what we need this
volume_config = {
    "name": SINGLE_MACHINE_VOLUME_NAME,
    "size_gigabytes": 25,
    "region": REGION,
    "filesystem_type": "ext4"
}

firewall_all_addresses =  {
    "addresses": [
        "0.0.0.0/0",
        "::/0"
    ]
}
firewall_config = {
    "name": FIREWALL_NAME,
    "inbound_rules": [
       {
        "protocol": "icmp",
        "ports": "0",
        "sources": firewall_all_addresses
      },
      {
        "protocol": "tcp",
        "ports": "22",
        "sources": firewall_all_addresses
      },
       {
        "protocol": "tcp",
        "ports": "80",
        "sources": firewall_all_addresses
      },
      {
        "protocol": "tcp",
        "ports": "443",
        "sources": firewall_all_addresses
      }
    ],
    "outbound_rules": [
      {
        "protocol": "tcp",
        "ports": "0",
        "destinations": firewall_all_addresses
      },
      {
        "protocol": "udp",
        "ports": "0",
        "destinations": firewall_all_addresses
      },
      {
        "protocol": "icmp",
        "ports": "0",
        "destinations": firewall_all_addresses
      }
    ]
}

def get_resources(resource):
    response = requests.get(f'{DIGITAL_OCEAN_URL}/{resource}', headers=AUTH_HEADER)
    response.raise_for_status()
    return response.json()[resource]


def create_resource(path, resource, data):
    response = requests.post(f'{DIGITAL_OCEAN_URL}/{path}', headers=AUTH_HEADER, json=data)
    if not response.ok:
        print_and_exit(f'''Fail to create {resource}!
        Code: {response.status_code}
        Body: {response.text}''')

    return response.json()[resource]

def create_droplets_if_needed():
    droplet_names_ids = {}
    for d in get_resources(DROPLETS_RESOURCE):
        if d[NAME] == SINGLE_MACHINE_NAME:
            droplet_names_ids[SINGLE_MACHINE_NAME] = d[ID]
        elif d[NAME] == TEST_MACHINE_1_NAME:
            droplet_names_ids[TEST_MACHINE_1_NAME] = d[ID]
        elif d[NAME] == TEST_MACHINE_2_NAME:
            droplet_names_ids[TEST_MACHINE_2_NAME] = d[ID]

    wait_for_machines_to_become_active = len(droplet_names_ids) < (1 + len(test_machine_names))

    if SINGLE_MACHINE_NAME in droplet_names_ids:
        print("Single machine exists, skipping its creation!")
    else:
        print(f"Creating {SINGLE_MACHINE_NAME}...")
        created_droplet = create_resource(DROPLETS_RESOURCE, "droplet", single_machine_config)
        droplet_names_ids[SINGLE_MACHINE_NAME] = created_droplet[ID]
        print(f"{SINGLE_MACHINE_NAME} created!")

    for tm in test_machine_names:
        if tm in droplet_names_ids:
            print(f"{tm} exists, skipping its creation!")
        else:
            print(f"Creating {tm}...")
            created_droplet = create_resource(DROPLETS_RESOURCE, "droplet", test_machine_config(tm))
            droplet_names_ids[tm] = created_droplet[ID]
            print(f"{tm} created!")

    if not wait_for_machines_to_become_active:
        return droplet_names_ids

    # Eventual consistency of Digital Ocean: sometimes new droplets are not visible immediately after creation
    time.sleep(3)

    while True:
        new_droplets = []

        for d in get_resources(DROPLETS_RESOURCE):
            d_status = d['status']
            if d_status == 'new':
                new_droplets.append(d[NAME])

        if new_droplets:
            print(f"Waiting for {new_droplets} droplets to become active...")
            print("...")
            time.sleep(5)
        else:
            print("All droplets are active!")
            print()
            break

    return droplet_names_ids

def create_and_attach_volume_if_needed(droplet_names_ids):
    volume_exists = False
    volume_attached = False
    single_machine_droplet_id = droplet_names_ids[SINGLE_MACHINE_NAME]
    for v in get_resources(VOLUMES_RESOURCE):
        if v[NAME] == SINGLE_MACHINE_VOLUME_NAME:
            volume_exists = True
            attached_to_droplets = v["droplet_ids"]
            volume_attached = single_machine_droplet_id in attached_to_droplets
            break

    if volume_exists:
        print("Volume exists, skipping its creation!")
    else:
        print("Creating volume...")
        create_resource(VOLUMES_RESOURCE, "volume", volume_config)
        print("Volume created!")

    if not volume_attached:
        print("Volume not attached, attaching it!")
        attach_volume(single_machine_droplet_id)
        print("Volume attached!")


def attach_volume(droplet_id):
    response = requests.post(f'{DIGITAL_OCEAN_URL}/{VOLUMES_RESOURCE}/actions', headers=AUTH_HEADER, json={
        "type": "attach",
        "volume_name": SINGLE_MACHINE_VOLUME_NAME,
        "droplet_id": droplet_id
    })
    if not response.ok:
        print_and_exit(f'''Fail to attach volume!
        Code: {response.status_code}
        Body: {response.text}''')

def create_and_assign_firewall_if_needed(droplet_names_ids):
    firewall_id = None
    assigned_droplet_ids = []
    for f in get_resources(FIREWALLS_RESOURCE):
        if f[NAME] == FIREWALL_NAME:
            firewall_id = f[ID]
            assigned_droplet_ids = f["droplet_ids"]
            break

    
    if firewall_id:
        print("Firewall exists, skipping its creation!")
    else:
        print("Firewall does not exist, creating it..")
        create_resource(FIREWALLS_RESOURCE, "firewall", firewall_config)
        print("Firewall created!")

    droplet_ids = droplet_names_ids.values()
    to_assign_droplet_ids = [did for did in droplet_ids if did not in assigned_droplet_ids]

    if len(to_assign_droplet_ids) > 0:
        print(f"{len(to_assign_droplet_ids)} droplets are not assinged to firewall, assigning them...")
        assign_firewall(firewall_id, to_assign_droplet_ids)
        print("Droplets assigned!")
    else:
        print("Droplets are assigned to firewalls already!")


def assign_firewall(firewall_id, droplet_ids):
    response = requests.post(f'{DIGITAL_OCEAN_URL}/{FIREWALLS_RESOURCE}/{firewall_id}/{DROPLETS_RESOURCE}',
                                headers=AUTH_HEADER, 
                                json={ "droplet_ids": droplet_ids })
    if not response.ok:
        print_and_exit(f'''Fail to assign firewall to droplets!
        Code: {response.status_code}
        Body: {response.text}''')


print("Creating droplets, if needed...")

droplet_names_ids = create_droplets_if_needed()
print("...")
time.sleep(1)

print("Droplets prepared, creating and attaching volume if needed...")

create_and_attach_volume_if_needed(droplet_names_ids)
print("...")
time.sleep(1)

print("Volume created and attached, creating and assigning firewall if needed...")

create_and_assign_firewall_if_needed(droplet_names_ids)

print("...")

print("Everything should be ready!")
print("Get your machine addresses from DigitalOcean UI and start experimenting!")