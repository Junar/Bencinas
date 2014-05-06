package cl.gob.datos.bencinas.helpers;

import java.util.Comparator;

import com.junar.searchbenzine.Benzine;

public class BenzineComparator<T> implements Comparator<Benzine> {

    @Override
    public int compare(Benzine lhs, Benzine rhs) {

        if (lhs.getDistance() < rhs.getDistance()) {
            return -1;
        } else if (lhs.getDistance() > rhs.getDistance()) {
            return 1;
        }
        return 0;
    }
}