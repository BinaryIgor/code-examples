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

# full machine slugs reference: https://slugs.do-api.dev/
STATIC_MACHINE_SIZE_SLUG = "s-1vcpu-2gb-amd"
TEST_MACHINE_SIZE_SLUG = "s-2vcpu-2gb-amd"

def print_and_exit(message):
    print(message)
    sys.exit(1)

DIGITAL_OCEAN_URL = "https://api.digitalocean.com/v2"

FIREWALL_NAME = "cdn-diff-firewall"

IMAGE = "ubuntu-22-04-x64"

API_TOKEN = environ.get("DO_API_TOKEN")
if API_TOKEN is None:
    print_and_exit("DO_API_TOKEN env variable needs to be supplied with valid digital ocean token")

SSH_KEY_FINGERPRINT = environ.get("SSH_KEY_FINGERPRINT")
if SSH_KEY_FINGERPRINT is None:
    print_and_exit("SSH_KEY_FINGERPRINT env variable needs to be supplied with ssh key fingerprint that will give you access to droplets!")


AUTH_HEADER = {"Authorization": f"Bearer {API_TOKEN}"}

with open("init_machine.bash") as f:
    init_machine_script = f.read().replace(USER_PLACEHOLDER, "deploy")

# To debug user data, run: 
# cat /var/log/cloud-init-output.log | grep userdata 
# ...on the droplet
def machine_config(region, name, size):
    return {
        "name": name,
        "region": region,
        "size": size,
        "image": IMAGE,
        "ssh_keys": [SSH_KEY_FINGERPRINT],
        "backups": False,
        "ipv6": True,
        "monitoring": True,
        "user_data": init_machine_script
}

machines = [
    machine_config("fra", "static-fra-droplet", STATIC_MACHINE_SIZE_SLUG),
    machine_config("fra", "test-fra-droplet", TEST_MACHINE_SIZE_SLUG),
    machine_config("lon", "static-lon-droplet", STATIC_MACHINE_SIZE_SLUG),
    machine_config("tor", "static-tor-droplet", STATIC_MACHINE_SIZE_SLUG),
    machine_config("syd", "static-syd-droplet", STATIC_MACHINE_SIZE_SLUG)
]
machine_names = [m[NAME] for m in machines]

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

def create_droplets_if_needed():
    droplet_names_ids = {}

    for d in get_resources(DROPLETS_RESOURCE):
        d_name = d[NAME]
        if d_name in machine_names:
            droplet_names_ids[d_name] = d[ID]

    # Eventual consistency of Digital Ocean: sometimes new droplets are not visible immediately after creation
    new_droplet_names = []

    for m in machines:
        mn = m[NAME]
        if mn in droplet_names_ids:
            print(f"{mn} exists, skipping its creation!")
        else:
            new_droplet_names.append(mn)
            print(f"Creating {mn}...")
            created_droplet = create_resource(DROPLETS_RESOURCE, "droplet", m)
            droplet_names_ids[mn] = created_droplet[ID]
            print(f"{mn} created!")

    if len(new_droplet_names) == 0:
        return droplet_names_ids
    
    print()

    while True:
        for d in get_resources(DROPLETS_RESOURCE):
            d_status = d['status']
            d_name = d[NAME]
            if d_status == 'active' and d_name in new_droplet_names:
                new_droplet_names.remove(d_name)

        if new_droplet_names:
            print(f"Waiting for {new_droplet_names} droplets to become active...")
            time.sleep(5)
        else:
            print()
            print("All droplets are active!")
            print()
            break

    return droplet_names_ids

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


print("Needed droplets:")
print(machine_names)
print()

print("Creating droplets, if needed...")

droplet_names_ids = create_droplets_if_needed()
print()
print("...")
print()
time.sleep(1)

print("Droplets prepared, reating and assigning firewall if needed...")

create_and_assign_firewall_if_needed(droplet_names_ids)
print()
print("...")
print()

print("Everything should be ready!")
print()
print("Get your machine addresses from DigitalOcean UI and start experimenting!")