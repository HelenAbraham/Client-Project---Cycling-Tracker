package uk.co.cardiff.council.morebike;

public class Bike implements Emissions {
    private double distance;
    private final int emissionsPerKilometre = 250;
    private final int emissionsSavedPerKilometre = 250;

    public Bike(double kilometre) {
        distance = kilometre;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double kilometre) {
        distance = kilometre;
    }

    @Override
    public double getEmissions() {
        return distance * emissionsSavedPerKilometre;
    }

    @Override
    public String toString(){
        return String.format("%s: %.2f",
                "Emissions of  ", getDistance() + " saved " );
    }


}
