package TP069905_Airport;

import java.util.Random;
import java.util.concurrent.*;

public class Airport {
    private static Semaphore refuelTruck = new Semaphore(1);
    private AirTrafficController atc = new AirTrafficController();
    private static Random rand = new Random();
    private static ExecutorService executor = Executors.newFixedThreadPool(6);

    public void simulate() {
        boolean emergencyAssigned = false;

        for (int i = 0; i < 6; i++) {
            boolean emergency = false;
            if (!emergencyAssigned && (i == 5 || rand.nextInt(6 - i) == 0)) { // Ensure one emergency plane
                emergency = true;
                emergencyAssigned = true;
            }

            AirPlane plane = new AirPlane(emergency, atc, refuelTruck);
            executor.submit(plane);
            try {
                Thread.sleep(rand.nextInt(2000)); // New plane arrives every 0-2 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Perform sanitary check before printing statistics
        performSanitaryCheck();

        EndingReport.printStatistics();
    }

    private void performSanitaryCheck() {
        System.out.println("Performing sanitary check on the airport.");
        try {
            Thread.sleep(2000); // Simulate time for sanitary check
            System.out.println("Sanitary check completed.");
            if (atc.areAllGatesEmpty()) {
                System.out.println("All gates are empty.");
            } else {
                System.out.println("Some gates are still occupied.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Airport airport = new Airport();
        airport.simulate();
    }
}

 