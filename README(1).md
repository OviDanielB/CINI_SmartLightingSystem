# HELIOS Monitoring And Control System

This repository contains a monitoring and control system implemented as an Apache Storm application. This application is called CINI_SmartLightingSystem, because it implements a project for the **CINI Smart City University Challenge** ([LINK](https://it.eventbu.com/l-aquila/cini-smart-city-university-challenge/2263724)).

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
The following software must be present to install the System and the related links shows how to install them:
* [Maven](https://maven.apache.org/) - Dependency Management to build the system
* [Docker](https://www.docker.com/) - Software Container Platform to build, ship, and run distributed applications, whether on laptops, data center VMs, or the cloud
* [Docker Compose](https://docs.docker.com/compose/) - Tool to define and run multi-container Docker applications

(for Cloud Deployment)
* Amazon Web Services ([AWS]()) registered account

### Configuration 
The repository contains a default configuration file *resources/config.yml* where are grouped all the following System configurable parameters:
* *memcached*: related to caching server:
	* *hostname*
	* *port*
* *parkingServer* related to parking server to simulate REST API:
	* *hostname*
	* *port*
* *statisticsTopologyParams* related to statistics computation:
	* *tickTupleFrequency* computing values update rate
	* *hourlyStatistics* related to hourly statistics computation:
			- *windowLenght* time interval to compute statistics (1 hour)
			- *emitFrequency* results emission rate
	* *dailyFrequency* related to weekly statistics computation:
		    - *windowLenght* time interval to compute statistics (24 hours)
		    - *emitFrequency* results emission rate
* *queue_in* input queue(s):
    * *queue-name*
	* *hostname*
	* *port*
* *queue_out* output queue(s):
	* *queue-name*
	* *hostname*
	* *port*
* *rankingTopologyParams* related to ranking computation:
	* *rank_size* size of ranking
	* *lifetime_minimum* threshold to include in ranking
* *controlThresholds* related to control intensity level:
	* *traffic_tolerance* threshold above which traffic percentage is considered significant 
	* *parking_tolerance* threshold above which parking occupation percentage is considered significant
		
An example of configuration is
```
queue_in: 
    queue_name: 'storm'
    hostname: 'localhost'
    port: 5672
```
### Installing

##### 'pom.xml'
Application can run in Local Mode or Cluster Mode to be specified in file *pom.xml* where *scope* of Storm is *compile* for Local Mode or *provided* for Cluster one.
An example of *pom.xml* configured for Cluster Mode execution:
```
    [...]
    <dependency>
        <groupId>org.apache.storm</groupId>
        <artifactId>storm-core</artifactId>
        <version>1.0.3</version>
        <!--> compile for LOCAL MODE, provided for real CLUSTER MODE </!-->
        <scope>provided</scope>
    </dependency>
    [...]
```
### Compile
To compile code with all dependencies and create the executable Java file *.jar*, first change the current directory to the main directory of the project:  
```
~$ cd {$HELIOS_HOME} 
```
then execute:
```
{$HELIOS_HOME}$ mvn clean validate compile test package org.apache.maven.plugins:maven-assembly-plugin:2.2-beta-5:assembly 
```
Now *.jar* file is created in *{$HELIOS_HOME}/target*.

##### Local Mode
To run locally a topology, change the current directory in *{$HELIOS_HOME}/target* and execute

```
{$HELIOS_HOME}/target$ java -jar CINI_SmartLightingSystem-1.0-jar-with-dependencies.jar org.uniroma2.sdcc.<topologyName>
```
where *<topologyName>* can be:
* *AnomaliesDetectionTopology*, to monitor and control luminous intensity of the lamps network respect to weather, daytime, street averaged intensity, eventual malfunctioning 
* *RankingOldestLampsTopology*, to notice the count of lamps with a bulb replacement's date faraway more than a defined threshold of days from now 
* *ConsumptionStatisticsTopology*, to compute single and aggregated consumption statistics measured in the last hour, last day and last week

##### Cluster Mode
To submit a Storm topology, change current directory in 
```
~$ cd {$HELIOS_HOME}/Scripts
```
and modify the following line in *{$HELIOS_HOME}/Scripts/start_storm_on_docker* specifying the *<topologyName>*
```
[...]
docker run --link some-nimbus:nimbus -it --rm -v $(pwd)/CINI_SmartLightingSystem-1.0-jar-with-dependencies.jar:/CINI_SmartLightingSystem-1.0-jar-with-dependencies.jar storm:1.0.3 storm jar /CINI_SmartLightingSystem-1.0-jar-with-dependencies.jar org.uniroma2.sdcc.<topologyName>
[...]
```
Start up the Docker cluster of containers and submit the topology 
```
{$HELIOS_HOME}/Scripts$ ./start_storm_on_docker
```

Storm UI to monitor the running topology is available at http://localhost:8000/.

To stop topology and destroy the Docker cluster of containers
```
{$HELIOS_HOME}/Scripts$ ./stop_storm_on_docker
```

## Running the tests

Explain how to run the automated tests for this system

### Unit tests
Unit tests test correct single component operations execution. 
```
$ java GlobalRankBoltTest.java 
```

### Topology tests
Topology tests test correct global topology execution.
```
$ java TopologyTest.java <topologyName> test<number>
```

## Deployment
For Cloud Deployment of HELIOS System are used **Amazon Web Services Cloud Formation** and **Elastic Compute Cloud** (EC2) instances.
1) Sign in AWS account
2) Create a **Docker stack** following the Docker template to deploy the cluster in **Swarm Mode** with AWS Cloud Formation, available at https://stelligent.com/2017/02/21/docker-swarm-mode-on-aws/
    a) Choose the stack name
    b) Select number of *manager* nodes
    c) Select number of *worker* nodes
    d) Choose a SSH key to use, selecting an existing EC2 Key Pair to enable SSH connections to instances (create it in AWS EC2 console if no one is available)
    e) Acknowledge that AWS CloudFormation might create IAM resources
    f) Confirm to create stack
3) Enter the AWS EC2 console to visualize all instances started by the stack (this are running in some minutes)
4) SSH into any one of your Manager nodes with the user *docker* and the EC2 Keypair you specified when you launched the stack
 a) Select any Manager instance
 b) Click *Connect* button and copy the command to connect via SSH to the instance
5) Download raw file *swarm-compose.yml* from the GitHub repository to start up containers distributed among the stack EC2 instances.
An example:
```
~$ curl -O https://raw.githubusercontent.com/OviDanielB/cini-aws-help/master/swarm-compose.yml
```
6) Download raw file *<topologyName>.jar* from the GitHub repository to make it available for Storm Submitter container
7) Make a new directory *ParkingREST_API* and download via *curl* raw files from folder *ParkingREST_API* from the GitHub repository to make it available for NodeJs server container
8) Deploy Docker stack in Swarm mode
```
~$ deploy stack deploy -c swarm-compose.yml <stackName> 
```
In AWS EC2 console, in tab *Load Balancers* is available instance of stack Load Balancer, which public DNS name allows to communicate with the deployed System.

Docker Swarm Visualizer is available at http://{LOAD_BALANCER_DNS}:8080/.

Storm UI is available at http://{LOAD_BALANCER_DNS}:8000/.

To enable RabbitMQ UI:
1) SSH into instance where RabbitMQ is running on
2) Find container ID where RabbitMQ is running on
```
~$ docker container list 
```
3) Execute
```
~$ docker exec -it <ContainerIDRabbit> /bin/bash
```
4) Execute
```
bash$ rabbitmq-plugin enable rabbitmq_management
```

RabbitMQ UI is available at http://{LOAD_BALANCER_DNS}:15672/.
## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [Git](https://git-scm.com/) for versioning.

## Authors

* **Ovidiu Daniel Barba** - [OviDanielB](https://github.com/OviDanielB)
* **Laura Trivelloni** - [lauratrive](https://github.com/lauratrive)
* **Emanuele Vannacci** - [Zanna-94](https://github.com/Zanna-94)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone who's code was used
* Inspiration
* etc