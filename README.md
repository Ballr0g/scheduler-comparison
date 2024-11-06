# scheduler-comparison
A repository is created to compare the developer experience of a Spring Boot App using one of the following schedulers:
- Quartz
- JobRunr
- db-scheduler

# Demo application legend

We're working on a marketplace order processing adapter application that keeps forwarding various order statuses
to a user notification platform via a Kafka topic. Each order requires custom handling logic determined by its status
(although we don't have any specific details because the system analyst working on the task is on a 60-days vacation).
Furthermore, some large merchants featured on our marketplace require dedicated handling due to the large amounts
of traffic (going all the way up to 100rps/merchant) as well as a strict contract that will require customization in
the next quarter for such merchants.

**General flow**: Our implementation is going to load the orders from a relational database (PostgreSQL to be specific,
because our notorious marketplace master system is a spooky legacy monolith working with one) grouped by
operation type and merchant id every minute and send the results to a Kafka topic
(later consumed by the notification platform).

We also need to send the Loca-Lola cancellations in the FAILED status to yet another topic in case they have
the ELIGIBLE_FOR_REFUND flag set to true in the order_refunds table.

**WildFruit flow** requires a customization in comparison to general flow: it should set the operations in the CANCELLED status to
the ERROR status because we aren't supposed to handle cancellations but our spooky monolith keeps putting them in the
table sometimes, so we have no choice other than to patch it in our system. Such operation must not be sent to the
Notification Platform.

- There are 5 merchants:
  - Tiny Mistress, a luxury tier clothing manufacturer primarily targeting ladies of all ages
  - SungSam, a smartphone manufacturer
  - Sonic Boom, a mass-market speaker reseller
  - Loca-Lola, a worldwide famous soft fuzzy drinks manufacturer, **one of the large merchants**
  - WildFruit: a partner marketplace which reuses our infrastructure for its operations, **the second large merchant**
- There are multiple order statuses:
  - PAID: the order has been successfully paid by the client
  - DELIVERED: the order has been successfully delivered by a specially trained buff pigeon
  - FAILED: the specially trained buff pigeon couldn't have delivered the order 
    (and probably got eaten by an unimaginable force)
  - CANCELLED: the client has randomly decided they no longer need the order and cancelled it. The buff delivery pigeon
    is lucky enough to decide what to do with the order to their own likings.

# Implementation notices
- For simplicity, neither monitoring nor appropriate error handling will be present.
- The jobs are created on application startup from a Spring configuration since the goal is to test dynamic job setup.
- The data model definition can be found in Liquibase files for each submodule.
- There is intentional data model/config duplication to represent the effort it takes to set up each scheduler app.