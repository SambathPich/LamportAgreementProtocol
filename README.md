# Lamport Agreement Protocol
Assume we have 7 networked machines with 1-2 faulty machines.Assume that any machines can communicate with any other directly

* The program should determine if either 1 or 2 of them are faulty. So, we will always have either 1 or 2 faulty machines. The faulty process provides a random value to all others.  The program should randomly select the 1 or 2 faulty threads prior to creation.  The program will randomly select one of the 7 threads to be the initiator and the initiator could possibly be faulty.

* Initiating Process
The initiating process will select a value of either 0 or 1 and will send it to all other machines. If it is faulty, then it should randomly select its value for each individual message sent to the others. If it is not faulty, then it should select a specific value (0 or 1) and send said value to others. 

* All processes have a socket connection between them. There will be 6 socket connections per process to ensure that direct, logical communication is correctly simulated. 

