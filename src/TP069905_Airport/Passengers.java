package TP069905_Airport;

public class Passengers {
    private int count;

    public Passengers(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void deboard(int planeId) throws InterruptedException {
        for (int i = 1; i <= count; i++) {
            System.out.println("Passenger_" + i +": Passenger " + i + " exiting Plane " + planeId + ".");
            Thread.sleep(100); // Simulate time for each passenger to deboard
        }
        System.out.println("Passenger_MSG: All passengers exited Plane " + planeId + ".");
    }

    public void board(int planeId) throws InterruptedException {
        for (int i = 1; i <= count; i++) {
            System.out.println("Passenger_" + i +": Passenger " + i + " boarding Plane " + planeId + ".");
            Thread.sleep(100); // Simulate time for each passenger to board
        }
        System.out.println("Passenger_MSG: All passengers boarded Plane " + planeId + ".");
    }
}
