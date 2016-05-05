/*
 * Copyright 2016 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.bson;

import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import org.bson.types.Decimal128;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import util.JsonPoweredTestHelper;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static org.bson.BsonDocument.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

// BSON tests powered by language-agnostic JSON-based tests included in test resources
@RunWith(Parameterized.class)
public class GenericBsonDecimal128Test {

    enum TestCaseType {
        VALID,
        PARSE_ERROR
    }

    private final BsonDocument testDefinition;
    private final BsonDocument testCase;
    private final TestCaseType testCaseType;

    public GenericBsonDecimal128Test(final String description, final BsonDocument testDefinition, final BsonDocument testCase,
                                     final TestCaseType testCaseType) {
        this.testDefinition = testDefinition;
        this.testCase = testCase;
        this.testCaseType = testCaseType;
    }

    @Test
    public void shouldPassAllOutcomes() {
        switch (testCaseType) {
            case VALID:
                runValid();
                break;
            case PARSE_ERROR:
                runParseError();
                break;
            default:
                throw new IllegalArgumentException(format("Unsupported test case type %s", testCaseType));
        }
    }

    private void runValid() {
        String key = testDefinition.getString("test_key").getValue();
        String subjectHex = testCase.getString("subject").getValue().toUpperCase();
        String stringValue = testCase.containsKey("match_string")
                                     ? testCase.getString("match_string").getValue() : testCase.getString("string").getValue();
        String description = testCase.getString("description").getValue();

        BsonDocument decodedDocument = decodeToDocument(subjectHex, description);

        assertEquals(String.format("Failed to generate correct string value '%s' with description '%s'", stringValue, description),
                stringValue, decodedDocument.getDecimal128(key).getValue().toString());

        String actualSubject = encodeToHex(decodedDocument);
        assertEquals(format("Failed to create expected BSON for document with description '%s'", description),
                subjectHex, actualSubject);

        if (testCase.containsKey("match_string")) {
            String uncanonicalStringValue = testCase.getString("string").getValue();
            Decimal128 parsed = null;
            try {
                parsed = Decimal128.parse(uncanonicalStringValue);
            } catch (Exception e) {
                fail(format("Failed to parse '%s' with description '%s'", uncanonicalStringValue, description));
            }
            assertEquals(format("Failed to canonicalize %s to %s", uncanonicalStringValue, stringValue),
                    stringValue, parsed.toString());
        }

        if (testCase.getBoolean("from_extjson", BsonBoolean.TRUE).getValue()) {
            assertEquals(String.format("Failed to parse expected decimal '%s' with description '%s'", stringValue,
                    description), Decimal128.parse(stringValue), decodedDocument.getDecimal128(key).getValue());

            if (testCase.containsKey("extjson")) {
                assertEquals(format("Failed to decode to expected document with description '%s'", description),
                        parse(testCase.getString("extjson").getValue()), decodedDocument);
            }
        }

        if (testCase.getBoolean("to_extjson", BsonBoolean.TRUE).getValue()) {
            if (testCase.containsKey("extjson")) {
                assertEquals(format("Failed to create expected JSON for document with description '%s'", description),
                        stripWhiteSpace(testCase.getString("extjson").getValue()), stripWhiteSpace(decodedDocument.toJson()));
            }
        }
    }

    private BsonDocument decodeToDocument(final String subjectHex, final String description) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(DatatypeConverter.parseHexBinary(subjectHex));
        BsonDocument actualDecodedDocument = new BsonDocumentCodec().decode(new BsonBinaryReader(byteBuffer),
                DecoderContext.builder().build());

        if (byteBuffer.hasRemaining()) {
            fail(format("Should have consumed all bytes, but " + byteBuffer.remaining() + " still remain in the buffer "
                                + "for document with description ", description));
        }
        return actualDecodedDocument;
    }

    private String encodeToHex(final BsonDocument decodedDocument) {
        BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
        new BsonDocumentCodec().encode(new BsonBinaryWriter(outputBuffer), decodedDocument, EncoderContext.builder().build());
        return DatatypeConverter.printHexBinary(outputBuffer.toByteArray());
    }

    private void runParseError() {
        try {
            String description = testCase.getString("description").getValue();
            Decimal128.parse(testCase.getString("subject").getValue());
            fail(format("Should have failed parsing for subject with description '%s'", description));
        } catch (NumberFormatException e) {
            // all good
        } catch (IllegalArgumentException e) {
            // all good
        }
    }


    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() throws URISyntaxException, IOException {
        List<Object[]> data = new ArrayList<Object[]>();
        for (File file : JsonPoweredTestHelper.getTestFiles("/decimal128")) {
            BsonDocument testDocument = JsonPoweredTestHelper.getTestDocument(file);
            for (BsonValue curValue : testDocument.getArray("valid", new BsonArray())) {
                BsonDocument testCaseDocument = curValue.asDocument();
                data.add(new Object[]{createTestCaseDescription(testDocument, testCaseDocument, "valid"), testDocument, testCaseDocument,
                        TestCaseType.VALID});
            }

            for (BsonValue curValue : testDocument.getArray("parseErrors", new BsonArray())) {
                BsonDocument testCaseDocument = curValue.asDocument();
                data.add(new Object[]{createTestCaseDescription(testDocument, testCaseDocument, "parseError"), testDocument,
                        testCaseDocument, TestCaseType.PARSE_ERROR});
            }
        }
        return data;
    }

    private static String createTestCaseDescription(final BsonDocument testDocument, final BsonDocument testCaseDocument,
                                                    final String testCaseType) {
        return testDocument.getString("description").getValue()
                       + "[" + testCaseType + "]"
                       + ": " + testCaseDocument.getString("description").getValue();
    }

    private String stripWhiteSpace(final String json) {
        return json.replace(" ", "");
    }
}
