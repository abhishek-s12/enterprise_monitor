FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm install --legacy-peer-deps --no-audit

COPY . .
RUN npm run build -- --configuration production

FROM nginx:alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/admin-dashboard/browser /usr/share/nginx/html

EXPOSE 4200

CMD ["nginx", "-g", "daemon off;"]
