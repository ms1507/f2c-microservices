# Config Service and Discovery Service 
In Microservices, managing configuration and communication manually is a nightmare. Here is exactly why we use these two services:

## 1. Spring Cloud Config Server (The "Brain")
### Why externalize configuration? 
In a traditional app, application.yml is packaged inside the JAR file. If you want to change the database password or a feature flag, you have to:
1) Change code.
2) Re-build the JAR.
3) Re-deploy the application.

#### With Config Server:
1) **Separation of Concerns:** Your code (users-service.jar) contains logic. Your configuration (user-service.yml in config-repo) contains settings.
2) **Runtime Updates:** You can change config-repo/user-service.yml and hit a refresh endpoint, and the service updates without restarting/rebuilding.
3) **Central Management:** You see ALL your environments (Dev, QA, Prod) in one place.

#### Why config-repo?
We created this folder to act as our Configuration Source.

##### Ideally (Production): 
This is a Git Repository (e.g., GitHub). The Config Server connects to Git. Every time you git push a config change, it's live.
##### Currently (Dev):
We are using the Native profile (spring.profiles.active=native). This tells Config Server: "Don't look at GitHub, just look at this local folder config-repo on my disk." It's faster for development.
# 2. Service Discovery - Eureka (The "Phonebook")
#### The Problem: 
Imagine Order Service needs to call User Service.
##### Code:
    RestTemplate.get("http://localhost:8081/users/1")
##### Problem:
In the cloud (AWS/Azure/Kubernetes), services restart constantly. The IP address localhost or 192.168.x.x changes every time a container restarts. You cannot hardcode URLs.
##### The Solution:
**1) Registration:** When User Service starts, it calls Eureka: "Hi, I am 'USER-SERVICE' and I am available at 192.168.1.5:8081".

**2) Discovery:** When Order Service needs a user, it asks Eureka: "Where is USER-SERVICE?"

**3) Resolution:** Eureka replies: "It's at 192.168.1.5:8081".

* **Why we need it now:**  Even though you are on localhost now, setting this up means your services communicate by Name (http://user-service/api/...), not by Port. This makes them cloud-ready immediately.

*  **Verify Your Setup:**
If you have started the services as requested, you can verify this flow:

* **1) Check Config:** Go to http://localhost:8888/user-service/default
You should see the JSON content of the file we put in config-repo.

* **2) Check Discovery:** Go to http://localhost:8761
You should see user-service listed there.

**-------------------------------------------------------------------------------------------------------------------------------------------------**

**-------------------------------------------------------------------------------------------------------------------------------------------------** 

**1) First we have created user-service** - including CRUD for create Role, Permissions, Users etc.

**2) We have implemented Spring security for Authentication of requests using JWT, JWTAuthFilter. etc.**

**3) We have to create infrastructure for configuration and discovery of services and interservices communication.**

**next ->**

**Phase 1.1 (Config Service) & 1.2 (Service Discovery) are Ready!**

I have:

**Centralized Configuration:>** Moved user-service and catalog-service configs to the config-repo folder.
**Updated Services:** Configured both services to fetch settings from the Config Server.
**Added Dependencies:** Enabled config-client and eureka-client in both services.

**Action Required:** Start Infrastructure
    Please start the services in this exact order (in separate terminals):
    
**Order:**

**1) Config Service (Port 8888)**
        cd config-service
        mvn spring-boot:run

**2) Discovery Service (Port 8761)**
        cd discovery-service
        mvn spring-boot:run

**3) User Service (Port 8081)**
        cd user-service
        mvn spring-boot:run
    (Check logs: It should say "Fetching config from server..." and "Registered with Eureka")

**4) Catalog Service (Port 8082)**
        cd catalog-service
        mvn spring-boot:run

*  **Once all are running, check:**

   **Eureka Dashboard:** http://localhost:8761 (Should show USER-SERVICE and CATALOG-SERVICE)
   
   **Config Test:** http://localhost:8888/user-service/default (Should show JSON config)
