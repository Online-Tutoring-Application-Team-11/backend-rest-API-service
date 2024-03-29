# online-tutoring-app-backend
Backend Rest API Service Powered by Java and Spring for the Online Tutoring Application

## SETUP -
`git clone git@github.com:Online-Tutoring-Application-Team-11/backend-rest-API-service.git`

1. Download Java version 17
2. [Download intellij IDEA](https://www.jetbrains.com/idea/download/#section=windows)
3. `mvn clean install` [This is a maven project so click on the green button to download dependencies]
<img width="885" alt="image" src="https://user-images.githubusercontent.com/55306116/236575280-2d5e5b0d-4940-49ca-b87e-132fbd3f9fad.png">

4. Replace the contents of the `src/main/application.properties` file with the passwords provided in the submission
5. Run the main function present in  `src/main/java/onlinetutoring/com/teamelevenbackend/TeamElevenBackendApplication.java` (click on the green button next to main to run the app)
<img width="1353" alt="image" src="https://user-images.githubusercontent.com/55306116/236575389-70f6d087-0c3e-45e8-a434-004e98cec2b9.png">


## Database - 
The Application uses Postgres DB and is hosted on AWS

Credentials -
* host-name: `CONTACT-ADMIN`
* port: `CONTACT-ADMIN`
* username: `CONTACT-ADMIN`
* password: `CONTACT-ADMIN`

### Deployment -
The backend is deployed using [railway](https://railway.app/dashboard)

#### ENVIRONMENTS - 

- production - `CONTACT-ADMIN`
- localhost - `http://localhost:8080`


> DO NOT PUSH TO PRODUCTION WITHOUT A PULL REQUEST


#### TESTING -
* You can use the `postman testing suite` with the app running locally

### INFRASTRUCTURE - 
```mermaid
sequenceDiagram
  participant React-FE
  participant Java-Spring-API
  participant AWS-SQL-DB
  participant Caching-Layer

  Java-Spring-API->>Caching-Layer: Replace cache every 10 minutes or bust cache if any update API call has been made

  React-FE->>Java-Spring-API: Send Request
  Java-Spring-API->>AWS-SQL-DB: Query Database
  AWS-SQL-DB-->>Java-Spring-API: Return Data
  Java-Spring-API-->>React-FE: Return Response

  React-FE->>Caching-Layer: Send Request to Get Tutor Info
  Caching-Layer-->>React-FE: Return Cached Data
```
