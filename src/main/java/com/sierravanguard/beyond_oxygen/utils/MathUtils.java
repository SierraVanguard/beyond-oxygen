package com.sierravanguard.beyond_oxygen.utils;

import org.joml.Vector3i;

public class MathUtils {
    public static Vector3i sum (Vector3i a, Vector3i b){
        return new Vector3i(a.x+b.x, a.y+b.y, a.z+b.z);
    }
}
