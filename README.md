[![Build Status](http://build.gravitee.io/jenkins/buildStatus/icon?job=gravitee-repository-couchbase)](http://build.gravitee.io/jenkins/view/Tous/job/gravitee-repository-couchbase/)

# Gravitee Couchbase Repository

Couchbase repository based on Couchbase

## Requirement

The minimum requirement is :
 * Maven3 
 * Jdk8

For user gravitee snapshot, You need the declare the following repository in you maven settings :

https://oss.sonatype.org/content/repositories/snapshots


## Building

```
$ git clone https://github.com/gravitee-io/gravitee-repository-couchbase.git
$ cd gravitee-repository-couchbase
$ mvn clean package
```

## Installing

Unzip the gravitee-repository-couchbase-1.0.0-SNAPSHOT.zip in the gravitee home directory.
 


## Configuration

repository.couchbase options : 

| Parameter                                        |   default  |
| ------------------------------------------------ | ---------: |
| hosts                                            |  			|
| bucketname                                       |  	gravitee|
| bucketpassword                                   |            |
| connectTimeout                                   |            |
| queryTimeout                               		|            |
| connectTimeout                                   |            |
| maxWaitTime                                      |            |
| socketTimeout                                    |            |
| socketKeepAlive                                  |            |
| maxConnectionLifeTime                            |            |
| maxConnectionIdleTime                            |            |
| minHeartbeatFrequency                            |            |
| description                                      |            |
| heartbeatConnectTimeout                          |            |
| heartbeatFrequency 	                           |            |
| heartbeatsocketTimeout                           |            |
| localThreshold 	                               |            |
| minConnectionsPerHost                            |            |
| sslEnabled 		                               |            |
| threadsAllowedToBlockForConnectionMultiplier     |            |
| cursorFinalizerEnabled                           |            |