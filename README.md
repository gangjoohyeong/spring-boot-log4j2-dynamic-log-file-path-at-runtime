# Dynamic log file path at runtime (Spring Boot + Log4j 2)

## Overview

1. Updates the log file path and log level dynamically at runtime via an API.
2. Uses Log4j 2 instead of the default Logback in Spring Boot.
3. If the logging configuration file does not exist in the specified path, it will generate a default log configuration file.

## API

1. **Update Log Configuration**  
   **POST** `/logging`

   **Request Example**
   ```json
   {
     "path": "log/project-name.log",
     "level": "INFO"
   }
   ```

   **Response Example**
   ```json
   {
     "path": "log/project-name.log",
     "level": "INFO"
   }
   ```

2. **Get Current Log Configuration**  
   **GET** `/logging`

   **Response Example**
   ```json
   {
     "path": "log/project-name.log",
     "level": "INFO"
   }
   ```

3. **Logging Test**  
   **GET** `/logging/test`

   **Log Output Example**
   ```bash
   2024-08-31 15:57:59.654 [http-nio-8080-exec-2] DEBUG gangjoo.logging.controller.LoggingController - DEBUG message
   2024-08-31 15:57:59.654 [http-nio-8080-exec-2] INFO  gangjoo.logging.controller.LoggingController - INFO message
   2024-08-31 15:57:59.654 [http-nio-8080-exec-2] WARN  gangjoo.logging.controller.LoggingController - WARN message
   2024-08-31 15:57:59.654 [http-nio-8080-exec-2] ERROR gangjoo.logging.controller.LoggingController - ERROR message
   ```