/*
 * Copyright 2016 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bson.types;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import static java.math.MathContext.DECIMAL128;
import static java.util.Arrays.asList;

/**
 * A representation of a 128-bit decimal.
 *
 * @since 3.4
 */
public final class Decimal128 implements Serializable {

    private static final long serialVersionUID = 4570973266503637887L;

    private static final long INFINITY_MASK = 0x7800000000000000L;
    private static final long NaN_MASK = 0x7c00000000000000L;
    private static final long SIGN_BIT_MASK = 1L << 63;
    private static final int MIN_EXPONENT = -6176;
    private static final int MAX_EXPONENT = 6111;

    private static final int EXPONENT_OFFSET = 6176;
    private static final int MAX_BIT_LENGTH = 113;

    private static final Set<String> NaN_STRINGS = new HashSet<String>(asList("nan"));
    private static final Set<String> POSITIVE_INFINITY_STRINGS = new HashSet<String>(asList("inf", "+inf", "infinity", "+infinity"));
    private static final Set<String> NEGATIVE_INFINITY_STRINGS = new HashSet<String>(asList("-inf", "-infinity"));

    /**
     * A constant holding the positive infinity of type {@code Decimal128}.  It is equal to the value return by
     * {@code Decimal128.valueOf("Infinity")}.
     */
    public static final Decimal128 POSITIVE_INFINITY = new Decimal128(INFINITY_MASK, 0);

    /**
     * A constant holding the negative infinity of type {@code Decimal128}.  It is equal to the value return by
     * {@code Decimal128.valueOf("-Infinity")}.
     */
    public static final Decimal128 NEGATIVE_INFINITY = new Decimal128(INFINITY_MASK | SIGN_BIT_MASK, 0);

    /**
     * A constant holding a Not-a-Number (NaN) value of type {@code Decimal128}.  It is equal to the value return by
     * {@code Decimal128.valueOf("NaN")}.
     */
    public static final Decimal128 NaN = new Decimal128(NaN_MASK, 0);

    /**
     * A constant holding a postive zero value of type {@code Decimal128}.  It is equal to the value return by
     * {@code Decimal128.valueOf("0")}.
     */
    public static final Decimal128 POSITIVE_ZERO = Decimal128.valueOf("0");

    /**
     * A constant holding a negative zero value of type {@code Decimal128}.  It is equal to the value return by
     * {@code Decimal128.valueOf("-0")}.
     */
    public static final Decimal128 NEGATIVE_ZERO = Decimal128.valueOf("-0");

    private final long high;
    private final long low;

    /**
     * Returns a Decimal128 value representing the given String.
     *
     * @param value the Decimal128 value represented as a String
     * @return the Decimal128 value representing the given String
     */
    public static Decimal128 valueOf(final String value) {
        String lowerCasedValue = value.toLowerCase();

        if (NaN_STRINGS.contains(lowerCasedValue)) {
            return NaN;
        }
        if (POSITIVE_INFINITY_STRINGS.contains(lowerCasedValue)) {
            return POSITIVE_INFINITY;
        }
        if (NEGATIVE_INFINITY_STRINGS.contains(lowerCasedValue)) {
            return NEGATIVE_INFINITY;
        }
        return valueOf(new BigDecimal(value), value.charAt(0) == '-');
    }

    /**
     * Returns a Decimal128 value representing the given long.
     *
     * @param value the Decimal128 value represented as a long
     * @return the Decimal128 value representing the given long
     */
    public static Decimal128 valueOf(final long value) {
        return valueOf(new BigDecimal(value, DECIMAL128));
    }

    /**
     * Returns a Decimal128 value representing the given BigDecimal.
     *
     * @param value the Decimal128 value represented as a BigDecimal
     * @return the Decimal128 value representing the given BigDecimal
     */
    public static Decimal128 valueOf(final BigDecimal value) {
        return valueOf(value, value.signum() == -1);
    }

    /**
     * Constructs an instance with the given high and low order bits representing this Decimal128.  Under normal circumstances, this
     * constructor will not be used, as it requires knowledge of the Decimal128 encoding scheme.
     *
     * @param high the high-order 64 bits
     * @param low  the low-order 64 bits
     */
    public Decimal128(final long high, final long low) {
        this.high = high;
        this.low = low;
    }

    // isNegative is necessary to detect -0, which can't be represented with a BigDecimal
    private static Decimal128 valueOf(final BigDecimal value, final boolean isNegative) {
        long high = 0;
        long low = 0;

        long exponent = -value.scale();

        if ((exponent < MIN_EXPONENT) || (exponent > MAX_EXPONENT)) {
            throw new IllegalArgumentException("Exponent is out of range for Decimal128 encoding: " + exponent);
        }

        if (value.unscaledValue().bitLength() > MAX_BIT_LENGTH) {
            throw new IllegalArgumentException("Unscaled roundedValue is out of range for Decimal128 encoding:"
                    + value.unscaledValue());
        }

        BigInteger significand = value.unscaledValue().abs();
        int bitLength = significand.bitLength();

        for (int i = 0; i < Math.min(64, bitLength); i++) {
            if (significand.testBit(i)) {
                low |= 1L << i;
            }
        }

        for (int i = 64; i < bitLength; i++) {
            if (significand.testBit(i)) {
                high |= 1L << (i - 64);
            }
        }

        long biasedExponent = exponent + EXPONENT_OFFSET;

        high |= biasedExponent << 49;

        if (value.signum() == -1 || isNegative) {
            high |= SIGN_BIT_MASK;
        }

        return new Decimal128(high, low);
    }

    /**
     * Gets the high-order 64 bits of this Decimal128.
     *
     * @return the high-order 64 bits of this Decimal128
     */
    public long getHigh() {
        return high;
    }

    /**
     * Gets the low-order 64 bits of this Decimal128.
     *
     * @return the low-order 64 bits of this Decimal128
     */
    public long getLow() {
        return low;
    }

    /**
     * Gets a BigDecimal that is equivalent to this Decimal128.
     *
     * @return a BigDecimal that is equivalent to this Decimal128
     */
    public BigDecimal bigDecimalValue() {

        if (isNaN()) {
            throw new ArithmeticException("NaN can not be converted to a BigDecimal");
        }

        if (isInfinite()) {
            throw new ArithmeticException("Infinity can not be converted to a BigDecimal");
        }

        BigDecimal bigDecimal = bigDecimalValueNoNegativeZeroCheck();

        // If the BigDecimal is 0, but the Decimal128 is negative, that means we have -0.
        if (isNegative() && bigDecimal.signum() == 0) {
            throw new ArithmeticException("Negative zero can not be converted to a BigDecimal");
        }

        return bigDecimal;
    }

    private BigDecimal bigDecimalValueNoNegativeZeroCheck() {
        int scale = -getExponent();

        if (twoHighestCombinationBitsAreSet()) {
            return BigDecimal.valueOf(0, scale);
        }

        return new BigDecimal(new BigInteger(isNegative() ? -1 : 1, getBytes()), scale);
    }

    // May have leading zeros.  Strip them before considering making this method public
    private byte[] getBytes() {
        byte[] bytes = new byte[15];

        long mask = 0x00000000000000ff;
        for (int i = 14; i >= 7; i--) {
            bytes[i] = (byte) ((low & mask) >>> ((14 - i) << 3));
            mask = mask << 8;
        }

        mask = 0x00000000000000ff;
        for (int i = 6; i >= 1; i--) {
            bytes[i] = (byte) ((high & mask) >>> ((6 - i) << 3));
            mask = mask << 8;
        }

        mask = 0x0001000000000000L;
        bytes[0] = (byte) ((high & mask) >>> 48);
        return bytes;
    }

    // Consider making this method public
    int getExponent() {
        if (twoHighestCombinationBitsAreSet()) {
            return (int) ((high & 0x1fffe00000000000L) >>> 47) - EXPONENT_OFFSET;
        } else {
            return (int) ((high & 0x7fff800000000000L) >>> 49) - EXPONENT_OFFSET;
        }
    }

    private boolean twoHighestCombinationBitsAreSet() {
        return (high & 3L << 61) == 3L << 61;
    }

    /**
     * Returns true if this Decimal128 is negative.
     *
     * @return true if this Decimal128 is negative
     */
    public boolean isNegative() {
        return (high & SIGN_BIT_MASK) == SIGN_BIT_MASK;
    }

    /**
     * Returns true if this Decimal128 is infinite.
     *
     * @return true if this Decimal128 is infinite
     */
    public boolean isInfinite() {
        return (high & INFINITY_MASK) == INFINITY_MASK;
    }

    /**
     * Returns true if this Decimal128 is finite.
     *
     * @return true if this Decimal128 is finite
     */
    public boolean isFinite() {
        return !isInfinite();
    }

    /**
     * Returns true if this Decimal128 is Not-A-Number (NaN).
     *
     * @return true if this Decimal128 is Not-A-Number
     */
    public boolean isNaN() {
        return (high & NaN_MASK) == NaN_MASK;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Decimal128 that = (Decimal128) o;

        if (high != that.high) {
            return false;
        }
        if (low != that.low) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (low ^ (low >>> 32));
        result = 31 * result + (int) (high ^ (high >>> 32));
        return result;
    }

    @Override
    public String toString() {
        if (isNaN()) {
            return "NaN";
        }
        if (isInfinite()) {
            if (isNegative()) {
                return "-Infinity";
            } else {
                return "Infinity";
            }
        }
        return toStringWithBigDecimal();
    }

    private String toStringWithBigDecimal() {
        StringBuilder buffer = new StringBuilder();

        BigDecimal bigDecimal = bigDecimalValueNoNegativeZeroCheck();
        char[] significand = bigDecimal.unscaledValue().abs().toString().toCharArray();

        if (isNegative()) {
            buffer.append('-');
        }

        int exponent = -bigDecimal.scale();
        int adjustedExponent = exponent + (significand.length - 1);
        if (exponent <= 0 && adjustedExponent >= -6) {
            if (exponent == 0) {
                buffer.append(significand);
            } else {
                int pad = -exponent - significand.length;
                if (pad >= 0) {
                    buffer.append('0');
                    buffer.append('.');
                    for (int i = 0; i < pad; i++) {
                        buffer.append('0');
                    }
                    buffer.append(significand, 0, significand.length);
                } else {
                    buffer.append(significand, 0, -pad);
                    buffer.append('.');
                    buffer.append(significand, -pad, -exponent);
                }
            }
        } else {
            buffer.append(significand[0]);
            if (significand.length > 1) {
                buffer.append('.');
                buffer.append(significand, 1, significand.length - 1);
            }
            buffer.append('E');
            if (adjustedExponent > 0) {
                buffer.append('+');
            }
            buffer.append(adjustedExponent);
        }
        return buffer.toString();
    }
}
