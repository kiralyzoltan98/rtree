## The purpose of this file is to provide a notebook for the commands I used.

 ```bash 
 podman machine start

 podman machine stop
 
 podman run --name Database -e MYSQL_ROOT_PASSWORD=password -p 3306:3306 -d kiralyzoltan98/mariadb-schema:latest
 # ssh into the podman machine
 podman machine ssh --username root
 # building a new database image
 podman build -f Dockerfile -t kiralyzoltan98/mariadb-schema:latest
 # pushing the database image to the docker hub
 docker push kiralyzoltan98/mariadb-schema:tagname
 
 
 
 ```


### in a real projects these should be done correctly. 

- Handling keys and passwords.
- Committing the application.properties file.
- 
