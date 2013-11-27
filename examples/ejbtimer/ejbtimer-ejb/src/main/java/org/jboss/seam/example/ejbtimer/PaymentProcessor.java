package org.jboss.seam.example.ejbtimer;

import java.math.BigDecimal;
import java.util.Date;

import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.persistence.EntityManager;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.log.Log;

@Name("processor")
@AutoCreate
@Stateless
public class PaymentProcessor
{

   @In
   EntityManager entityManager;

   @Logger
   Log log;

   @Asynchronous
   public Timer schedulePayment(@Expiration Date when, 
                                @IntervalDuration Long interval, 
                                Payment payment) 
   { 
       payment = entityManager.merge(payment);

       log.info("[#0] Processing payment #1", System.currentTimeMillis(), payment.getId());
       processPayment(payment);

       return null;
   }

   @Timeout
   public void timeout(Timer timer)
   {

      Payment payment = (Payment) timer.getInfo();
      payment = entityManager.merge(payment);

      log.info("[#0] Processing payment #1", System.currentTimeMillis(), payment.getId());

      if (payment.getPaymentEndDate() != null && payment.getPaymentEndDate().getTime() < System.currentTimeMillis())
      {
         payment.setActive(false);
         payment.setTimer(null);
         timer.cancel();
      }

      if (payment.getActive())
      {
         BigDecimal balance = payment.getAccount().adjustBalance(payment.getAmount().negate());
         log.info(":: balance is now #0", balance);
         payment.setLastPaid(new Date());

         if (payment.getPaymentFrequency().equals(Payment.Frequency.ONCE))
         {
            payment.setActive(false);
         }
      }
   }
   
   public void processPayment(Payment payment) {
       if (payment.getActive()) {
           payment.getAccount().adjustBalance(payment.getAmount().negate());
           
           payment.setLastPaid(new Date());
           
           if (payment.getPaymentFrequency().equals(Payment.Frequency.ONCE)) {
               payment.setActive(false);
           }
       }
   }   

}
