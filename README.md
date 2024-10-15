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

Our implementation is going to load the orders from a relational database (PostgreSQL to be specific,
because our notorious marketplace master system is a spooky legacy monolith working with one) grouped by
operation type and merchant id every minute, filter out the operations in the FAILED status in case they have
the REFUND flag set to true and send the results to a Kafka topic (later consumed by the notification platform).
- For simplicity, no monitoring or error handling will be present
- All the jobs are created on application startup from a Spring configuration
- There are 5 merchants:
  - Tiny Mistress, a luxury tier clothing manufacturer primarily targeting ladies of all ages
  - SungSam, a smartphone manufacturer
  - Sonic Boom, a mass-market speaker reseller
  - Loca-Lola, a worldwide famous soft fuzzy drinks manufacturer, **one of the large merchants**
  - WildFruits: a partner marketplace which reuses our infrastructure for its operations, **the second large merchant**
- There are multiple order statuses:
  - PAID: the order has been successfully paid by the client
  - DELIVERED: the order has been successfully delivered by a specially trained buff pigeon
  - FAILED: the specially trained buff pigeon couldn't have delivered the order 
    (and probably got eaten by an unimaginable force)
  - CANCELLED: the client has randomly decided they no longer need the order and cancelled it. The buff delivery pigeon
    is lucky enough to decide what to do with the order to their own likings.