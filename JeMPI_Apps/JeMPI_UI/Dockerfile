# FROM node:16-alpine

# WORKDIR /app

# COPY . .

# RUN yarn install

# RUN yarn build

# RUN yarn global add serve

# CMD serve -s build  


# Build 
FROM node:16-alpine as development-stage

WORKDIR /app

COPY package.json ./

COPY yarn.lock ./

RUN yarn

COPY . .

RUN yarn build

CMD ["yarn","start"]

# Production 
FROM node:16-alpine as production-stage

WORKDIR /app

COPY package.json ./

COPY yarn.lock ./

RUN yarn global add serve

RUN yarn install --production=true

COPY --chown=node:node --from=development-stage /app/build ./build

ENV NODE_ENV=production
ENV REACT_APP_JEMPI_BASE_URL=$REACT_APP_JEMPI_BASE_URL

CMD ["serve" ,"-s", "build"]