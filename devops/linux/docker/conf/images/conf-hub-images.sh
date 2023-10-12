
# https://docs.docker.com/registry/
# https://registry.hub.docker.com/_/registry/tags
export REGISTRY_IMAGE=registry:2

# https://hub.docker.com/r/bitnami/keycloak/tags
export KEYCLOAK_IMAGE=bitnami/keycloak:21.1.2

# https://registry.hub.docker.com/r/bitnami/kafka/tags
export KAFKA_IMAGE=bitnami/kafka:3.5

# https://registry.hub.docker.com/r/dgraph/dgraph/tags
export DGRAPH_IMAGE=dgraph/dgraph:v23.1.0

# https://registry.hub.docker.com/r/dgraph/ratel/tags
export RATEL_IMAGE=dgraph/ratel:v21.03.2

# https://registry.hub.docker.com/r/bitnami/postgresql/tags
export POSTGRESQL_IMAGE=bitnami/postgresql:15.4.0

# https://hub.docker.com/r/haproxytech/haproxy-debian
export HAPROXY_IMAGE=haproxytech/haproxy-debian:2.8

# https://hub.docker.com/_/nginx/tags
#export NGINX_IMAGE=nginx:1.25.2
#export NGINX_IMAGE=bitnami/nginx:latest

# Javascript runtime image (nodeJs, DenoJs, BunJs, ect ...)
export JAVASCRIPT_RUNTIME=node:18-alpine