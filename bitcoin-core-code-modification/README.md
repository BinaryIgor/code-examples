# Bitcoin Core Code: let's modify it

What is the Bitcoin Core?

It is the reference client and the reference implementation of all aspects of the Bitcoin system.

Let's modify it!

Plan of action:
* build the codebase to have a stable reference point
* start the bitcoin node in the reg test (regression test) mode; play with some RPC (Remote Procedure Call) commands
* modify the code and add our own RPC command - `getmagicnumber`
* recompile, run the node again and check that it all works as intended
* write some functional (integration) tests to additionally future-prove that it works

For the additional context, my platform (OS + CPU architecture) is `Ubuntu 24.04.3 LTS, AMD Ryzen™ 7 PRO 7840U w/ Radeon™ 780M Graphics × 16`.

## Build the codebase

First, clone the repo:
```
git clone https://github.com/bitcoin/bitcoin
```

Then, make sure that you have [required dependencies](https://github.com/bitcoin/bitcoin/blob/master/depends/README.md), which for my platform are:
```
apt install cmake curl make patch
```
On top of that, you need to have a C++ compiler, [GCC](https://gcc.gnu.org/) for me, and a more or less new Python interpreter, to run functional tests.

Having those prerequisites, we can build dependencies from sources, using Bitcoin Core's *depends* build system (from the `depends` dir):
```
# skip GUI deps
export NO_QT=1
# adjust to your desired level of parallelism
make -j8
```

Having required dependencies, we can build the Bitcoin Core (from the root dir):
````
# wallet, but without GUI; change toolchain file to your specific path
cmake -B build \
  -DCMAKE_TOOLCHAIN_FILE=depends/x86_64-pc-linux-gnu/toolchain.cmake \
  -DENABLE_WALLET=ON \
  -DBUILD_GUI=OFF
  
# make files are generated; let's compile the Core (remember about parallelism)
cmake --build build -j8  
````

## Run bitcoin node locally and execute some RPC commands

To start a node in the local, test network (from the `build` dir):
```
./bin/bitcoind -regtest
```

The node should be up and running; we can issue some commands to it using `bitcoin-cli` binary:
```
./bin/bitcoin-cli -regtest getblockcount
0

./bin/bitcoin-cli -regtest getblockchaininfo
{
  "chain": "regtest",
  "blocks": 0,
  "headers": 0,
  "bestblockhash": "0f9188f13cb7b2c71f2a335e3a4fc328bf5beb436012afca590b1a11466e2206",
  "bits": "207fffff",
  "target": "7fffff0000000000000000000000000000000000000000000000000000000000",
  "difficulty": 4.656542373906925e-10,
  "time": 1296688602,
  "mediantime": 1296688602,
  "verificationprogress": 2.174604298183246e-06,
  "initialblockdownload": true,
  "chainwork": "0000000000000000000000000000000000000000000000000000000000000002",
  "size_on_disk": 293,
  "pruned": false,
  "warnings": [
    "This is a pre-release test build - use at your own risk - do not use for mining or merchant applications"
  ]
}
```

As we might notice, the blockchain is empty. In the regression test mode, it's possible to generate blocks with 0 difficulty:
```
# generate new wallet and address to get some BTC
./bin/bitcoin-cli -regtest createwallet "test"
{
  "name": "test"
}
./bin/bitcoin-cli -regtest getnewaddress
bcrt1q8pnpgd0r2hcf9tynkz6gkh249p706ejfx0rv79

# mine 200 blocks and send them to our address
./bin/bitcoin-cli -regtest generatetoaddress 200 bcrt1q8pnpgd0r2hcf9tynkz6gkh249p706ejfx0rv79
```

Now, we have 200 blocks mined; there are some stats to analyze about the blockchain:
```
./bin/bitcoin-cli -regtest getblockcount
200

./bin/bitcoin-cli -regtest getblockhash 100
46747dc7740039ba46d4cc5ab41c7c841c793ac1950c6b8cc56ccca6bb1ae782

./bin/bitcoin-cli -regtest getblock 46747dc7740039ba46d4cc5ab41c7c841c793ac1950c6b8cc56ccca6bb1ae782
{
  "hash": "46747dc7740039ba46d4cc5ab41c7c841c793ac1950c6b8cc56ccca6bb1ae782",
  "confirmations": 101,
  "height": 100,
  "version": 536870912,
  "versionHex": "20000000",
  "merkleroot": "e8c326c7e4bb16b6c04a1bb8bbffe2e9d63c6c1f0f0a3c3bd7fc01fa4d194a6f",
  "time": 1756541694,
  "mediantime": 1756541693,
  "nonce": 0,
  "bits": "207fffff",
  "target": "7fffff0000000000000000000000000000000000000000000000000000000000",
  "difficulty": 4.656542373906925e-10,
  "chainwork": "00000000000000000000000000000000000000000000000000000000000000ca",
  "nTx": 1,
  "previousblockhash": "54a9008e2fba252c60cecfa4f7cfb84b8fe2d802083ecc9a15977173d2b67f51",
  "nextblockhash": "4926e9ce037ea9afdefc26a25c961b11409506b2d393dd03e75abf0533515210",
  "strippedsize": 213,
  "size": 249,
  "weight": 888,
  "tx": [
    "e8c326c7e4bb16b6c04a1bb8bbffe2e9d63c6c1f0f0a3c3bd7fc01fa4d194a6f"
  ]
}
```

Knowing more or less how the RPC commands work, let's add our own!

## Modify the code

In the `rpc/register.h` file, all RPC commands are being registered; let's declare and add our own:
```
...
void RegisterMagicNumberRPCCommands(CRPCTable&);

static inline void RegisterAllCoreRPCCommands(CRPCTable &t)
{
    ...
    RegisterMagicNumberRPCCommands(t);
    ...
}
```

That's the declaration part - we now must implement it somewhere. Create `rpc/magicnumber.cpp` file:
```
#include <rpc/server.h>
#include <rpc/util.h>

static RPCHelpMan getmagicnumber() {
    return RPCHelpMan{
        "getmagicnumber",
        "returns a magic number",
        {
            {"multiplier", RPCArg::Type::NUM, RPCArg::Optional::NO, "A multiplier, making magic number even more magical; must be positive!"},
        },
        RPCResult{RPCResult::Type::NUM, "", "A magic number"},
        RPCExamples{
            HelpExampleCli("getmagicnumber", "5")
            + HelpExampleRpc("getmagicnumber", "5")
        },
        [&](const RPCHelpMan &self, const JSONRPCRequest &request) -> UniValue {
            auto multiplier = request.params[0].getInt<int>();
            if (multiplier < 1) {
                throw JSONRPCError(RPC_INVALID_PARAMETER, "Multiplier must be positive!");
            }
            return UniValue(multiplier * 437);
        }
    };
}

void RegisterMagicNumberRPCCommands(CRPCTable &t) {
    static const CRPCCommand commands[]{
        {"util", &getmagicnumber},
    };
    for (const auto &c: commands) {
        t.appendCommand(c.name, &c);
    }
}
```

To have our `multiplier` arg properly parsed as an integer, we must register it in the `rpc/client.cpp` file:
```
/**
 * Specify a (method, idx, name) here if the argument is a non-string RPC
 * argument and needs to be converted from JSON.
 *
 * @note Parameter indexes start from 0.
 */
static const CRPCConvertParam vRPCConvertParams[] =
{
    ...
    { "getmagicnumber", 0, "multiplier" },
};
```
Next, we should modify `src/CMakeLists.txt` to have it all linked for the compilation:
```
add_library(bitcoin_node STATIC EXCLUDE_FROM_ALL
  ...
  rpc/magicnumber.cpp
  ...
)
```

We can now compile our changes:
```
cmake --build build -j8
```
And see that it simply works:
```
./bin/bitcoin-cli -regtest getmagicnumber 5
2185

./bin/bitcoin-cli -regtest help getmagicnumber
getmagicnumber multiplier

returns a magic number

Arguments:
1. multiplier    (numeric, required) A multiplier, making magic number even more magical; must be positive!

Result:
n    (numeric) A magic number

Examples:
> bitcoin-cli getmagicnumber 5
> curl --user myusername --data-binary '{"jsonrpc": "2.0", "id": "curltest", "method": "getmagicnumber", "params": [5]}' -H 'content-type: application/json' http://127.0.0.1:8332/
```

## Add a functional test

As we know how to modify the code, let's also write an automated test to prove that it works. How can we do this?

In the codebase, we have unit and functional (integration) tests written in Python.
For our particular change, there is little value in unit testing it; let's write a functional test instead.

Create the following `test/functional/rpc_getmagicnumber.py` file:
```
#!/usr/bin/env python3
from test_framework.test_framework import BitcoinTestFramework
from test_framework.util import (
    assert_equal,
    assert_raises_rpc_error
)

class GetMagicNumberRPCTest(BitcoinTestFramework):
    def set_test_params(self):
        self.setup_clean_chain = True
        self.num_nodes = 1

    def run_test(self):
        node = self.nodes[0]

        self.log.info("Testing getmagicnumber with proper arg values")
        assert_equal(node.getmagicnumber(1), 437)
        assert_equal(node.getmagicnumber(10), 4370)

        self.log.info("Testing getmagicnumber arg validation")
        assert_raises_rpc_error(-8, "Multiplier must be positive!", node.getmagicnumber, 0)
        assert_raises_rpc_error(-8, "Multiplier must be positive!", node.getmagicnumber, -1)



if __name__ == '__main__':
    GetMagicNumberRPCTest(__file__).main()
```

To run it, make it executable and trigger test config generation first:
```
chmod +x test/functional/rpc_getmagicnumber.py

# wallet, but without GUI; change toolchain file to your specific path
cmake -B build \
  -DCMAKE_TOOLCHAIN_FILE=depends/x86_64-pc-linux-gnu/toolchain.cmake \
  -DENABLE_WALLET=ON \
  -DBUILD_GUI=OFF
```

Then (from the root dir):
```
build/test/functional/rpc_getmagicnumber.py
2025-08-30T09:05:33.643670Z TestFramework (INFO): PRNG seed is: 1759730082586944849
2025-08-30T09:05:33.644149Z TestFramework (INFO): Initializing test directory /tmp/bitcoin_func_test_pw5q0olw
2025-08-30T09:05:33.900422Z TestFramework (INFO): Testing getmagicnumber with proper arg values
2025-08-30T09:05:33.901158Z TestFramework (INFO): Testing getmagicnumber arg validation
2025-08-30T09:05:33.952558Z TestFramework (INFO): Stopping nodes
2025-08-30T09:05:34.104615Z TestFramework (INFO): Cleaning up /tmp/bitcoin_func_test_pw5q0olw on exit
2025-08-30T09:05:34.104731Z TestFramework (INFO): Tests successful
```

We should also add it to the `test/functional/test_runner.py` so that it runs together with all other tests:
```
...
BASE_SCRIPTS = [
    ...
    'rpc_getmagicnumber.py',
    ... 
]
```
And then, we might run all functional tests and see our own as well:
```
build/test/functional/test_runner.py

...
259/273 - rpc_getmagicnumber.py passed, Duration: 1 s
...
```