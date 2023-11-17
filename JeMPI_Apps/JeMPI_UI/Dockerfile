# Development 

FROM node:18-alpine as development-stage

WORKDIR /app
COPY package.json ./
COPY yarn.lock ./

ENV NODE_ENV=development

RUN yarn

COPY . .

CMD ["yarn", "start"]

# Build

FROM node:18-alpine as build-stage

WORKDIR /app

COPY package.json ./

COPY yarn.lock ./

ENV NODE_ENV=production

RUN yarn

COPY . .

RUN yarn build

# Production

FROM node:18-alpine as production-stage

ENV BUILD_DEPS="gettext"  \
    RUNTIME_DEPS="libintl"

RUN set -x && \
    apk add --update $RUNTIME_DEPS && \
    apk add --virtual build_deps $BUILD_DEPS &&  \
    cp /usr/bin/envsubst /usr/local/bin/envsubst && \
    apk del build_deps

WORKDIR /app

RUN yarn global add serve

COPY --chown=node:node --from=build-stage /app/build ./

COPY ./docker-entrypoint.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/docker-entrypoint.sh

ENTRYPOINT [ "/bin/sh", "/usr/local/bin/docker-entrypoint.sh" ]

