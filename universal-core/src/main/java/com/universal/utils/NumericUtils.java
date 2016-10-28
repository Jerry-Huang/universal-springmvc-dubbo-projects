package com.universal.utils;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public final class NumericUtils {

    public static String toShort(final String p) {

        return toShort(p, StringUtils.EMPTY);
    }

    public static String toShort(final String p, final String def) {

        if (!NumberUtils.isNumber(p)) {

            return def;
        }

        return toShort(new BigDecimal(p));
    }

    public static String toShort(final double p) {

        return toShort(String.valueOf(p));
    }

    public static String toShort(final BigDecimal p) {

        return toShort(p, StringUtils.EMPTY);
    }

    public static String toShort(final BigDecimal p, final String def) {

        if (p == null) {

            return def;
        }

        final BigDecimal bd = p.setScale(2, BigDecimal.ROUND_HALF_UP);

        final String bdStr = bd.toString();
        final int pos = bdStr.indexOf('.');
        String bdTail = bdStr.substring(pos + 1);
        String bdHead = bdStr.substring(0, pos);

        bdTail = StringUtils.stripEnd(bdTail, "0");

        return StringUtils.isEmpty(bdTail) ? bdHead : String.format("%s.%s", bdHead , bdTail);

    }

}
