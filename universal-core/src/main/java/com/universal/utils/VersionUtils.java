package com.universal.utils;

import org.apache.commons.lang3.math.NumberUtils;

public final class VersionUtils {

    public static int compare(final String ver1, final String ver2) {

        String[] ver1Slices = ver1.split("\\.");
        String[] ver2Slices = ver2.split("\\.");

        if (ver1Slices.length >= ver2Slices.length) {

            for (int i = 0; i < ver1Slices.length; i++) {

                if (ver2Slices.length >= i + 1) {

                    int comparedResult = compareSlice(ver1Slices[i], ver2Slices[i]);

                    if (comparedResult != 0) {

                        return comparedResult;
                    }
                } else {

                    return 1;
                }
            }

        } else {

            for (int i = 0; i < ver2Slices.length; i++) {

                if (ver1Slices.length >= i + 1) {

                    int comparedResult = compareSlice(ver1Slices[i], ver2Slices[i]);

                    if (comparedResult != 0) {

                        return comparedResult;
                    }
                } else {

                    return -1;
                }
            }
        }

        return 0;
    }

    private static int compareSlice(final String slice1, final String slice2) {

        if (NumberUtils.isDigits(slice1 + slice2)) {

            return NumberUtils.toInt(slice1) - NumberUtils.toInt(slice2);

        } else {

            return slice1.compareTo(slice2);
        }
    }

    public static boolean isBefor2_1(final String version) {

        return compare(version, "2.1") < 0;
    }

    public static boolean is1_2_160222(final String version) {

        return version.equals("1.2.160222");
    }
}
