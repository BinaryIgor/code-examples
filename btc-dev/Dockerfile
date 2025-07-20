FROM ubuntu:24.04

# Noninteractive apt installs
ENV DEBIAN_FRONTEND=noninteractive

# Install build tools & minimal base deps (not Bitcoin's, just for build)
# For GUI you need to add a few more, as specified in https://github.com/bitcoin/bitcoin/blob/master/depends/README.md
RUN apt-get update && apt-get install -y \
    build-essential \
    curl \
    cmake \
    make \
    patch \
    ccache

# Use ccache (c++ cache) to speed up recompilations
ENV CCACHE_DIR=/ccache
ENV PATH="/usr/lib/ccache:$PATH"

# Create user (optional)
RUN useradd -ms /bin/bash builder
# USER builder
WORKDIR /home/builder

# Entrypoint shell
CMD ["/bin/bash"]

