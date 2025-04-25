# Spring Boot Weather Forecast Application
This is a Spring Boot example project that demonstrates various features and best practices in Spring Boot development,
while implementing an API service for retrieving weather forecasts for a specific location.

## Prerequisites

- Java 17.0.12
- Maven 3.9.9
- IntelliJ IDEA (recommended IDE)
- Git


## Project Setup
1. **Clone the repository:**

```bash
    git clone <https://github.com/parallel-minds-assignment/java-kshitija-apple-assignment>
  
   ```
    

2. **Open the project in IntelliJ IDEA:**
     - Launch IntelliJ IDEA
     - Select "Open" and choose the project directory
     - IntelliJ will automatically detect the Maven project and set up the environment
     - Wait for the project to sync and dependencies to be downloaded

### Using Maven

To run the application from the command line:

```bash
mvn spring-boot:run
```

# Running Tests
1. Using IntelliJ IDEA
- Open the test class you want to run.
- Click the green play button next to the test method or class.
- To run all tests, right-click the src/test directory and select "Run Tests".
2. Using Maven
- To run all tests:
    ```bash
    mvn test
    ```
3. To run tests with coverage report:
- mvn test jacoco/SonarQube:report

# Project Structure
- src\main\java\com\example\weather\ - Main application source code
- src\main\java\com\example\weather\config - Configuration files
- src\main\java\com\example\weather\controller - Handles Http requests and responses
- src\main\java\com\example\weather\customException - Contains custom exception classes to define your own error types
- src\main\java\com\example\weather\exceptionHandler - Contains classes that handle exceptions globally
- src\main\java\com\example\weather\model - Contains model classes that represent the data structure
- src\main\java\com\example\weather\service - Contains service classes that implement business logic related to weather forecasting

# Dependencies
-The project uses the following main dependencies:
1. spring-boot-starter 
2. spring-boot-starter-web 
3. spring-boot-starter-cache 
4. lombok 
5. spring-boot-starter-test 
6. resilience4j-spring-boot3 
7. spring-boot-starter-validation 
8. caffeine

# Features Implemented
1. Weather forecast API:
  - Accepts a zip code from the user. 
  - Provides the current temperature at the requested location as the primary output. 
  - Offers additional details, such as the highest and lowest temperatures, and an extended forecast.
2. Caching:
  - Implements caching to store forecast details for a duration of 15 minutes for subsequent requests using the same zip code. 
  - An indicator is displayed to notify users if the result is retrieved from the cache.
3. Geocoding Integration:
  - Latitude and longitude for a zip code are fetched using the Nominatim Geocoding API. 
  - Weather is retrieved for the latitude and longitude using the Open-Meteo API, which does not require an API key.
4. Access Weather API with JWT Authentication :
  - Run Spring Boot Application
  - Call the Login API to Get JWT Token(Username = "admin", password = "password123")
  - Copy the JWT Token.Call the Weather API with Bearer Token

# Development Tips
1. Code Formatting: Use IntelliJ's built-in code formatter (Ctrl+Alt+L / Cmd+Alt+L)
2. Code Generation: Use Lombok annotations to reduce boilerplate code. 
3. Caching: Weather data for a specific zip code is cached for 15 minutes to minimize requests to the external APIs

# Contributing
1. Create a new branch for your feature.
2. Make your changes.
3. Run tests to ensure everything works.
4. Submit a pull request.


