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

WORKDIR /app

RUN yarn global add serve

COPY --chown=node:node --from=build-stage /app/build ./build

CMD ["serve" , "-s", "build"]