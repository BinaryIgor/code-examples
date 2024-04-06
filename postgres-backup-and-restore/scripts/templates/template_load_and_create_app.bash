#!/bin/bash
tagged_image="${app}:${tag}"
gzipped_image_path="${app}.tar.gz"

echo "Loading ${tagged_image} image, this can take a while..."
docker load < ${gzipped_image_path}
echo "Image loaded, creating it..."
exec bash create_app.bash