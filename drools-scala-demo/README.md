# drools-scala-demo
This repository contains example code associated with the article ‘Event Processing with Drools and Scala: An Application to Corporate Wellness Program Management’
The unit tests correspond directly to examples outlined therein.

To run: 
```
sbt test
```

## From ‘Event Processing with Drools and Scala: An Application to Corporate Wellness Program Management’:
Corporate Wellness Programs, programs sponsored by employers that pay employees for activities like exercising, eating nutritiously, and engaging with their doctor, have demonstrable positive impacts on employee health. At Rally, we’ve developed a system to more efficiently configure and manage these programs, reducing administrative costs significantly. Importantly, in the case where imperfect employee data leads to dissemination of erroneous payments, our system provides a mechanism to “roll-back” the state of an employee’s program and recoup unspent money on behalf of the employer. This document describes the salient aspects of our system as they pertain to roll-back and the roll-back algorithm which guarantees that the state of employee’s program is consistent after a roll-back event. A complete working example is provided
