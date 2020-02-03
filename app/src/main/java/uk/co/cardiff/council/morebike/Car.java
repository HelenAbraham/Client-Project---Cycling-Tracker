package uk.co.cardiff.council.morebike;

public class Car implements Emissions {
    private double averageDistance;
    private final int averageEissionsSavedPerKilometre = 271;

    //km = Kilometre
    public Car(double km) {
        this.averageDistance = km;
    }

    public double getAverageDistance() {
        return averageDistance;
    }

    public void setAverageDistance(double km) {
        this.averageDistance = km;
    }

    @Override
    public double getEmissions() {
        return ((getAverageDistance() * averageEissionsSavedPerKilometre));
    }

    @Override
    public String toString() {
        return String.format("%s: %.2f \\n%s: %.2f \\n",
                "the average distance drove by car would be ", getAverageDistance(),
                "emissions of ", getEmissions() + "saved ");

    }
}
