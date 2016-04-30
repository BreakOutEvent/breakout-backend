FROM frolvlad/alpine-oraclejdk8:slim
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN ./gradlew stage
CMD ["java", "-jar", "/usr/src/myapp/app.jar"]
EXPOSE 8082
