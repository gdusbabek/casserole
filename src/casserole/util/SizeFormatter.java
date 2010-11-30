package casserole.util;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file0
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** formats numbers concisely */
public class SizeFormatter extends Format {
    private static DecimalFormat decLow = new DecimalFormat("0.00");
    private static DecimalFormat decHigh = new DecimalFormat("0.0###");
    
    public enum Type {
        Bytes,
        DecimalSmall,
        DecimalLarge,
        Milliseconds,
        Percent
    }
    private Type type;
    
    public SizeFormatter(Type type) {
        this.type = type;
    }
    
    public StringBuffer format(Object src, StringBuffer dst, FieldPosition pos) {
        // not sure what to do with pos...
        if (dst == null)
            dst = new StringBuffer();
        if (src == null)
            return dst;
        else if (!(src instanceof Number))
            dst.append(src.toString());
        else {
            long val = ((Number)src).longValue();
            
            if (type == Type.Bytes) {
                if (val < 800)
                    formatAs(Bytes.B, (Number)src, dst, decLow);
                else if (val < 800000)
                    formatAs(Bytes.KB, (Number)src, dst, decLow);
                else if (val < 800000000)
                    formatAs(Bytes.MB, (Number)src, dst, decLow);
                else if (val < 800000000000L) // 800B
                    formatAs(Bytes.GB, (Number)src, dst, decLow);
                else
                    formatAs(Bytes.TB, (Number)src, dst, decHigh);
            } else if (type == Type.DecimalSmall) {
                if (val < 1000)
                    formatAs(DecimalSmall.B, (Number)src, dst, decLow);
                else if (val < 1000000)
                    formatAs(DecimalSmall.K, (Number)src, dst, decLow);
                else
                    formatAs(DecimalSmall.M, (Number)src, dst, decLow);
            } else if (type == Type.DecimalLarge) {
                if (val < 1000000)
                    formatAs(DecimalLarge.B, (Number)src, dst, decLow);
                else
                    formatAs(DecimalLarge.M, (Number)src, dst, decHigh);
            } else if (type == Type.Milliseconds) {
                if (val == Double.NaN)
                    dst.append("fkd");
                if (val < 1000)
                    formatAs(Milliseconds.ms, (Number)src, dst, decLow);
                else if (val < 300000) // 5 mins
                    formatAs(Milliseconds.s, (Number)src, dst, decLow);
                else if (val < 7200000) // 2 hrs
                    formatAs(Milliseconds.m, (Number)src, dst, decLow);
                else
                    formatAs(Milliseconds.h, (Number)src, dst, decLow);
            } else if (type == Type.Percent)
                formatAs(Percent.Pct, (Number)src, dst, decLow);
        }
        return dst;
    }

    private static void formatAs(Magnitude mag, Number src, StringBuffer dst, DecimalFormat fmt) {
        if (src instanceof Float || src instanceof Double) {
            // I'm aware of the problems casting big numbers to doubles, but that shouldn't happen.
            double v = src.doubleValue() / (double)mag.multiplier();
            if (Double.isNaN(v))
                dst.append("-");
            else
                dst.append(fmt.format(v) + mag.label());
        } else {
            long lv = src.longValue();
            long lvNoFraction = lv / mag.multiplier();
            long left = lv % mag.multiplier();
            double frac = figureFraction(left, mag.multiplier());
            assert frac < 1;
            
            if (frac > 0) {
                double result = (double)lvNoFraction + frac;
                dst.append(fmt.format(result));
            } else
                dst.append(lvNoFraction);
            if (mag.label().length() > 0) {
                dst.append(" ");
                dst.append(mag.label());
            }
            
        }
    }
    
    // computes a fraction when it can.
    private static double figureFraction(long up, long down) {
        // i need terms that can be expressed as a fraction. java can't handle casting a huge num to a double to express
        // a ratio. whittle down until something manageable is produced.
        while (down > Double.MAX_VALUE) {
            up /= 10;
            down /= 10;
            if (up == 0)
                return 0;
        }
        return (double)up/(double)down;
        
    }
    
    // todo: implement this.
    public Object parseObject(String source, ParsePosition pos)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
    
    /** magnitude interface */
    private interface Magnitude {
        long multiplier();
        String label();
    }
    
    // todo: these could probably all be combined.
    
    private enum Percent implements Magnitude {
        Pct(1);
        private long mult;
        Percent(long mult) {
            this.mult = mult;
        }
        public long multiplier() { return mult; }
        
        public String label() {
            return "%";
        }
    }
    
    private enum Milliseconds implements Magnitude {
        ms(1), s(1000), m(60000), h(3600000);
        private long mult;
        Milliseconds(long mult) {
            this.mult = mult;
        }

        public long multiplier() { return mult; }

        public String label() {
            switch (this) {
                default: return name();
            }
        }
    }
    
    // used for showing byte representations.
    private enum Bytes implements Magnitude {
        B(1), KB(1024), MB(1048576), GB(1073741824), TB(1099511627776L);
        
        private long mult;
        
        Bytes(long mult) {
            this.mult = mult;
        }
        
        public long multiplier() { return mult; }
        
        public String label() {
            switch (this) {
                default: return name();
            }
        }
    }
    
    // used for showing base10, fine granularity.
    private enum DecimalSmall implements Magnitude {
        B(1), K(1000), M(1000000);
        
        private long mult;
        
        DecimalSmall(long mult) {
            this.mult = mult;
        }
        
        public long multiplier() { return mult; }
        
        public String label() {
            switch (this) {
                case B: return "";
                default: return name();
            }
        }
    }
    
    // used for showing base10, coarse granularity.
    private enum DecimalLarge implements Magnitude {
        B(1), M(1000000);
        private long mult;
        DecimalLarge(long mult) {
            this.mult = mult;
        }
        
        public long multiplier () { return mult; }
        public String label() {
            switch (this) {
                case B: return "";
                default: return name();
            }
        }
    }
}
