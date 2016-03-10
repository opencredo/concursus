Running Kafka in Docker
-----------------------

- install Docker, Docker Machine and Docker Compose
- make sure a docker machine is up and running or [create a new one](https://docs.docker.com/machine/get-started/):

  ```shell
    $ docker-machine ls
    $ docker-machine create --driver virtualbox default
    $ eval $(docker-machine env default)
  ```
  
- check what ip the docker machine is working on. if it's not `192.168.99.100` you will need to adapt the `KAFKA_ADVERTISED_HOST_NAME` in the docker compose file, the Spring Boot configuration file and some of the commands in this file.
  
- set up the kafka-docker submodule:

  ```shell
    $ git submodule init
    $ git update
  ```
  
- start Kafka and Zookeeper with `docker-compose`:

  ```shell
    $ docker-compose -f docker-compose-concourse.yml up
  ```

- Start the application with the `kafka` profile:

  ```shell
    $ mvn spring-boot:run -Dspring.profiles.active=redis,kafka
  ```
  
- Download Kafka (currenlty 0.9.0.1) from http://kafka.apache.org/downloads.html. This needed to use the consle producer (and should be replaced by a docker container).
- Start the Kafka console producer in `kafka_2.11-0.9.0.1/bin`:

  ```shell
      $ ./kafka-console-producer.sh --topic=user --broker-list=192.168.99.100:9092
  ```

- Type (or copy) `{"name": "Amy", "password": "pythagorus"}` into the console and you should be able to observe the creation of a new user.  


