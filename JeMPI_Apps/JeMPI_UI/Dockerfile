# FROM node:18-alpine

# WORKDIR /app

# COPY . .

# RUN yarn install

# RUN yarn build

# RUN yarn global add serve

# CMD serve -s build  


# Build 
FROM node:18-alpine as development-stage

WORKDIR /app
COPY package.json ./
COPY yarn.lock ./

RUN yarn install

COPY . .
RUN yarn build

CMD ["yarn", "start"]

# Production
FROM node:18-alpine as production-stage

WORKDIR /app

COPY package.json ./

COPY yarn.lock ./

RUN yarn global add serve 

RUN yarn install --silent --prod --frozen-lockfile

COPY --chown=node:node --from=development-stage /app/build ./build

ENV NODE_ENV=production

CMD ["serve" , "-s", "build"]