package com.sambath;

import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.lang.Thread.sleep;

public class Main {

    public static void main(String[] args) {
        int NUM_PROCESSORS = 7;
        int NUM_FAULTY = new Random().nextInt(2) + 1;                   //Assign Number of Faulty Randomly
        int INITIATOR = new Random().nextInt(NUM_PROCESSORS-1) + 0;
        int FAULTY1 = new Random().nextInt(NUM_PROCESSORS-1) + 0;
        int FAULTY2 = -1;   // -1 because i use OR in comparision, so if there is only 1 faulty, there will be no error

        if (NUM_FAULTY == 1) {
            while (FAULTY1 == INITIATOR) {
                FAULTY1 = new Random().nextInt(NUM_PROCESSORS-1) + 0;
            }
        } else {
            while (FAULTY1 == INITIATOR) {
                FAULTY1 = new Random().nextInt(NUM_PROCESSORS-1) + 0;
            }
            FAULTY2 = new Random().nextInt(NUM_PROCESSORS-1) + 0;
            while ( (FAULTY2 == FAULTY1) || (FAULTY2 == INITIATOR) ) {
                FAULTY2 = new Random().nextInt(NUM_PROCESSORS-1) + 0;
            }
        }

        Mission mission = new Mission(NUM_PROCESSORS, NUM_FAULTY, INITIATOR, FAULTY1, FAULTY2);
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_PROCESSORS);
        System.out.println("TOTAL PROCESSES: " + NUM_PROCESSORS + ", TOTAL FAULTY: " + NUM_FAULTY);
        System.out.println("=> INITIATOR: PROCESS " + INITIATOR);

        if (NUM_FAULTY == 1) {
            System.out.println("=> FAULTY [Process: " + FAULTY1 + "]\n");
        } else {
            System.out.println("=> FAULTY [Process: " + FAULTY1 + " and " + FAULTY2 + "]\n");
        }

        System.out.println("===================== ROUND 0 =====================");
        for (int i = 1; i <= NUM_PROCESSORS; i++) {
            Processor processor = new Processor(mission, INITIATOR);// Create Processes
            executorService.execute(processor);
        }
        executorService.shutdown();
    }
}

class Mission {

    byte default_message = 0;
    public Vector<Processor> processors;
    int num_Processor, num_Faulty, initiator, faulty1, faulty2;
    int id = 0;
    int started = 0;
    MessageStorage messageStorage = new MessageStorage();

    public Mission(int num_generals, int num_traitors, int initiator, int faulty1, int faulty2) {
        num_Processor = num_generals;
        num_Faulty = num_traitors;
        this.initiator = initiator;
        this.faulty1 = faulty1;
        this.faulty2 = faulty2;

        if (num_Processor <= 3 * num_Faulty) {
            throw new IllegalArgumentException("Requires n > 3*m.");
        }
        processors = new Vector<Processor>();
    }

    public int numRounds() {
        return num_Faulty + 1;
    }

    public synchronized void startDuty(Processor p) throws InterruptedException {
        p.assignId(id++);
        processors.add(p);
        if (processors.size() == num_Processor) {
            started = 1;
            notifyAll();
        } else {
            while (started == 0) {
                wait();
            }
        }
    }

    public void broadCastMessages(int id, int round) {
        if (round == 0) {
            if (id == initiator) {
                for (int i = 0; i < num_Processor; i++) {
                    if (i != initiator) {
                        if ( (i == faulty1) || (i == faulty2) ) {
                            System.out.println("Process " + initiator + " is sending " + default_message + " to process " + i + " [FAULTY]");
                            messageStorage.addNewMessage(default_message, initiator, i, 0, initiator, faulty1, faulty2);
                        } else {
                            System.out.println("Process " + initiator + " is sending " + default_message + " to process " + i);
                            messageStorage.addNewMessage(default_message, initiator, i, 0, initiator, faulty1, faulty2);
                        }
                    }
                }
            }
        } else if (round == 1) {
            if (id == initiator) {
                System.out.println("\n===================== ROUND " + round + " =====================");
                int loopTimes = num_Processor - 1;
                for (int i = 0; i <= loopTimes ; i++) {
                    if (i != initiator) {
                        for (int z = 0; z < num_Processor; z ++) {
                            if ( (z == initiator) || (z == i) ) {
                            } else {
                                if ( (i == faulty1) || (i == faulty2) ) {
                                    byte randomZeroOne = 0;
                                    Random random = new Random();
                                    if (random.nextBoolean()) {
                                        randomZeroOne = 1;
                                    } else {
                                        randomZeroOne = 0;
                                    }

                                    if ( (z == faulty1) || (z == faulty2) ) {
                                        System.out.println("Process " + i + "[FAULTY] is sending " + randomZeroOne + " to process " + z + "[FAULTY]");
                                        messageStorage.addNewMessage(randomZeroOne, i, z, 1, initiator, faulty1, faulty2);
                                    } else {
                                        System.out.println("Process " + i + "[FAULTY] is sending " + randomZeroOne + " to process " + z);
                                        messageStorage.addNewMessage(randomZeroOne, i, z,  1, initiator, faulty1, faulty2);
                                    }
                                } else {
                                    if ( (z == faulty1) || (z == faulty2) ) {
                                        System.out.println("Process " + i + " is sending " + default_message + " to process " + z + "[FAULTY]");
                                        messageStorage.addNewMessage(default_message, i, z, 1, initiator, faulty1, faulty2);
                                    } else {
                                        System.out.println("Process " + i + " is sending " + default_message + " to process " + z);
                                        messageStorage.addNewMessage(default_message, i, z, 1, initiator, faulty1, faulty2);
                                    }
                                }
                            }
                        }
                    }
                    System.out.print("\n");
                }
            }
        } else if (round == 2) {
            //If there is only 1 Faulty, It will be the end of the loop => Calculate Final Round
            if (faulty2 == -1) {
                if (id == initiator) {
                    System.out.println("\n==================== FINAL ROUND ====================");
                    for (int z = 0; z < num_Processor; z ++) {
                        if (z == initiator) {
                            System.out.println("Process " + z + " (Initiator)\t\t" + default_message);
                        } else if ( (z == faulty1) || (z == faulty2) ){
                            int newMsg = passMajorityMsgForNextRound(z);
                            System.out.println("Process " + z + " (Faulty)\t\t\t" + newMsg);
                        } else {
                            int newMsg = passMajorityMsgForNextRound(z);
                            System.out.println("Process " + z + "\t\t\t\t\t" + newMsg);
                        }
                    }
                }
            } else {
                if (id == initiator) {
                    System.out.println("\n===================== ROUND " + round + " =====================");
                    int loopTimes = num_Processor - 1;
                    for (int i = 0; i <= loopTimes ; i++) {
                        if (i != initiator) {
                            for (int z = 0; z < num_Processor; z ++) {
                                if ( (z == initiator) || (z == i) ) {
                                } else {
                                    if ( (i == faulty1) || (i == faulty2) ) {
                                        byte randomZeroOne = 0;
                                        Random random = new Random();
                                        if (random.nextBoolean()) {
                                            randomZeroOne = 1;
                                        } else {
                                            randomZeroOne = 0;
                                        }

                                        if ( (z == faulty1) || (z == faulty2) ) {
                                            System.out.println("Process " + i + "[FAULTY] is sending " + randomZeroOne + " to process " + z + "[FAULTY]");
                                            messageStorage.addNewMessage(randomZeroOne, i, z, 2, initiator, faulty1, faulty2);
                                        } else {
                                            System.out.println("Process " + i + "[FAULTY] is sending " + randomZeroOne + " to process " + z);
                                            messageStorage.addNewMessage(randomZeroOne, i, z, 2, initiator, faulty1, faulty2);
                                        }
                                    } else {
                                        int newMsg = passMajorityMsgForNextRound(z);
                                        if ( (z == faulty1) || (z == faulty2) ) {
                                            System.out.println("Process " + i + " is sending " + newMsg + " to process " + z + "[FAULTY]");
                                            messageStorage.addNewMessage((byte) newMsg, i, z, 2, initiator, faulty1, faulty2);
                                        } else {
                                            System.out.println("Process " + i + " is sending " + newMsg + " to process " + z);
                                            messageStorage.addNewMessage((byte) newMsg, i, z, 2, initiator, faulty1, faulty2);
                                        }
                                    }
                                }
                            }
                        }
                        System.out.print("\n");
                    }
                }
            }
        } else {    //FINAL ROUND
            if (id == initiator) {
                System.out.println("\n==================== FINAL ROUND ====================");
                for (int z = 0; z < num_Processor; z ++) {
                    if (z == initiator) {
                        System.out.println("Process " + z + " (Initiator)\t\t" + default_message);
                    } else if ( (z == faulty1) || (z == faulty2) ){
                        int newMsg = passMajorityMsgForNextRound(z);
                        System.out.println("Process " + z + " (Faulty)\t\t\t" + newMsg);
                    } else {
                        int newMsg = passMajorityMsgForNextRound(z);
                        System.out.println("Process " + z + "\t\t\t\t\t" + newMsg);
                    }
                }
            }
        }
    }

    public int passMajorityMsgForNextRound(int i) {
        if (i == 0) {
             return messageStorage.getMajority(messageStorage.vecP0);
        } else if (i == 1) {
            return messageStorage.getMajority(messageStorage.vecP1);
        } else if (i == 2) {
            return messageStorage.getMajority(messageStorage.vecP2);
        } else if (i == 3) {
            return messageStorage.getMajority(messageStorage.vecP3);
        } else if (i == 4) {
            return messageStorage.getMajority(messageStorage.vecP4);
        } else if (i == 5) {
            return messageStorage.getMajority(messageStorage.vecP5);
        } else {
            return messageStorage.getMajority(messageStorage.vecP6);
        }
    }
}



class MessageStorage {

    public Vector<Integer> vecP0 = new Vector<Integer>();
    public Vector<Integer> vecP1 = new Vector<Integer>();
    public Vector<Integer> vecP2 = new Vector<Integer>();
    public Vector<Integer> vecP3 = new Vector<Integer>();
    public Vector<Integer> vecP4 = new Vector<Integer>();
    public Vector<Integer> vecP5 = new Vector<Integer>();
    public Vector<Integer> vecP6 = new Vector<Integer>();

    public MessageStorage() {
    }
    public void insertMessage(int index, byte msg) {
        if (index == 0) {
            vecP0.addElement((int) msg);
        } else if (index == 1) {
            vecP1.addElement((int) msg);
        } else if (index == 2) {
            vecP2.addElement((int) msg);
        } else if (index == 3) {
            vecP3.addElement((int) msg);
        } else if (index == 4) {
            vecP4.addElement((int) msg);
        } else if (index == 5) {
            vecP5.addElement((int) msg);
        } else {
            vecP6.addElement((int) msg);
        }
    }

    public int getMajority(Vector<Integer> whichArray) {
        int countZero = 0, countOne = 0;
        for (int i = 0; i < whichArray.size(); i++) {
            if (whichArray.elementAt(i) == 0) {
                countZero += 1;
            } else {
                countOne += 1;
            }
        }
        if (countOne > countZero) {
            return 1;
        } else {
            return 0;
        }
    }

    public void addNewMessage(byte message, int from, int to, int whichRound, int initiator, int faulty1, int faulty2) {

        //Round 0 only
        if (whichRound == 0) {
            if (to == faulty1) {   //Make sure it runs once
                for (int i = 0; i <= 6; i++) {
                    if (i != initiator) {
                        insertMessage(i, message);
                    }
                }
            }
        } else {
            if (to != initiator) {
                byte newMsg = 0;
                if ( (to == faulty1) || (to == faulty2) ) {
                    if (message == 1) {
                        newMsg = 0;
                    } else {
                        newMsg = 1;
                    }
                } else {
                    newMsg = message;
                }
                insertMessage(to, newMsg);
            }
        }
    }
}

class Processor implements Runnable {

    public Mission mission;
    int id;
    int initiator;

    public Processor(Mission m, int initiator) {
        this.mission = m;
        this.initiator = initiator;
    }

    public void assignId(int id) {
        this.id = id;
    }

    public void run() {
        try {
            startOperation();
        } catch (Exception e) {

        }
    }

    public void startOperation() throws InterruptedException {
        mission.startDuty(this);
        // <= because I have FINAL round
        for (int round = 0; round <= mission.numRounds(); round++) {
            mission.broadCastMessages(id, round);
            sleep(1000);
        }
    }
}