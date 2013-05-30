package org.jboss.seam.example.ejbtimer;

import javax.ejb.Timer;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.log.Log;

@Name("paymentHome")
public class PaymentHome extends EntityHome<Payment>
{
   @RequestParameter
   Long paymentId;
   @In
   PaymentProcessor processor;

   @Logger
   Log log;

   public String saveAndSchedule()
   {
      String result = persist();

      Payment payment = getInstance();

      log.info("scheduling instance #0", payment);
      Timer timer = processor.schedulePayment(payment.getPaymentDate(), payment.getPaymentFrequency().getInterval(), payment);

      payment.setTimer(timer.getHandle());

      return result;
   }

   @Override
   public Object getId()
   {
      return paymentId;
   }

   @Transactional
   public void cancel()
   {
      Payment payment = getInstance();

      try
      {
         Timer timer = payment.getTimerHandle().getTimer();
         payment.setTimer(null);
         payment.setActive(false);
         timer.cancel();
      }
      catch (Exception nsole)
      {
         FacesMessages.instance().add("Payment already processed");
      }
   }

}
