package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Filter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class DoubleToStatisticsFilter extends Filter<Double, DescriptiveStatistics> {
    @Override
    protected DescriptiveStatistics updateImpl() throws Exception {
        DescriptiveStatistics ret = new DescriptiveStatistics();

        for(int i = 0; i < getNumberOfInputs(); i++) {
            ret.addValue(getInput(i));
        }

        return ret;
    }
}
