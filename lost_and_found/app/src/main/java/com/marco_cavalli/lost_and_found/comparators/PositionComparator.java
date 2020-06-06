package com.marco_cavalli.lost_and_found.comparators;

import com.marco_cavalli.lost_and_found.objects.Position;

import java.util.Comparator;

public class PositionComparator implements Comparator<Position> {

    @Override
    public int compare(Position left, Position right) {
        return right.getPos_id().compareTo(left.getPos_id());
    }
}
