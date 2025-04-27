FROM node:alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install

COPY src src
COPY .env .env
COPY tsconfig.tsbuildinfo tsconfig.tsbuildinfo
COPY tsconfig.json tsconfig.json

COPY public public
RUN npm run build


FROM nginx:alpine

COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf etc/nginx/nginx.conf

CMD ["nginx", "-g", "daemon off;"]