#!/bin/bash

original_tag=$1
push_tag=$2
registry_url=$3
username=$4
password=$5

if [ -z "$registry_url" ] || [ -z "$username" ] || [ -z "$password" ]; then
    echo "Docker host details not set. Skipping deploying"
    exit 0
fi


if [ -z "$push_tag" ]; then
    push_tag=$original_tag
fi

if ! docker login "$registry_url" -u "$username" -p "$password"; then
    echo "Failed to authenticate with Docker registry. Cannot push."
    exit 1
fi


IMAGE_LIST=$(docker image ls --filter "reference=*:$original_tag" --format "{{.Repository}}:{{.Tag}}")

for IMAGE in $IMAGE_LIST; do
    IFS=':' read -a image_details <<< "$IMAGE"
    push_tag_url="$registry_url/$username/${image_details[0]}:$push_tag"

    echo "Pushing image: $IMAGE to '$push_tag_url'"

    docker tag "$IMAGE" $push_tag_url
    docker push $push_tag_url
done