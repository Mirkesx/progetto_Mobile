package com.marco_cavalli.lost_and_found.comparators;

import com.marco_cavalli.lost_and_found.objects.FoundItem;
import java.util.Comparator;

public class FoundItemComparator implements Comparator<FoundItem> {

    @Override
    public int compare(FoundItem left, FoundItem right) {
        return right.getTimestamp().compareTo(left.getTimestamp());
    }
}
