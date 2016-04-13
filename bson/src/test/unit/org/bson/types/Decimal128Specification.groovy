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

package org.bson.types

import spock.lang.Specification

import static org.bson.types.Decimal128.NEGATIVE_INFINITY
import static org.bson.types.Decimal128.NEGATIVE_ZERO
import static org.bson.types.Decimal128.NaN
import static org.bson.types.Decimal128.POSITIVE_INFINITY
import static org.bson.types.Decimal128.POSITIVE_ZERO
import static org.bson.types.Decimal128.of

class Decimal128Specification extends Specification {

    def 'should have correct constants'() {
        expect:
        POSITIVE_ZERO == new Decimal128(0x3040000000000000L, 0x0000000000000000L)
        NEGATIVE_ZERO == new Decimal128(0xb040000000000000L, 0x0000000000000000L)
        POSITIVE_INFINITY == new Decimal128(0x7800000000000000L, 0x0000000000000000L)
        NEGATIVE_INFINITY == new Decimal128(0xf800000000000000L, 0x0000000000000000L)
        NaN == new Decimal128(0x7c00000000000000L, 0x0000000000000000L)
    }

    def 'should construct from high and low'() {
        given:
        def decimal = new Decimal128(0x3040000000000000L, 0x0000000000000001L)

        expect:
        decimal.high == 0x3040000000000000L
        decimal.low == 0x0000000000000001L
    }

    def 'should construct from simple string'() {
        expect:
        of('0') == new Decimal128(0x3040000000000000L, 0x0000000000000000L)
        of('-0') == new Decimal128(0xb040000000000000L, 0x0000000000000000L)
        of('1') == new Decimal128(0x3040000000000000L, 0x0000000000000001L)
        of('-1') == new Decimal128(0xb040000000000000L, 0x0000000000000001L)
        of('12345678901234567') == new Decimal128(0x3040000000000000L, 0x002bdc545d6b4b87L)
        of('989898983458') == new Decimal128(0x3040000000000000L, 0x000000e67a93c822L)
        of('-12345678901234567') == new Decimal128(0xb040000000000000L, 0x002bdc545d6b4b87L)
        of('0.12345') == new Decimal128(0x3036000000000000L, 0x0000000000003039L)
        of('0.0012345') == new Decimal128(0x3032000000000000L, 0x0000000000003039L)
        of('00012345678901234567') == new Decimal128(0x3040000000000000L, 0x002bdc545d6b4b87L)
    }

    def 'should construct from long'() {
        expect:
        of(1L) == of(new BigDecimal('1'))
        of(Long.MIN_VALUE) == of(new BigDecimal(Long.MIN_VALUE))
        of(Long.MAX_VALUE) == of(new BigDecimal(Long.MAX_VALUE))
    }

    def 'should construct from large BigDecimal'() {
        expect:
        of('12345689012345789012345') == new Decimal128(0x304000000000029dL, 0x42da3a76f9e0d979L)
        of('1234567890123456789012345678901234') == new Decimal128(0x30403cde6fff9732L, 0xde825cd07e96aff2L)
        of('9.999999999999999999999999999999999E+6144') == new Decimal128(0x5fffed09bead87c0L, 0x378d8e63ffffffffL)
        of('9.999999999999999999999999999999999E-6143') == new Decimal128(0x0001ed09bead87c0L, 0x378d8e63ffffffffL)
        of('5.192296858534827628530496329220095E+33') == new Decimal128(0x3040ffffffffffffL, 0xffffffffffffffffL)
    }

    def 'should convert to simple BigDecimal'() {
        expect:
        new Decimal128(0x3040000000000000L, 0x0000000000000000L).bigDecimalValue() == new BigDecimal('0')
        new Decimal128(0x3040000000000000L, 0x0000000000000001L).bigDecimalValue() == new BigDecimal('1')
        new Decimal128(0xb040000000000000L, 0x0000000000000001L).bigDecimalValue() == new BigDecimal('-1')
        new Decimal128(0x3040000000000000L, 0x002bdc545d6b4b87L).bigDecimalValue() == new BigDecimal('12345678901234567')
        new Decimal128(0x3040000000000000L, 0x000000e67a93c822L).bigDecimalValue() == new BigDecimal('989898983458')
        new Decimal128(0xb040000000000000L, 0x002bdc545d6b4b87L).bigDecimalValue() == new BigDecimal('-12345678901234567')
        new Decimal128(0x3036000000000000L, 0x0000000000003039L).bigDecimalValue() == new BigDecimal('0.12345')
        new Decimal128(0x3032000000000000L, 0x0000000000003039L).bigDecimalValue() == new BigDecimal('0.0012345')
        new Decimal128(0x3040000000000000L, 0x002bdc545d6b4b87L).bigDecimalValue() == new BigDecimal('00012345678901234567')
    }

    def 'should convert to large BigDecimal'() {
        expect:
        new Decimal128(0x304000000000029dL, 0x42da3a76f9e0d979L).bigDecimalValue() ==
                new BigDecimal('12345689012345789012345')

        new Decimal128(0x30403cde6fff9732L, 0xde825cd07e96aff2L).bigDecimalValue() ==
                new BigDecimal('1234567890123456789012345678901234')

        new Decimal128(0x5fffed09bead87c0L, 0x378d8e63ffffffffL).bigDecimalValue() ==
                new BigDecimal('9.999999999999999999999999999999999E+6144')

        new Decimal128(0x0001ed09bead87c0L, 0x378d8e63ffffffffL).bigDecimalValue() ==
                new BigDecimal('9.999999999999999999999999999999999E-6143')

        new Decimal128(0x3040ffffffffffffL, 0xffffffffffffffffL).bigDecimalValue() ==
                new BigDecimal('5.192296858534827628530496329220095E+33')
    }

    def 'should convert invalid representations of 0 as BigDecimal 0'() {
        expect:
        new Decimal128(0x6C10000000000000, 0x0).bigDecimalValue() == new BigDecimal('0')
        new Decimal128(0x6C11FFFFFFFFFFFF, 0xffffffffffffffffL).bigDecimalValue() == new BigDecimal('0E+3')
    }

    def 'should detect infinity'() {
        expect:
        POSITIVE_INFINITY.isInfinite()
        NEGATIVE_INFINITY.isInfinite()
        !of('0').isInfinite()
        !of('9.999999999999999999999999999999999E+6144').isInfinite()
        !of('9.999999999999999999999999999999999E-6143').isInfinite()
        !POSITIVE_INFINITY.isFinite()
        !NEGATIVE_INFINITY.isFinite()
        of('0').isFinite()
        of('9.999999999999999999999999999999999E+6144').isFinite()
        of('9.999999999999999999999999999999999E-6143').isFinite()
    }

    def 'should detect NaN'() {
        expect:
        NaN.isNaN()
        new Decimal128(0x7e00000000000000L, 0).isNaN()    // SNaN
        !POSITIVE_INFINITY.isNaN()
        !NEGATIVE_INFINITY.isNaN()
        !of('0').isNaN()
        !of('9.999999999999999999999999999999999E+6144').isNaN()
        !of('9.999999999999999999999999999999999E-6143').isNaN()
    }

    def 'should convert NaN to string'() {
        expect:
        NaN.toString() == 'NaN'
    }

    def 'should convert NaN from string'() {
        expect:
        of('NaN') == NaN
        of('nan') == NaN
        of('nAn') == NaN
    }

    def 'should not convert NaN to BigDecimal'() {
        when:
        NaN.bigDecimalValue()

        then:
        thrown(ArithmeticException)
    }

    def 'should convert infinity to string'() {
        expect:
        POSITIVE_INFINITY.toString() == 'Infinity'
        NEGATIVE_INFINITY.toString() == '-Infinity'
    }

    def 'should convert infinity from string'() {
        expect:
        of('Inf') == POSITIVE_INFINITY
        of('inf') == POSITIVE_INFINITY
        of('inF') == POSITIVE_INFINITY
        of('+Inf') == POSITIVE_INFINITY
        of('+inf') == POSITIVE_INFINITY
        of('+inF') == POSITIVE_INFINITY
        of('Infinity') == POSITIVE_INFINITY
        of('infinity') == POSITIVE_INFINITY
        of('infiniTy') == POSITIVE_INFINITY
        of('+Infinity') == POSITIVE_INFINITY
        of('+infinity') == POSITIVE_INFINITY
        of('+infiniTy') == POSITIVE_INFINITY
        of('-Inf') == NEGATIVE_INFINITY
        of('-inf') == NEGATIVE_INFINITY
        of('-inF') == NEGATIVE_INFINITY
        of('-Infinity') == NEGATIVE_INFINITY
        of('-infinity') == NEGATIVE_INFINITY
        of('-infiniTy') == NEGATIVE_INFINITY
    }

    def 'should convert finite to string'() {
        expect:
        of('0').toString() == '0'
        of('-0').toString() == '-0'
        of('0E10').toString() == '0E+10'
        of('-0E10').toString() == '-0E+10'
        of('1').toString() == '1'
        of('-1').toString() == '-1'
        of('-1.1').toString() == '-1.1'

        of('123E-9').toString() == '1.23E-7'
        of('123E-8').toString() == '0.00000123'
        of('123E-7').toString() == '0.0000123'
        of('123E-6').toString() == '0.000123'
        of('123E-5').toString() == '0.00123'
        of('123E-4').toString() == '0.0123'
        of('123E-3').toString() == '0.123'
        of('123E-2').toString() == '1.23'
        of('123E-1').toString() == '12.3'
        of('123E0').toString() == '123'
        of('123E1').toString() == '1.23E+3'

        of('1234E-7').toString() == '0.0001234'
        of('1234E-6').toString() == '0.001234'

        of('1E6').toString() == '1E+6'
    }

    def 'should convert invalid representations of 0 to string'() {
        expect:
        new Decimal128(0x6C10000000000000, 0x0).bigDecimalValue().toString() == '0'
        new Decimal128(0x6C11FFFFFFFFFFFF, 0xffffffffffffffffL).toString() == '0E+3'
    }


    def 'test equals'() {
        given:
        def d1 = new Decimal128(0x3040000000000000L, 0x0000000000000001L)
        def d2 = new Decimal128(0x3040000000000000L, 0x0000000000000001L)
        def d3 = new Decimal128(0x3040000000000001L, 0x0000000000000001L)
        def d4 = new Decimal128(0x3040000000000000L, 0x0000000000000011L)

        expect:
        d1.equals(d1)
        d1.equals(d2)
        !d1.equals(d3)
        !d1.equals(d4)
        !d1.equals(null)
        !d1.equals(0L)
    }

    def 'test hashCode'() {
        expect:
        new Decimal128(0x3040000000000000L, 0x0000000000000001L).hashCode() == 809500703
    }

    def 'should not convert infinity to BigDecimal'() {
        when:
        decimal.bigDecimalValue()

        then:
        thrown(ArithmeticException)

        where:
        decimal << [POSITIVE_INFINITY, NEGATIVE_INFINITY]
    }

    def 'should not convert negative zero to BigDecimal'() {
        when:
        decimal.bigDecimalValue()

        then:
        thrown(ArithmeticException)

        where:
        decimal << [of('-0'), of('-0E+1'), of('-0E-1')]
    }

    def 'should throw IllegalArgumentException if BigDecimal is too large'() {
        when:
        of(val)

        then:
        thrown(IllegalArgumentException)

        where:
        val << [
                '1234567890123456789012345678901234E+6112',
                '1E-6177',
                '12345678901234567890123456789012345',
                new BigDecimal('12345678901234567890123456789012345')
        ]
    }
}
