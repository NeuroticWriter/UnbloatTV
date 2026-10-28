package com.dewzone.dlaunch;

import java.util.Comparator;

public class AppComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof AppItem && o2 instanceof AppItem) {
            String l1 = ((AppItem) o1).getLabel();
            String l2 = ((AppItem) o2).getLabel();
            return l1.compareToIgnoreCase(l2);
        }
        return 0;
    }
}

