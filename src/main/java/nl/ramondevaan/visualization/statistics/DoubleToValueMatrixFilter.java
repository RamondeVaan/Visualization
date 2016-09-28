package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.utilities.DataUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DoubleToValueMatrixFilter extends Filter<Double, ValueMatrix> {
    private List<String> names;
    private DecimalFormat format;

    public DoubleToValueMatrixFilter() {
        super(0);
        names = new ArrayList<>();
        format = DataUtils.NUMBER_FORMAT;
    }

    public final void setNumberFormat(DecimalFormat format) {
        this.format = format == null ?
                DataUtils.NUMBER_FORMAT : format;
    }

    public final void addInput(String name, Source<Double> source) {
        setInput(names.size(), source);
        names.add(name);
    }

    @Override
    protected ValueMatrix updateImpl() throws Exception {
        String[] values = new String[names.size()];
        for(int i = 0; i < values.length; i++) {
            values[i] = format.format(getInput(i));
        }

        return new ValueMatrix(names
                .toArray(new String[names.size()]),
                values, 1);
    }
}
