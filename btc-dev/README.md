* Why are there so many deps?
* deps - self compile in a deterministic way


Build deps:
```
cd depends
export NO_QT=1
make
```

Build core, using previously compiled deps:
```
rm -rf build/

cmake --build build --target clean

cmake -B build \
  -DCMAKE_TOOLCHAIN_FILE=$PWD/depends/x86_64-pc-linux-gnu/toolchain.cmake \
  -DCMAKE_PREFIX_PATH=$PWD/depends/x86_64-pc-linux-gnu \
  -DENABLE_WALLET=OFF

cmake -B build \
  -DCMAKE_TOOLCHAIN_FILE=depends/x86_64-pc-linux-gnu/toolchain.cmake \
  -DENABLE_WALLET=OFF

cmake --build build -j$(nproc)
ctest --test-dir build

cmake --build build --target bitcoind
``` 

Cache:
```
root@ba3103b6ad59:/home/builder/bitcoin# ccache -s
Local storage:
  Cache size (GiB): 0.0 / 5.0 ( 0.00%)

root@ba3103b6ad59:/home/builder/bitcoin# ccache -s
Cacheable calls:    426 / 426 (100.0%)
  Hits:               0 / 426 ( 0.00%)
    Direct:           0
    Preprocessed:     0
  Misses:           426 / 426 (100.0%)
Local storage:
  Cache size (GiB): 0.3 / 5.0 ( 5.26%)
  Hits:               0 / 426 ( 0.00%)
  Misses:           426 / 426 (100.0%)
```

Run on signet/testnet:
```
./build/bin/bitcoind -signet
./build/bin/bitcoind -testnet
```
