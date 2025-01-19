This is a simple recursive tree project with a more complex infrastructure
The main goal of this application is to test my skills, and learn more about the technologies used in this project.

Pre-requisites:
 - podman || docker
 - Make
 - git (optional)

The application consists of a Spring application that has two core endpoints:
after starting the application you can access the swagger-ui at http://localhost:8081/doc or http://localhost:8082/doc

 - GET **/getunique**
   - expects an absolute path as a parameter, and returns a list of unique files in the given directory iterating through all subdirectories recursively.
   - accepts an optional extension parameter that returns only the unique files with the given extension.
   - after a successful request, the application saves the result to the history table.
 - GET **/history**
   - without any parameters, it returns the contents of the history table.
   - the results can be filtered by:
     - **username** (application instance identifier)
     - **createdAt** (timestamp)
     - **jsonData** (result of a /getunique request).