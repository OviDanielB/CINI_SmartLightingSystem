package org.uniroma2.sdcc.Utils;


/**
 * maintains stastics (mean, percentage of on lamps,etc)
 * for every street
 */
public class StreetStatistics {
    private Integer sampleNumb;
    private Float currentMean;
    private Float currentV;
    private Float onPercentage;

    public StreetStatistics(Integer sampleNumb, Float currentValue, Float currentV, Float onPercentage) {
        this.sampleNumb = sampleNumb;
        this.currentMean = currentValue;
        this.currentV = currentV;
        this.onPercentage = onPercentage;
    }

    public StreetStatistics() {
    }

    public Float stdDev() {
        return (float) Math.sqrt(this.currentV / this.sampleNumb);
    }

    public Integer getSampleNumb() {
        return sampleNumb;
    }

    public void setSampleNumb(Integer sampleNumb) {
        this.sampleNumb = sampleNumb;
    }

    public Float getCurrentMean() {
        return currentMean;
    }

    public void setCurrentMean(Float currentMean) {
        this.currentMean = currentMean;
    }

    public Float getCurrentV() {
        return currentV;
    }

    public void setCurrentV(Float currentStdDev) {
        this.currentV = currentStdDev;
    }

    public Float getOnPercentage() {
        return onPercentage;
    }

    public void setOnPercentage(Float onPercentage) {
        this.onPercentage = onPercentage;
    }

    public void updateOnPercentage(Float on) {
        if (!sampleNumb.equals(0f)) {
            this.onPercentage = onPercentage + (on - onPercentage) / (sampleNumb);
        }
    }

    @Override
    public String toString() {
        return "StreetStatistics{" +
                "sampleNumb=" + sampleNumb +
                ", currentMean=" + currentMean +
                ", currentV=" + currentV +
                ", currentStdDev = " + stdDev() +
                ", onPercentage = " + onPercentage +
                '}';
    }
}
