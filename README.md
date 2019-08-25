# Moneyger

* [Demo](https://moneyger.cfapps.io)
* [API Doc](https://moneyger.cfapps.io/docs/index.html)
* [Swagger UI](https://moneyger.cfapps.io/docs/swagger-ui.html)

```
mvn clean package -Drestdoc.scheme=https \
   -Drestdoc.host=moneyger.cfapps.io \
   -Drestdoc.port=443 && \
java -jar target/moneyger-1.0-SNAPSHOT.jar
```

```
cf create-service elephantsql turtle moneyger-db
cf push
```