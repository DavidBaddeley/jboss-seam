package com.jboss.dvd.seam.test;

import com.jboss.dvd.seam.Order;

import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;

public class Deployments {

   @OverProtocol("Servlet 3.0")
   public static WebArchive dvdStoreDeployment() {

      File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
              .importCompileAndRuntimeDependencies().resolve().withTransitivity().asFile();

      return ShrinkWrap.create(WebArchive.class, "seam-dvdstore.war")
              .addPackage(Order.class.getPackage())
              .addAsWebInfResource("META-INF/ejb-jar.xml", "ejb-jar.xml")
              .addAsWebInfResource("import.sql", "classes/import.sql")
              .addAsWebInfResource("persistence.xml", "classes/META-INF/persistence.xml")
              .addAsWebInfResource("components-test.xml", "components.xml")
              .addAsWebInfResource("jboss-deployment-structure.xml", "jboss-deployment-structure.xml")
              .addAsWebInfResource("seam.properties", "classes/seam.properties")
              .addAsWebInfResource("web.xml", "web.xml")
              .addAsWebInfResource("pages.xml", "pages.xml")

              .addAsWebInfResource("hibernate.cfg.xml", "classes/hibernate.cfg.xml")
              .addAsWebInfResource("jbpm.cfg.xml", "classes/jbpm.cfg.xml")

              .addAsWebInfResource("ordermanagement1.jpdl.xml", "classes/ordermanagement1.jpdl.xml")
              .addAsWebInfResource("checkout.jpdl.xml", "classes/checkout.jpdl.xml")
              

              .addAsWebInfResource("jboss-seam-dvdstore-ds.xml", "jboss-seam-dvdstore-ds.xml")

              .addAsLibraries(libs);
   }
}
