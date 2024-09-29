package TP069905_Airport;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class AirTrafficController {   
    private Semaphore runway = new Semaphore(1);
    private Lock[] gates = {new ReentrantLock(), new ReentrantLock(), new ReentrantLock()};
    private Semaphore groundCapacity = new Semaphore(3);

    private static class PlaneRequest {
        int planeId;
        boolean emergency;
        Condition condition;

        PlaneRequest(int planeId, boolean emergency, Condition condition) {
            this.planeId = planeId;
            this.emergency = emergency;
            this.condition = condition;
        }
    }

    private PriorityQueue<PlaneRequest> landingQueue = new PriorityQueue<>(Comparator.comparingInt(p -> p.emergency ? 0 : 1));
    private final Lock queueLock = new ReentrantLock();
    private final Condition queueCondition = queueLock.newCondition();

    public void requestLanding(int planeId, boolean emergency) throws InterruptedException {
        // Removed duplicate print statement
        // Check if there's room on the ground
        if (!groundCapacity.tryAcquire()) {
            System.out.println("[ATC] Plane " + planeId + ": No gates available, wait in sky.");
            groundCapacity.acquire();
        }

        queueLock.lock();
        try {
            Condition myCondition = queueLock.newCondition();
            PlaneRequest request = new PlaneRequest(planeId, emergency, myCondition);
            landingQueue.offer(request);

            // Wait for permission to land
            while (true) {
                if (landingQueue.peek().planeId == planeId && runway.tryAcquire()) {
                    landingQueue.poll();
                    System.out.println("[ATC] Plane " + planeId + " can proceed to land. Runway is clear.");
                    break;
                }
                if (landingQueue.peek().planeId != planeId) {
                    System.out.println("[ATC] Plane " + planeId + ": Waiting for its turn in the landing queue.");
                } else {
                    System.out.println("[ATC] Plane " + planeId + ": Runway not free, waiting for clearance.");
                }
                myCondition.await();
            }
        } finally {
            queueLock.unlock();
        }
    }

    public void land(int planeId, boolean emergency) throws InterruptedException {
        // Moved to the Plane class
    }

    public void takeOff(int planeId) throws InterruptedException {
        runway.acquire();
        System.out.println("[ATC] Plane " + planeId + ": Taking off.");
        Thread.sleep(new Random().nextInt(1000) + 1000); // Simulate takeoff time
        System.out.println("[ATC] Plane " + planeId + ": Departed.");
        openRunway(planeId);
    }

    public void openRunway(int planeId) {
        runway.release();
        queueLock.lock();
        try {
            if (!landingQueue.isEmpty()) {
                PlaneRequest nextPlane = landingQueue.peek();
                nextPlane.condition.signal();
                System.out.println("[ATC] Runway is now free, notifying Plane " + nextPlane.planeId + ".");
            }
        } finally {
            queueLock.unlock();
        }
    }

    public void releaseGroundCapacity() {
        groundCapacity.release();
    }

    public int searchOpenGate(boolean emergency) {
        if (emergency) {
            for (int i = 0; i < gates.length; i++) {
                if (gates[i].tryLock()) {
                    gates[i].unlock();
                    System.out.println("[ATC] Gate " + (i + 1) + " found for emergency landing.");
                    return i;
                }
            }
        }
        for (int i = 0; i < gates.length; i++) {
            if (gates[i].tryLock()) {
                gates[i].unlock();
                return i;
            }
        }
        System.out.println("[ATC] No gates available.");
        return -1;
    }

    public void releaseGate(int gateIndex) {
        gates[gateIndex].unlock();
        queueLock.lock();
        try {
            // Notify any waiting planes that a gate is available
            queueCondition.signalAll();
        } finally {
            queueLock.unlock();
        }
    }

    public void closeGate(int gateIndex) {
        gates[gateIndex].lock();
    }

    public void openGate(int gateIndex) {
        gates[gateIndex].unlock();
    }

    public boolean isRunwayClear() {
        return runway.availablePermits() > 0;
    }

    public boolean isGateClear(int gateIndex) {
        boolean isClear = gates[gateIndex].tryLock();
        if (isClear) {
            gates[gateIndex].unlock();
        }
        return isClear;
    }

    public boolean areAllGatesEmpty() {
        for (Lock gate : gates) {
            if (gate.tryLock()) {
                gate.unlock();
            } else {
                return false;
            }
        }
        return true;
    }
}

