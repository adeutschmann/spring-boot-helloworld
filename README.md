# Spring Boot HelloWorld

A simple Spring Boot application demonstrating the use of RouterFunction and handler pattern for creating REST endpoints.

## Features

- Spring Boot 3.5.5
- Java 21
- Functional web framework with RouterFunction
- Clean separation of concerns with handler and router classes
- JSON response endpoints

## Endpoint

- **GET** `/hello` - Returns a JSON greeting message

**Response:**
```json
{
  "message": "Hello World"
}
```

## Project Structure

```
src/main/java/ch/adeutschmanndev/helloworld/
├── HelloworldApplication.java          # Main Spring Boot application
├── resource/
│   └── HelloWorldHandler.java          # Request handler containing business logic
└── router/
    └── HelloWorldRouter.java           # RouterFunction configuration
```

## Running the Application

### Prerequisites
- Java 21
- Maven 3.6+

### Steps
1. Clone the repository
```bash
git clone https://github.com/adeutschmann/spring-boot-helloworld.git
cd spring-boot-helloworld
```

2. Run the application
```bash
./mvnw spring-boot:run
```

3. Test the endpoint
```bash
curl http://localhost:8080/hello
```

## Architecture

This application uses Spring's functional web framework approach:

- **HelloWorldHandler**: Contains the business logic for handling requests
- **HelloWorldRouter**: Defines the routing configuration using RouterFunction
- **Functional Style**: Leverages Spring's functional programming model for web layer

## Development

The application runs on port 8080 by default and includes comprehensive logging for debugging purposes.
