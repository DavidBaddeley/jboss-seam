package org.jboss.seam.example.ejbtimer.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@RunWith(Arquillian.class)
@RunAsClient
public class EJBTimerTest
{

   @Drone
   protected WebDriver driver;

   @ArquillianResource
   protected Deployer deployer;

   private static final String PATH = "http://localhost:8080/seam-ejbtimer/timer.seam";
   private final static String DEPLOYMENT = "EJBTimerTest";
   private final static String EXCEPTION = "ERROR [org.jboss.as.ejb3]";

   private By START = By.id("form1:start");
   private By CANCEL = By.id("form1:cancel");

   @Deployment(name = DEPLOYMENT, managed = false, testable = false)
   @OverProtocol("Servlet 3.0")
   public static Archive<?> createDeployment()
   {
      return Deployments.ejbTimerDeployment();
   }

   @Test
   public void testRedeployWithRunningTimer() throws InterruptedException
   {
      deployer.deploy(DEPLOYMENT);

      driver.navigate().to(PATH);
      // start timer
      driver.findElement(START).click();

      deployer.undeploy(DEPLOYMENT);
      Thread.sleep(5000l);
      deployer.deploy(DEPLOYMENT);

      driver.navigate().to(PATH);
      // stop timer
      driver.findElement(CANCEL).click();
      
      Assert.assertFalse("There is some EJB error! Please check server log.", isNullPointerExceptionThrown());

   }

   private boolean isNullPointerExceptionThrown()
   {
      boolean found = false;
      
      FileReader reader;
      try
      {
         reader = new FileReader(new File(System.getProperty("env.JBOSS_HOME").concat("/standalone/log/server.log")));
         BufferedReader bfRead = new BufferedReader(reader);
         String line;
         while ((line = bfRead.readLine()) != null)
         {
            if(line.contains(EXCEPTION)){
               found = true;
               break;
            }
         }
         bfRead.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return found;

   }

}
