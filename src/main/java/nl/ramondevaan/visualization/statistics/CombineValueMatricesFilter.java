package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Filter;

import java.util.ArrayList;
import java.util.List;

public class CombineValueMatricesFilter extends Filter<ValueMatrix, ValueMatrix> {

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
        List<String> headers = new ArrayList<>(a.headers);
        List<String> bHeaders = new ArrayList<>(b.headers);
        List<Integer> bIndices = new ArrayList<>();
        for(int i = 0; i < bHeaders.size(); i++) {
            bIndices.add(i);
        }

        int[] bMap = new int[bHeaders.size()];

        int index;
        String s;
        for(int i = 0; i < headers.size(); i++) {
            s = headers.get(i);
            index = bHeaders.indexOf(s);
            if(index >= 0) {
                bMap[bIndices.get(index)] = i;
                bHeaders.remove(index);
                bIndices.remove(index);
                if(bHeaders.size() == 0) {
                    break;
                }
            }
        }
        for(int i = 0; i < bHeaders.size(); i++) {
            bMap[bIndices.get(i)] = headers.size();
            headers.add(bHeaders.get(i));
        }

        final int dif = headers.size() - a.headers.size();
        int numberOfRows = a.numberOfRows + b.numberOfRows;
        String[] values = new String[headers.size() * numberOfRows];

        int k = 0;
        int j = 0;
        int i, l;
        for(i = 0; i < a.numberOfRows; i++) {
            for(l = 0; l < a.headers.size(); l++) {
                values[k++] = a.values.get(j++);
            }
            k += dif;
        }
        j = 0;
        for(i = 0; i < b.numberOfRows; i++) {
            for(l = 0; l < bMap.length; l++) {
                values[k + bMap[l]] = b.values.get(j++);
            }
            k += headers.size();
        }

        return new ValueMatrix(headers, values, numberOfRows);
    }
}
