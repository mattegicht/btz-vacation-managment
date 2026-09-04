# Deployment Guide for Vacation Management System

This guide outlines the steps to deploy the Spring Boot application and its MySQL database.

## Prerequisites

*   **Java 17 JDK** (if running locally without Docker)
*   **Docker & Docker Compose** (recommended)
*   **Maven** (wrapper script `./mvnw` is included in the project)

---

## Option 1: Docker Compose (Recommended)

This method packages the application and the database into containers, ensuring a consistent environment.

### 1. Create a `Dockerfile`

Create a file named `Dockerfile` in the root of your project (`demo/Dockerfile`) with the following content:

```dockerfile
# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Convert line endings for mvnw (optional, useful if building from Windows)
RUN dos2unix mvnw

# Download dependencies (to improve build caching)
RUN ./mvnw dependency:go-offline

# Copy the project source
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Copy the jar file to a specific name
RUN cp target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","app.jar"]
```

### 2. Update `compose.yaml`

Modify your existing `compose.yaml` to include the application service and link it to the database.

```yaml
services:
  app:
    build: .
    ports:
      - '8080:8080'
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/mydatabase
      - SPRING_DATASOURCE_USERNAME=myuser
      - SPRING_DATASOURCE_PASSWORD=secret
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
    depends_on:
      - mysql

  mysql:
    image: 'mysql:latest'
    environment:
      - 'MYSQL_DATABASE=mydatabase'
      - 'MYSQL_PASSWORD=secret'
      - 'MYSQL_ROOT_PASSWORD=verysecret'
      - 'MYSQL_USER=myuser'
    ports:
      - '3306:3306'
    volumes:
      - db_data:/var/lib/mysql

volumes:
  db_data:
```

### 3. Build and Run

Run the following command in the terminal to build the image and start the services:

```bash
docker-compose up --build -d
```

Your application will be accessible at `http://localhost:8080` (or your server's IP).

---

## Option 2: Traditional Deployment (VPS/Server)

This method involves running the JAR file directly on a server (e.g., EC2, DigitalOcean Droplet) that has Java and MySQL installed.

### 1. Build the JAR File

On your local machine, run:

```bash
./mvnw clean package -DskipTests
```

This will create a `.jar` file in the `target/` directory (e.g., `demo-0.0.1-SNAPSHOT.jar`).

### 2. Prepare the Server

*   **Install Java 17:** Ensure the target server has Java 17 installed (`java -version`).
*   **Install MySQL:** Install and start a MySQL server. Create a database (e.g., `mydatabase`) and a user.

### 3. Transfer the JAR

Use `scp` or `sftp` to upload the JAR file to your server.

```bash
scp target/demo-0.0.1-SNAPSHOT.jar user@your-server-ip:/path/to/app/
```

### 4. Run the Application

Run the application, overriding the database configuration to point to your live database.

```bash
java -jar demo-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:mysql://localhost:3306/mydatabase \
  --spring.datasource.username=your_db_user \
  --spring.datasource.password=your_db_password
```

**Tip:** For production, use a service manager like `systemd` to keep the application running in the background and restart it on boot.

---

## Option 3: Cloud PaaS (e.g., Railway, Render)

Many modern cloud platforms detect the `pom.xml` and `Dockerfile` automatically.

1.  Push your code to a GitHub repository.
2.  Connect your repository to the PaaS provider.
3.  Add a MySQL database service within the platform.
4.  Configure the environment variables (`SPRING_DATASOURCE_URL`, `USER`, `PASSWORD`) in the platform's dashboard to connect the app to the database.
5.  Deploy.

