package org.apache.cassandra.casserole.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class ParseHelp {
    
    // returns file,paira,pairb,...,pairan,pairbn,cur_count,size
    public static List<String> parsePendingFile(String output) {
        List<String> list = new ArrayList<String>();
        String[] lines = output.split("\n\t");
        // let's regex this thing up. we have two lines like this:
        // jdyj.db/(6184164217682364795,6638853123181576232),(6722644748389737885,4335052370488432172)
        // progress=195000000/1000000000 - 19%
        Pattern firstLine = Pattern.compile("(.*\\.db)/([\\(\\d+,\\d+\\)]++)");
        Matcher firstMatch = firstLine.matcher(lines[0]);
        assert firstMatch.matches() : "No match: " + lines[0];
        String file = firstMatch.group(1);
        list.add(file);
        if (firstMatch.groupCount() > 1) {
            // (6184164217682364795,6638853123181576232),(6722644748389737885,4335052370488432172)
            String pairs = firstMatch.group(2).replaceAll("\\(", "").replaceAll("\\)", "");
            StringTokenizer toker = new StringTokenizer(pairs, ",");
            while (toker.hasMoreTokens())
                list.add(toker.nextToken());
        }
        Pattern secondLine = Pattern.compile("progress=(\\d+)/(\\d+)\\s.*");
        Matcher secondMatch = secondLine.matcher(lines[1].trim());
        assert secondMatch.matches() : "No match: " + lines[1].trim();
        list.add(secondMatch.group(1));
        list.add(secondMatch.group(2));
        return list;
    }
}
