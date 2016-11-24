package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Filter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.nio.DoubleBuffer;

public class DoubleBufferToStatisticsFilter extends Filter<DoubleBuffer, DescriptiveStatistics> {
    @Override
    protected DescriptiveStatistics updateImpl() throws Exception {
        DescriptiveStatistics ret = new DescriptiveStatistics();

        DoubleBuffer buffer;
        for(int i = 0; i < getNumberOfInputs(); i++) {
            buffer = getInput(i);
            buffer.rewind();
            while(buffer.hasRemaining()) {
                ret.addValue(buffer.get());
            }
        }

        return ret;
    }
}
