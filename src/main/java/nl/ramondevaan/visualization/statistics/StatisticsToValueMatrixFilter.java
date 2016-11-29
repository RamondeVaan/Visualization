package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.utilities.DataUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Arrays;

public class StatisticsToValueMatrixFilter extends Filter<DescriptiveStatistics, ValueMatrix> {
    private final static String[] HEADERS = new String[] {
            "Minimum",
            "Maximum",
            "Mean",
            "Median",
            "1st Quartile",
            "3rd Quartile",
            "Sum",
            "Sum Squared",
            "Variance",
            "Standard Deviation",
            "Geometric Mean",
            "Quadratic Mean",
            "Kurtosis",
            "Skewness",
            "Sample Size"
    };
    private String prefix;

    public final String getPrefix() {
        return prefix;
    }

    public final void setPrefix(String prefix) {
        this.prefix = prefix;
        changed();
    }

    @Override
    protected ValueMatrix updateImpl() throws Exception {
        DescriptiveStatistics d = getInput(0);

        String[] usedHeaders = Arrays.copyOf(HEADERS, HEADERS.length);
        if(prefix != null) {
            for(int i = 0; i < usedHeaders.length; i++) {
                usedHeaders[i] = prefix + " " + usedHeaders[i];
            }
        }

        if(d != null) {
            return new ValueMatrix(usedHeaders, new String[][]{{
                    DataUtils.NUMBER_FORMAT.format(d.getMin()),
                    DataUtils.NUMBER_FORMAT.format(d.getMax()),
                    DataUtils.NUMBER_FORMAT.format(d.getMean()),
                    DataUtils.NUMBER_FORMAT.format(d.getPercentile(50)),
                    DataUtils.NUMBER_FORMAT.format(d.getPercentile(25)),
                    DataUtils.NUMBER_FORMAT.format(d.getPercentile(75)),
                    DataUtils.NUMBER_FORMAT.format(d.getSum()),
                    DataUtils.NUMBER_FORMAT.format(d.getSumsq()),
                    DataUtils.NUMBER_FORMAT.format(d.getVariance()),
                    DataUtils.NUMBER_FORMAT.format(d.getStandardDeviation()),
                    DataUtils.NUMBER_FORMAT.format(d.getGeometricMean()),
                    DataUtils.NUMBER_FORMAT.format(d.getQuadraticMean()),
                    DataUtils.NUMBER_FORMAT.format(d.getKurtosis()),
                    DataUtils.NUMBER_FORMAT.format(d.getSkewness()),
                    String.valueOf(d.getN())
            }});
        }

        return new ValueMatrix(HEADERS, new String[][]{});
    }
}
