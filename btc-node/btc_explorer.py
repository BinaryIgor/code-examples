import subprocess as sp
from os import environ
import sys
import json
from datetime import datetime, UTC

bitcoin_cli_cmd = environ.get("BITCOIN_CLI_CMD", "bitcoin-cli")

RAW_CMD = "raw_cmd"
GET_BLOCK_HASH = "get_block_hash"
SEARCH_TRANSACTION_IN_BLOCK = "search_transaction_in_block"
GET_PEERS_INFO = "get_peers_info"

def run_bitcoin_cli(cmd, json_output=False):
    output = execute_script(f"{bitcoin_cli_cmd} {cmd}")
    return json.loads(output) if json_output else output

def execute_script(script, exit_on_failure=True):
    try:
        result = sp.run(script, shell=True, stdout=sp.PIPE, stderr=sp.PIPE)
        code = result.returncode
        if code == 0:
            return result.stdout.decode().strip()

        if exit_on_failure:
            print(f"Fail to execute script: {script}, exiting with code: {code}")
            print("Stdout:")
            print(result.stdout.decode())
            print("Stderr:")
            print(result.stderr.decode())
            sys.exit(code)
        else:
            raise Exception(f"Fail to execute script ({code}): {script}")
    except KeyboardInterrupt:
        print("Script execution interrupted by the user, exiting")
        sys.exit(0)

def get_block_hash(offset):
    block_count = int(run_bitcoin_cli("getblockcount"))
    return run_bitcoin_cli(f"getblockhash {block_count - offset}")

def search_transaction_in_block(block_hash, search):
    block_info = run_bitcoin_cli(f"getblock {block_hash}", json_output=True)
    tx_count = len(block_info["tx"])
    for idx, tx_id in enumerate(block_info["tx"]):
        transaction = run_bitcoin_cli(f"getrawtransaction {tx_id} true", json_output=True)

        for vin in transaction["vin"]:
            # coinbase transactions do not have scriptSig
            script_sig = vin.get("scriptSig")
            if not script_sig:
                continue
            if search in script_sig.get("hex", "") or search in script_sig.get("address", ""):
                return transaction

        for vout in transaction['vout']:
            script_pub_key = vout['scriptPubKey']
            if search in script_pub_key.get("desc", "") \
                    or search in script_pub_key.get("hex", "") \
                    or search in script_pub_key.get("address", ""):
             return transaction

        if idx > 0 and idx % 100 == 0:
            print(f"Checked {idx}/{tx_count} transactions...")

    return None

def get_peers_info():
    def peer_str(peer):
        return f'''
Peer {peer["id"]}, {peer["connection_type"]} connection
address: {peer["addr"]}, network: {peer["network"]}, version(-sub): {peer["version"]}-{peer["subver"]}'''

    peers = run_bitcoin_cli("getpeerinfo", json_output=True)
    peers_info = "\n".join(peer_str(p) for p in peers)
    inbound_peer_count = sum(1 if p["inbound"] else 0 for p in peers)
    return f"""
Time: {datetime.now(UTC).isoformat()}
All peers: {len(peers)}
Inbound peers: {inbound_peer_count} 
    
{peers_info.strip()}"""

if len(sys.argv) <= 1:
    print("At least one option or command needs to be specified!")
    sys.exit(1)


cmd_args = sys.argv[1:]
option = cmd_args[0]
if option == GET_BLOCK_HASH:
    offset = 0 if len(cmd_args) == 1 else int(cmd_args[1])
    block_hash = get_block_hash(offset)
    print(f"Block hash of -{offset} offset from the latest block")
    print(block_hash)
elif option == SEARCH_TRANSACTION_IN_BLOCK:
    if len(cmd_args) < 3:
        print(f"For {option} option, second argument with a block hash and third with a search phrase are required")
        sys.exit(1)
    block_hash = cmd_args[1]
    search_phrase = cmd_args[2]
    print(f"Using {search_phrase} search to match a transaction in {block_hash} block.")
    print("This might take a while...")
    info = search_transaction_in_block(block_hash, search_phrase)
    if info:
        print(info)
    else:
        print(f"There are no transactions associated with {search_phrase} in {block_hash} block")
elif option == GET_PEERS_INFO:
    info = get_peers_info()
    print(info.strip())
else:
    if option != RAW_CMD:
        cmd = option
        params = cmd_args[1:] if len(cmd_args) > 1 else []
    else:
        cmd = cmd_args[1]
        params = cmd_args[2:] if len(cmd_args) > 2 else []

    params_str = " ".join(params) if params else ""
    cmd_result = run_bitcoin_cli(f"{cmd} {params_str}".strip())
    print(cmd_result)
