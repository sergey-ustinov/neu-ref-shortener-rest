FROM swaggerapi/swagger-ui:latest

ENV API_URL='url-shortener-api.yml'
COPY src/swagger/url-shortener-api.yml /usr/share/nginx/html/
RUN chmod -R a+rw /usr/share/nginx/