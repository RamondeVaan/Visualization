package nl.ramondevaan.visualization.mesh;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Element {
    public final String         name;
    public final int            numberOfEntries;
    public final List<Property> properties;

    public Element(String name, int numberOfEntries, List<Property> properties) {
        Validate.notNull(name);
        Validate.notNull(properties);
        Validate.isTrue(numberOfEntries >= 0);

        this.name               = name;
        this.numberOfEntries    = numberOfEntries;
        List<Property> temp     = new ArrayList<>();
        temp.addAll(properties);

        for(int i = 0; i < temp.size(); i++) {
            for(int j = i + 1; j < temp.size(); j++) {
                if(temp.get(i).name.equals(temp.get(j).name)) {
                    throw new IllegalArgumentException("Property name \"" +
                            temp.get(i).name + "\" occurs multiple times");
                }
            }
        }

        this.properties = Collections.unmodifiableList(temp);
    }

    Element(String name, List<Property> properties, int numberOfEntries) {
        this.name               = name;
        this.numberOfEntries    = numberOfEntries;
        this.properties         = properties;
    }

    public final int findProperty(String name) {
        IntStream temp = IntStream.range(0, properties.size());
        return temp.parallel()
                .filter(i -> properties.get(i).name.equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Could not find element identifier \"" +
                        name + "\""));
    }

    public final int[] findProperties(String[] names) {
        return findProperties(names, true);
    }

    public final int[] findProperties(String[] names, boolean throwError) {
        IntStream is = IntStream.range(0, names.length * properties.size());
        int[] found = is.parallel()
                .filter(n -> properties.get(n % properties.size()).name
                        .equals(names[n / properties.size()]))
                .toArray();

        if(found.length < names.length) {
            if(!throwError) {
                return null;
            }
            outer: for(int i = 0; i < names.length; i++) {
                for(int j : found) {
                    if(j / properties.size() == i) {
                        continue outer;
                    }
                    throw new IllegalArgumentException("Could not find element identifier \"" +
                            names[i] + "\"");
                }
            }
        }

        int[] ret = new int[found.length];

        for(int i : found) {
            ret[i / properties.size()] = i % properties.size();
        }

        return ret;
    }

    public static int findElement(List<Element> elements, String name) {
        IntStream temp = IntStream.range(0, elements.size());
        return temp.parallel()
                .filter(i -> elements.get(i).name.equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Could not find element identifier \"" +
                        name + "\""));
    }

    public static List<Element> copyElements(List<Element> elements) {
        List<Element> temp = new ArrayList<>();
        temp.addAll(elements);

        return Collections.unmodifiableList(temp);
    }
}
