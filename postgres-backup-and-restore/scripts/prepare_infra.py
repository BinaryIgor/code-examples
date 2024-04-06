import requests
from os import environ
import sys
import time

MACHINE_NAME = "postgres-backup-and-restore"
# full machine slugs reference: https://slugs.do-api.dev/
MACHINE_SLUG = "s-1vcpu-2gb-amd"

DIGITAL_OCEAN_URL = "https://api.digitalocean.com/v2"

FIREWALL_NAME = f"{MACHINE_NAME}-firewall"

REGION = "fra1"
IMAGE = "ubuntu-22-04-x64"

USER_PLACEHOLDER = "_user_placeholder_"

DROPLETS_RESOURCE = "droplets"
FIREWALLS_RESOURCE = "firewalls"

ID = "id"
NAME = "name"

def print_and_exit(message):
    print(message)
    sys.exit(1)

API_TOKEN = environ.get("DO_API_TOKEN")
if API_TOKEN is None:
    print_and_exit("DO_API_TOKEN env variable needs to be supplied with valid digital ocean token")

SSH_KEY_FINGERPRINT = environ.get("SSH_KEY_FINGERPRINT")
if SSH_KEY_FINGERPRINT is None:
    print_and_exit("SSH_KEY_FINGERPRINT env variable needs to be supplied with ssh key fingerprint that will give you access to droplets!")


AUTH_HEADER = {"Authorization": f"Bearer {API_TOKEN}"}

with open("init_machine.bash") as f:
    init_machine = f.read().replace(USER_PLACEHOLDER, "deploy")


# To debug user data, run: 
# cat /var/log/cloud-init-output.log | grep userdata 
# ...on the droplet
machine_config = {
    "name": MACHINE_NAME,
    "region": REGION,
    "size": MACHINE_SLUG,
    "image": IMAGE,
    "ssh_keys": [SSH_KEY_FINGERPRINT],
    "backups": False,
    "ipv6": True,
    "monitoring": True,
    "user_data": init_machine
}

firewall_all_addresses =  {
    "addresses": [
        "0.0.0.0/0",
        "::/0"
    ]
}
# Basic firewall so nobody is bothering us during tests
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


def create_droplet_if_needed():
    droplet_name_id = {}

    for d in get_resources(DROPLETS_RESOURCE):
        d_name = d[NAME]
        if d_name == MACHINE_NAME:
            droplet_name_id[MACHINE_NAME] = d[ID]

    # Eventual consistency of Digital Ocean: sometimes new droplets are not visible immediately after creation
    new_droplet_names = []

    if MACHINE_NAME in droplet_name_id:
        print("Machine exists, skipping its creation!")
    else:
        new_droplet_names.append(MACHINE_NAME)
        print(f"Creating {MACHINE_NAME}...")
        created_droplet = create_resource(DROPLETS_RESOURCE, "droplet", machine_config)
        droplet_name_id[MACHINE_NAME] = created_droplet[ID]
        print(f"{MACHINE_NAME} created!")

    if len(new_droplet_names) == 0:
        return droplet_name_id
    
    print()

    while True:
        for d in get_resources(DROPLETS_RESOURCE):
            d_status = d['status']
            d_name = d[NAME]
            if d_status == 'active' and d_name in new_droplet_names:
                new_droplet_names.remove(d_name)

        if new_droplet_names:
            print(f"Waiting for {new_droplet_names} droplet to become active...")
            time.sleep(5)
        else:
            print()
            print("Needed droplet is active!")
            print()
            break

    return droplet_name_id


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
        created_firewall = create_resource(FIREWALLS_RESOURCE, "firewall", firewall_config)
        firewall_id = created_firewall[ID]
        print("Firewall created!")
        time.sleep(1)

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


print("Needed droplet:")
print(f"One machne: {MACHINE_SLUG}")
print()

print("Creating droplet, if needed...")

droplet_name_id = create_droplet_if_needed()
print()
print("...")
print()
time.sleep(1)

print("Droplet prepared, creating and assigning firewall if needed..")

create_and_assign_firewall_if_needed(droplet_name_id)
print()
print("...")
print()

print("Everything should be ready!")
print()
print("Get your machine addresses from DigitalOcean UI and start experimenting!")