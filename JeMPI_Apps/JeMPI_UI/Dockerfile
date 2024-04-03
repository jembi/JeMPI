# Development
FROM node:18-alpine as development-stage

WORKDIR /app

# Copy package.json and yarn.lock for installation
COPY package.json yarn.lock ./

# Install dependencies
RUN yarn install --frozen-lockfile

# Copy the rest of the application source code
COPY . .

# Command to start development server
CMD ["yarn", "start"]

# Build
FROM development-stage as build-stage

# Build the application
RUN yarn build

# Production
FROM node:18-alpine as production-stage

# Install runtime dependencies
RUN apk --no-cache add gettext libintl

WORKDIR /app

# Install serve globally
RUN yarn global add serve

# Copy built application from build-stage
COPY --from=build-stage /app/build ./

COPY ./docker-entrypoint.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/docker-entrypoint.sh

ENTRYPOINT [ "/bin/sh", "/usr/local/bin/docker-entrypoint.sh" ]
