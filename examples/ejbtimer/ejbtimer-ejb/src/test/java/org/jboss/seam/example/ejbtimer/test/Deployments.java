package org.jboss.seam.example.ejbtimer.test;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public class Deployments
{

   public static WebArchive ejbTimerDeployment()
   {
	   File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
               .importCompileAndRuntimeDependencies()
               // force resolve jboss-seam, because it is provided-scoped in the pom, but we need it bundled in the WAR
               .resolve("org.jboss.seam:jboss-seam")
               .withTransitivity().asFile();
      
      
      return ShrinkWrap.create(WebArchive.class, "seam-ejbtimer.war")
    		.addPackages(true, "org.jboss.seam.example.ejbtimer")

    		// already in EJB module
            .addAsResource("import.sql")
            .addAsResource("seam.properties")
            .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")

            .addAsWebInfResource("META-INF/ejb-jar.xml", "ejb-jar.xml")

            // manually copied from Web module
            .addAsWebInfResource("pages.xml")

            // manually copied from Web module, modified
            .addAsWebInfResource("web.xml") // only contains MockSeamListener definition
            .addAsWebInfResource("components.xml") // corrected ejb component jndi-name references from java:app/jboss-seam to java:app/seam-seampay

            // manually copied from EAR module
            .addAsWebInfResource("jboss-deployment-structure.xml")
            .addAsLibraries(libs);
   }

}
