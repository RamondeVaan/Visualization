package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CombineValueMatricesColumnFilter extends Filter<ValueMatrix, ValueMatrix> {
    @Override
    protected ValueMatrix updateImpl() throws Exception {
        final int numberOfInputs = getNumberOfInputs();
        if(numberOfInputs <= 0) {
            throw new IllegalArgumentException("No inputs given");
        }

        ValueMatrix matrix = getInput(0);
        for(int i = 1; i < numberOfInputs; i++) {
            matrix = combine(matrix, getInput(i));
        }

        return matrix;
    }

    private static ValueMatrix combine(ValueMatrix a, ValueMatrix b) {
        if(a.numberOfRows != b.numberOfRows) {
            throw new IllegalArgumentException("Matrices need to equal in number of rows");
        }

        List<String> shared = a.headers.parallelStream()
                .filter(b.headers::contains)
                .collect(Collectors.toList());
        if(shared.size() > 0) {
            throw new IllegalArgumentException("Matrices shared column headers:\n" +
                    String.join("\n", shared));
        }

        List<String> headers = new ArrayList<>(a.headers);
        headers.addAll(b.headers);
        List<String> values = new ArrayList<>();

        for(int i = 0; i < a.numberOfRows; i++) {
            values.addAll(a.values.subList(i * a.headers.size(),
                    (i + 1) * a.headers.size()));
            values.addAll(b.values.subList(i * b.headers.size(),
                    (i + 1) * b.headers.size()));
        }

        return new ValueMatrix(headers, values, a.numberOfRows);
    }
}
