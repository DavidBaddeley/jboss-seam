package org.jboss.seam.example.ejbtimer.test;

import java.math.BigDecimal;
import java.util.Date;

import javax.ejb.Timer;
import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.example.ejbtimer.Account;
import org.jboss.seam.example.ejbtimer.Payment;

@Name("paymentActions")
public class PaymentActions
{

   @In
   TestPaymentProcessor testProcessor;

   @In
   EntityManager entityManager;

   public void schedulePayment()
   {

      Payment payment = createPayment();
      Timer timer = testProcessor.schedulePayment(payment.getPaymentDate(), 5000l, payment);
      payment.setTimer(timer.getHandle());
   }


   public Payment createPayment()
   {

     Payment payment = new Payment();
      Account account = (Account) entityManager.createQuery("select a from Account a where a.id=1").getSingleResult();
      payment.setAccount(account);
      payment.setPayee("nobody");
      payment.setPaymentDate(new Date(System.currentTimeMillis()));
      payment.setCreatedDate(new Date(System.currentTimeMillis()));
      payment.setAmount(BigDecimal.valueOf(5l));
      return payment;

   }
}
