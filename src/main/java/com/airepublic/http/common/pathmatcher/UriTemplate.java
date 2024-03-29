/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.airepublic.http.common.pathmatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extracts path parameters from URIs used to create web socket connections using the URI template
 * defined for the associated Endpoint.
 */
public class UriTemplate {
    private final String normalized;
    private final List<Segment> segments = new ArrayList<>();
    private final boolean hasParameters;


    public UriTemplate(final String path) throws IOException {

        if (path == null || path.length() == 0 || !path.startsWith("/")) {
            throw new IOException("Invalid URI path: " + path);
        }

        final StringBuilder normalized = new StringBuilder(path.length());
        final Set<String> paramNames = new HashSet<>();

        // Include empty segments.
        final String[] segments = path.split("/", -1);
        int paramCount = 0;
        int segmentCount = 0;

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];

            if (segment.length() == 0) {
                if (i == 0 || i == segments.length - 1 && paramCount == 0) {
                    // Ignore the first empty segment as the path must always
                    // start with '/'
                    // Ending with a '/' is also OK for instances used for
                    // matches but not for parameterised templates.
                    continue;
                } else {
                    // As per EG discussion, all other empty segments are
                    // invalid
                    throw new IllegalArgumentException("Illegal empty segment in URI path: " + path);
                }
            }

            normalized.append('/');
            int index = -1;

            if (segment.startsWith("{") && segment.endsWith("}")) {
                index = segmentCount;
                segment = segment.substring(1, segment.length() - 1);
                normalized.append('{');
                normalized.append(paramCount++);
                normalized.append('}');

                if (!paramNames.add(segment)) {
                    throw new IllegalArgumentException("Duplicate parameter found in segment: " + segment);
                }
            } else {
                if (segment.contains("{") || segment.contains("}")) {
                    throw new IllegalArgumentException("Invalid segment " + segment + " in URI path: " + path);
                }
                normalized.append(segment);
            }

            this.segments.add(new Segment(index, segment));
            segmentCount++;
        }

        this.normalized = normalized.toString();
        hasParameters = paramCount > 0;
    }


    public Map<String, String> match(final UriTemplate candidate) {

        final Map<String, String> result = new HashMap<>();

        // Should not happen but for safety
        if (candidate.getSegmentCount() != getSegmentCount()) {
            return null;
        }

        final Iterator<Segment> candidateSegments = candidate.getSegments().iterator();
        final Iterator<Segment> targetSegments = segments.iterator();

        while (candidateSegments.hasNext()) {
            final Segment candidateSegment = candidateSegments.next();
            final Segment targetSegment = targetSegments.next();

            if (targetSegment.getParameterIndex() == -1) {
                // Not a parameter - values must match
                if (!targetSegment.getValue().equals(candidateSegment.getValue())) {
                    // Not a match. Stop here
                    return null;
                }
            } else {
                // Parameter
                result.put(targetSegment.getValue(), candidateSegment.getValue());
            }
        }

        return result;
    }


    public boolean hasParameters() {
        return hasParameters;
    }


    public int getSegmentCount() {
        return segments.size();
    }


    public String getNormalizedPath() {
        return normalized;
    }


    private List<Segment> getSegments() {
        return segments;
    }

    private static class Segment {
        private final int parameterIndex;
        private final String value;


        public Segment(final int parameterIndex, final String value) {
            this.parameterIndex = parameterIndex;
            this.value = value;
        }


        public int getParameterIndex() {
            return parameterIndex;
        }


        public String getValue() {
            return value;
        }
    }
}
