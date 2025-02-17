package com.easyerp.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil   {
	public static BigDecimal format(BigDecimal value) {
        return value != null ? value.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

}
