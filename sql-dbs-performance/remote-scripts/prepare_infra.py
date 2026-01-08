#!/usr/bin/env python3
import sys
import time
from os import environ

import requests

USER_PLACEHOLDER = "_user_placeholder_"

VPC_RESOURCE = "vpcs"
DROPLETS_RESOURCE = "droplets"
FIREWALLS_RESOURCE = "firewalls"

ID = "id"
NAME = "name"

# full machine slugs reference: https://slugs.do-api.dev/
# https://www.digitalocean.com/blog/premium-droplets-intel-cascade-lake-amd-epyc-rome
TESTS_MACHINE_SLUG = "s-8vcpu-16gb-amd"
DB_MACHINE_SLUG = "s-8vcpu-16gb-amd"


def print_and_exit(message):
    print(message)
    sys.exit(1)


DIGITAL_OCEAN_URL = "https://api.digitalocean.com/v2"

VPC_NAME = "sql-dbs-performance-vpc"
TESTS_MACHINE_NAMES = [
    "sql-dbs-performance-mysql-tests",
    "sql-dbs-performance-postgresql-tests",
    "sql-dbs-performance-mariadb-tests"
]
DB_MACHINE_NAMES = [
    "sql-dbs-performance-mysql",
    "sql-dbs-performance-postgresql",
    "sql-dbs-performance-mariadb"
]
ALL_MACHINE_NAMES = TESTS_MACHINE_NAMES + DB_MACHINE_NAMES
FIREWALL_NAME = "sql-dbs-performance-firewall"

REGION = "fra1"
IMAGE = "ubuntu-24-04-x64"

API_TOKEN = environ.get("DO_API_TOKEN")
if API_TOKEN is None:
    print_and_exit("DO_API_TOKEN env variable needs to be supplied with a valid digital ocean token!")

SSH_KEY_FINGERPRINT = environ.get("SSH_KEY_FINGERPRINT")
if SSH_KEY_FINGERPRINT is None:
    print_and_exit(
        "SSH_KEY_FINGERPRINT env variable needs to be supplied with a ssh key fingerprint, giving you access to droplets!")

AUTH_HEADER = {"Authorization": f"Bearer {API_TOKEN}"}

with open("init_machine.bash") as f:
    INIT_MACHINE_SCRIPT = f.read().replace(USER_PLACEHOLDER, "ops")

vpc_config = {
    "name": VPC_NAME,
    "description": "VPC for internal communication",
    "region": REGION
}


# To debug user data, run:
# cat /var/log/cloud-init-output.log | grep userdata
# ...on the droplet
def machine_config(name, size, vpc_id):
    return {
        "name": name,
        "region": REGION,
        "size": size,
        "image": IMAGE,
        "ssh_keys": [SSH_KEY_FINGERPRINT],
        "backups": False,
        "ipv6": True,
        "vpc_uuid": vpc_id,
        "monitoring": True,
        "user_data": INIT_MACHINE_SCRIPT
    }


firewall_all_addresses = {
    "addresses": [
        "0.0.0.0/0",
        "::/0"
    ]
}


# Basic firewall so nobody is bothering us during tests
def firewall_config(internal_ip_range):
    internal_addresses = {
        "addresses": [
            internal_ip_range
        ]
    }
    # 0 means all ports
    return {
        "name": FIREWALL_NAME,
        "inbound_rules": [
            {
                "protocol": "tcp",
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
                "sources": internal_addresses
            },
            {
                "protocol": "tcp",
                "ports": "443",
                "sources": internal_addresses
            },
            # dbs
            {
                "protocol": "tcp",
                "ports": "3306",
                "sources": internal_addresses
            },
            {
                "protocol": "tcp",
                "ports": "5432",
                "sources": internal_addresses
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


def create_droplets_if_needed(vpc_id):
    droplet_name_ids = {d[NAME]: d[ID] for d in get_resources(DROPLETS_RESOURCE)}

    new_droplet_names = []

    for m in ALL_MACHINE_NAMES:
        if m in droplet_name_ids:
            print(f"{m} exists, skipping its creation")
        else:
            new_droplet_names.append(m)
            print(f"Creating {m}...")
            machine_size = TESTS_MACHINE_SLUG if m in TESTS_MACHINE_NAMES else DB_MACHINE_SLUG
            created_droplet = create_resource(DROPLETS_RESOURCE, "droplet", machine_config(m, machine_size, vpc_id))
            droplet_name_ids[m] = created_droplet[ID]
            print(f"{m} created!")

    if len(new_droplet_names) == 0:
        return droplet_name_ids

    print()

    # Eventual consistency of Digital Ocean: sometimes new droplets are not visible immediately after creation
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

    return droplet_name_ids


def create_vpc_if_needed():
    for vpc in get_resources(VPC_RESOURCE):
        v_name = vpc[NAME]

        if v_name == VPC_NAME:
            print("VPC exists, skipping its creation")
            return vpc['ip_range'], vpc[ID]

    print("VPC does not exist, creating it...")

    created_vpc = create_resource(VPC_RESOURCE, "vpc", vpc_config)
    print("VPC created!")

    return created_vpc["ip_range"], created_vpc[ID]


def create_and_assign_firewall_if_needed(droplet_name_ids, internal_ip_range):
    firewall_id = None
    assigned_droplet_ids = []
    for f in get_resources(FIREWALLS_RESOURCE):
        if f[NAME] == FIREWALL_NAME:
            firewall_id = f[ID]
            assigned_droplet_ids = f["droplet_ids"]
            break

    if firewall_id:
        print("Firewall exists, skipping its creation")
    else:
        print("Firewall does not exist, creating it...")
        created_firewall = create_resource(FIREWALLS_RESOURCE, "firewall", firewall_config(internal_ip_range))
        firewall_id = created_firewall[ID]
        print("Firewall created!")
        time.sleep(1)

    droplet_ids = droplet_name_ids.values()
    to_assign_droplet_ids = [did for did in droplet_ids if did not in assigned_droplet_ids]

    if len(to_assign_droplet_ids) > 0:
        print(f"{len(to_assign_droplet_ids)} droplets are not assigned to the firewall, assigning them...")
        assign_firewall(firewall_id, to_assign_droplet_ids)
        print("Droplets assigned!")
    else:
        print("Droplets are assigned to firewalls already!")


def assign_firewall(firewall_id, droplet_ids):
    response = requests.post(f"{DIGITAL_OCEAN_URL}/{FIREWALLS_RESOURCE}/{firewall_id}/{DROPLETS_RESOURCE}",
                             headers=AUTH_HEADER,
                             json={"droplet_ids": droplet_ids})
    if not response.ok:
        print_and_exit(f'''Fail to assign firewall to droplets!
        Code: {response.status_code}
        Body: {response.text}''')


print("Needed droplets:")
print(f"Tests machines ({len(TESTS_MACHINE_NAMES)}): {TESTS_MACHINE_SLUG}")
print(f"DB machines ({len(DB_MACHINE_NAMES)}): {DB_MACHINE_SLUG}")
print()

print("Creating nad setting up virtual private network (VPC) if needed...")
internal_ip_range, vpc_id = create_vpc_if_needed()
print()
print("...")
print()
time.sleep(1)

print("VPC is prepared, creating droplets if needed...")

droplet_name_ids = create_droplets_if_needed(vpc_id)
print()
print("...")
print()
time.sleep(1)

print("Droplets prepared, creating and setting up firewall if needed")

create_and_assign_firewall_if_needed(droplet_name_ids, internal_ip_range)
print()
print("...")
print()

print("Everything should be ready!")
print()
print("Get your machine addresses from DigitalOcean UI and start experimenting!")
