FROM nginx:alpine
COPY build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html
EXPOSE 8080
CMD ["nginx", "-g", "damon off;"]