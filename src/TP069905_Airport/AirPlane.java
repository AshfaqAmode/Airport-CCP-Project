package TP069905_Airport;

import java.util.Random;
import java.util.concurrent.*;

public class AirPlane implements Runnable {
    private static int idCounter = 0;
    private int id_plane;
    private boolean EMERG;
    private AirTrafficController atc;
    private Semaphore refueler;
    private long arrivalTime;
    private Passengers passengers;
    private long waitingTime;
    private static Random rand = new Random();

    public AirPlane(boolean emergency, AirTrafficController atc, Semaphore refueler) {
        this.id_plane = ++idCounter;
        this.EMERG = emergency;
        this.atc = atc;
        this.refueler = refueler;
        this.arrivalTime = System.currentTimeMillis();
        this.passengers = new Passengers(rand.nextInt(50) + 1); // Each plane has 1-50 passengers
    }

    public int getId_plane() {
        return id_plane;
    }

    @Override
    public void run() {
        
        String PlaneNamed = "Plane No. " + id_plane + ":";
        Thread.currentThread().setName(PlaneNamed);
        
        try {
            System.out.println(PlaneNamed + " Plane " + id_plane + ": Requesting permission to land. \n");
            atc.requestLanding(id_plane, EMERG);

            if (EMERG) {
                System.out.println(PlaneNamed + " (Emergency): Landing immediately. \n");
            } else {
                System.out.println(PlaneNamed + " Landing.\n");
            }

            System.out.println(PlaneNamed + " Landed.\n");

            int gateNum = atc.searchOpenGate(EMERG);
            
            if (gateNum != -1) {
                atc.closeGate(gateNum);
                System.out.println(PlaneNamed + " Taxiing to gate " + (gateNum + 1) +"\n");
                atc.openRunway(id_plane); // Runway opened
                park(gateNum);
                atc.openGate(gateNum);
            } 
            
            else {
                atc.openRunway(id_plane); // Runway opened
                System.out.println("Air Command: Plane " + id_plane + ": No gates available, fly around.\n");
                while (gateNum == -1) {
                    Thread.sleep(1000);
                    gateNum = atc.searchOpenGate(EMERG);
                }
                atc.closeGate(gateNum);
                System.out.println("[ATC] Plane " + id_plane + ": Gate " + (gateNum + 1) + " available, taxiing to gate.\n");
                park(gateNum);
                atc.openGate(gateNum);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            atc.releaseGroundCapacity();
        }
    }

    private void park(int gateIndex) throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + " Parking at gate " + (gateIndex + 1) +"\n");
        long parkStartTime = System.currentTimeMillis();

        // Passengers deboard
        ExecutorService passengerService = Executors.newFixedThreadPool(1);
        passengerService.submit(() -> {
            try {
                passengers.deboard(id_plane);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        passengerService.shutdown();
        passengerService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        System.out.println(threadName + " All passengers have left.\n");

        // Unload luggage
        System.out.println( threadName + " Unloading cargo.\n");
        Thread.sleep(rand.nextInt(1000) + 1000); // Simulate luggage unloading time
        System.out.println( threadName + " Cargo unloaded.\n");

        // Simulate refilling supplies and cleaning
        System.out.println( threadName + " Refilling amenities, food, supplies and cleaning.\n");
        Thread.sleep(rand.nextInt(1000) + 1000);

        // Refueling (exclusive)
        refueler.acquire();
        System.out.println( threadName + " Refueling.\n");
        refuel();
        System.out.println(threadName + " Refuel complete.\n");
        refueler.release();

        // Load luggage
        System.out.println( threadName + " Loading cargo.\n");
        Thread.sleep(rand.nextInt(1000) + 1000); // Simulate luggage loading time
        System.out.println( threadName + " Cargo loaded.\n");

        // Passengers board
        passengerService = Executors.newFixedThreadPool(1);
        passengerService.submit(() -> {
            try {
                passengers.board(id_plane);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        passengerService.shutdown();
        passengerService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        System.out.println( threadName + " All passengers boarded.\n");

        long dockEndTime = System.currentTimeMillis();
        this.waitingTime = dockEndTime - this.arrivalTime;

        // Update statistics
        EndingReport.updateStatistics(this.waitingTime, passengers.getCount());

        System.out.println( threadName + " Ready for take-off.\n");
        System.out.println( threadName + " Preparing for takeoff.\n");
        atc.takeOff(id_plane);
    }

    private void refuel() throws InterruptedException {
        Thread.sleep(rand.nextInt(1000) + 1000); // Simulate refueling time
    }
}
