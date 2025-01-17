## The purpose of this file is to provide a notebook for the commands I used.

 ```bash 
 podman machine start
 # comment
 podman machine stop
 
 podman run --name Database -e MYSQL_ROOT_PASSWORD=password -p 3306:3306 -d kiralyzoltan98/mariadb-schema:latest
 ```
