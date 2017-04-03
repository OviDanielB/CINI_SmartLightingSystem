package org.uniroma2.sdcc.Utils;

public class AvgCalculator {

    private Float sum = 0f;
    private Integer n = 0;

    public AvgCalculator() {
    }

    public Float getAvg() {
        if (n == 0)
            return 0f;

        return sum / n;
    }

    public void add(Float x) {
        sum += x;
        n++;
    }

    public void sub(Float x) {
        if (sum < x || n <= 0)
            throw new IllegalArgumentException("result must be >= 0");

        sum -= x;
        n--;
    }

    public Float getSum() {
        return sum;
    }

    public void setSum(Float sum) {
        this.sum = sum;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

}
