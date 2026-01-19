FROM wiremock/wiremock:3x

COPY build/libs/wiremock-faker-extension-standalone-1.0.0-SNAPSHOT.jar \
     /var/wiremock/extensions/

EXPOSE 8080 5005
