# online-tutoring-app-backend
Backend Rest API Service Powered by Java and Spring for the Online Tutoring Application

## SETUP -
1. Download Java version 17
2. [Download intellij IDEA](https://www.jetbrains.com/idea/download/#section=windows)
3. `mvn clean install` [This is a MAVEN PROJECT]

## Database - 
The Application uses Postgres DB and is hosted on AWS

Credentials -
* host-name: `dev-db.cnyraed9rw6a.us-east-2.rds.amazonaws.com`
* port: `5432`
* username: `crud`
* password: `crudpass`

### NOTE -
A lot of NPE CHECKS are not being done because the APIs should be fast!
(Planning to fix it once we have caching enabled)