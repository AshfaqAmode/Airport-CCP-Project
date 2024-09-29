package TP069905_Airport;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

public class EndingReport {
    private static final AtomicLong totalWaitingTime = new AtomicLong(0);
    private static final AtomicLong totalPassengers = new AtomicLong(0);
    private static final AtomicLong maxWaitingTime = new AtomicLong(Long.MIN_VALUE);
    private static final AtomicLong minWaitingTime = new AtomicLong(Long.MAX_VALUE);
    private static final AtomicInteger planesServed = new AtomicInteger(0);

    public static void updateStatistics(long waitingTime, int passengers) {
        totalWaitingTime.addAndGet(waitingTime);
        totalPassengers.addAndGet(passengers);
        maxWaitingTime.updateAndGet(val -> Math.max(val, waitingTime));
        minWaitingTime.updateAndGet(val -> Math.min(val, waitingTime));
        planesServed.incrementAndGet();
    }

    public static void printStatistics() {
        JFrame EndReport = new JFrame("Airport Statistics");
        EndReport.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        EndReport.setSize(700, 600);
        EndReport.setTitle("Final Report");
       EndReport.setLayout(new GridLayout(0, 1));

        Font font = new Font("Arial", Font.PLAIN, 20);

        JLabel totalWaitingTimeLabel = new JLabel("Total waiting time: " + getTotalWaitingTime() + " ms");
        totalWaitingTimeLabel.setFont(font);

        JLabel totalPassengersLabel = new JLabel("Total passengers: " + getTotalPassengers());
        totalPassengersLabel.setFont(font);

        JLabel avgWaitingTimeLabel = new JLabel("Average plane waiting time: " + getAverageWaitingTime() + " ms");
        avgWaitingTimeLabel.setFont(font);

        JLabel maxWaitingTimeLabel = new JLabel("Maximum plane waiting time for parking: " + getMaxWaitingTime() + " ms");
        maxWaitingTimeLabel.setFont(font);

        JLabel minWaitingTimeLabel = new JLabel("Min plane waiting time for parking: " + getMinWaitingTime() + " ms");
        minWaitingTimeLabel.setFont(font);

        JLabel planesServedLabel = new JLabel("Number of planes served: " + getPlanesServed());
        planesServedLabel.setFont(font);

        EndReport.add(totalWaitingTimeLabel);
        EndReport.add(totalPassengersLabel);
        EndReport.add(avgWaitingTimeLabel);
        EndReport.add(maxWaitingTimeLabel);
        EndReport.add(minWaitingTimeLabel);
        EndReport.add(planesServedLabel);

        EndReport.setVisible(true);
    }

    public static long getTotalWaitingTime() {
        return totalWaitingTime.get();
    }

    public static long getTotalPassengers() {
        return totalPassengers.get();
    }

    public static double getAverageWaitingTime() {
        long planeCount = planesServed.get();
        if (planeCount == 0) {
            return 0;
        }
        return (double) totalWaitingTime.get() / planeCount;
    }

    public static long getMaxWaitingTime() {
        return maxWaitingTime.get();
    }

    public static long getMinWaitingTime() {
        return minWaitingTime.get();
    }

    public static int getPlanesServed() {
        return planesServed.get();
    }
}
