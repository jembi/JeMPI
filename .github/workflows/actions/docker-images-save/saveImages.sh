#!/bin/bash

images_path="$2"

if [ ! -d "$images_path" ]; then
    mkdir -p "$images_path"
fi

IMAGE_LIST=$(docker image ls --filter "reference=*:$1" --format "{{.Repository}}:{{.Tag}}")

for IMAGE in $IMAGE_LIST; do
    IFS=':' read -a image_details <<< "$IMAGE"
    echo "Saving image: $IMAGE to '$images_path/${image_details[0]}.${image_details[1]}.tar'"
    docker save -o "$images_path/${image_details[0]}.${image_details[1]}.tar" "$IMAGE"
done