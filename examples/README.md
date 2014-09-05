Seam Example EE6 Applications
=============================
This directory contains the Seam example applications, which have all been
tested on the latest release of JBoss AS 7.1.1. Consult the `README.md` file in each of 
the examples to see details.

The name of each example, refered to later as `${example.name}`, is equivalent to the name of the folder.

----------------------------------------------------------------------

## Deploying and Testing an Example Application

These are general instructions for deploying Seam examples. Take a look at the 
`README.md` file in the example to see if there are any specific instructions.

### How to Build and Deploy an Example on JBoss AS

1. Download and unzip JBoss AS 7.1.1 from:
   
        http://jboss.org/jbossas/downloads

2. Make sure you have an up to date version of Seam: 

        http://seamframework.org/Download

3. Build the example by running the following command from the Seam `examples/${example.name}` directory:
   
        mvn clean install

   _NOTE: There is an option to create an "exploded" archive. For this purpose, use `-Pexploded` maven profile._

4. Deploy the example by setting the `JBOSS_HOME` property and running the 
   following command from the Seam "examples/${example.name}/{example.name}-ear" directory:

        mvn jboss-as:deploy
    
   To undeploy the example, run:

        mvn jboss-as:undeploy

5. Point your web browser to:

        http://localhost:8080/seam-${example.name}

   The context path is set to the final name of the EAR archive.

   However, WAR deployments use a different naming convention for the context
   path. If you deploy a WAR example, point your web browser to:

        http://localhost:8080/${example.name}-web

   The WAR examples are:
   spring, jpa, hibernate, groovybooking.

_NOTE: The examples use the H2 database embedded in JBoss AS_

   
### Running The Integration Tests

Integration tests can be executed during a build of the application using:

    mvn clean install -Darquillian=jbossas-{managed,remote}-7


### Running integration test(s) in Eclipse

Detailed guide is at `http://docs.jboss.org/arquillian/reference/1.0.0.Alpha1/en-US/html_single/#d0e552`


### Debugging of integration test(s) in Eclipse

`http://docs.jboss.org/arquillian/reference/1.0.0.Alpha1/en-US/html_single/#d0e974`


## Running functional tests on an example

The following steps describe executing of functional tests in general. Some examples are not covered with functional tests and thus don't contain the `${example.name}-ftest` folder.

* Set `JBOSS_HOME` environment property to path where is your JBoss AS 7 server

To run a functional test:

    mvn clean test -f ${example.name}/${example.name}-ftest/pom.xml

To run all functional tests:

    mvn clean test -Dftest
