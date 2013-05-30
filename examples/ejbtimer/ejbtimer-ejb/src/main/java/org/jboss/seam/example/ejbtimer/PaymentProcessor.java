package org.jboss.seam.example.ejbtimer;

import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("processor")
@AutoCreate
@Stateless
public class PaymentProcessor
{

   @In
   EntityManager entityManager;

   @Resource
   TimerService timerservice;

   @Logger
   Log log;

   public Timer schedulePayment(Date when, Long interval, Payment payment)
   {
      Timer timer = null;

      if (payment.getPaymentFrequency().equals(Payment.Frequency.ONCE))
      {

         timer = timerservice.createSingleActionTimer(when, new TimerConfig(payment, true));

      }
      else
      {

         timer = timerservice.createIntervalTimer(when, interval, new TimerConfig(payment, true));

      }
      return timer;
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

}
