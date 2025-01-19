## This is a simple recursive tree project with a more complex infrastructure
### The main goal of this application is to test my skills, and learn more about the technologies used in this project.

Pre-requisites:
 - podman || docker
 - Make
 - git (optional)

The application is compatible with both windows, and linux.

## Quick start
```bash
git clone https://github.com/kiralyzoltan98/rtree.git
```
```bash
cd rtree
```
```bash
make run
```
Thats it! 

After `make run` you can access the swagger-ui at http://localhost:8081/doc or http://localhost:8082/doc

In this setup, the Makefile starts up two instances of the application, and a database.

The project consists of a Spring application that has two core endpoints:
 - GET **/getunique**
   - expects an absolute path as a parameter, and returns a list of unique files in the given directory iterating through all subdirectories recursively.
   - accepts an optional extension parameter that returns only the unique files with the given extension.
   - after a successful request, the application saves the result to the history table.
 - GET **/history**
   - without any parameters, it returns the contents of the history table.
   - the results can be filtered by:
     - **username** (application instance identifier)
     - **createdAt** (timestamp)
     - **jsonData** (result of a `/getunique` request).
 - PUT **/generate**
   - by calling the generate endpoint, the application creates a directory structure and populates it with files, so it can be tested manually.
   - this endpoint is idempotent, so calling it multiple times will not create multiple directories, but reset the contents of the directory.
   - The path of the generated directory is `/tmp/rtree` so you should use this path as a parameter for the `/getunique` endpoint.

## Generated structure: (/generate)
#### WARNING: The generated path is only available on the instance that generated it.
``* means unique``
```text
/tmp/rtree
│   file1.txt   *
│   file2.json  *
│   file2.txt
├───a
│   │   file2.txt
│   │   file3.txt
│   │   file3.yaml
│   └───aa
│       │   file3.txt
│       │   file3.yaml
│       │   file4.txt
│       │   file4.yaml  *
│       └───aaa
│               file4.c     *
│               file4.txt
│               file5.txt
│               file55.txt  *
├───b
│       file5.txt
│       file6.c     *
│       file6.txt
└───c
        file6.txt
        file7.txt   *
```

The project also contains a Makefile, that handles the building, and running of the applications, and the database.

## Makefile
By default, make pulls the images from my docker hub repository, but you can also build the images locally by running:
```bash
make database_build
# or
make instance_build
```

## Database
The database is MariaDB. I use the latest official image from docker hub.
The database is initiated with a schema, and some environment variables are set on docker run. (timezone for example)
There is no additional user created, I just use the root user.

There is only one table in the database, the history table.
It has the following DDL:
```sql
create table history
(
    id         bigint auto_increment
        primary key,
    username   varchar(255) null,
    created_at datetime(6)  null,
    json_data  longtext     null
);
```

This is mapped by the History entity in the application.

## Github actions
I have found some good resources on running tests with gradle in GitHub actions, so I created a simple workflow that runs the tests on every push, or pull request to the master branch.

Actions could be extended to push the images to docker hub if the tests are passing.
For this the GitHub secrets should be used to store the docker hub credentials.

##  If the application does not work.
Try to examine the output of the make run command, and see if there are any errors.
Check if the containers started, issue a `podman ps` or `docker ps` command you should see three containers running named:
- Database
- Instance1
- Instance2

### If you cant pull the images
Podman should have docker.io registry enabled.
If it isn't, you can enable it by adding the following line to the /etc/containers/registries.conf file.
```bash
unqualified-search-registries = ["docker.io"]
```