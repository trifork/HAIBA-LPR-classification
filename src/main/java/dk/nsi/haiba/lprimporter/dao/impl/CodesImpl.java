/**
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
 * (http://www.nsi.dk)
 *
 * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dk.nsi.haiba.lprimporter.dao.impl;

import dk.nsi.haiba.lprimporter.dao.Codes;

public class CodesImpl implements Codes {
    private String aCode;
    private String aSecondaryCode;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aCode == null) ? 0 : aCode.hashCode());
        result = prime * result + ((aSecondaryCode == null) ? 0 : aSecondaryCode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CodesImpl other = (CodesImpl) obj;
        if (aCode == null) {
            if (other.aCode != null)
                return false;
        } else if (!aCode.equals(other.aCode))
            return false;
        if (aSecondaryCode == null) {
            if (other.aSecondaryCode != null)
                return false;
        } else if (!aSecondaryCode.equals(other.aSecondaryCode))
            return false;
        return true;
    }

    public CodesImpl(String code, String secondaryCode) {
        aCode = code;
        aSecondaryCode = secondaryCode;
    }

    @Override
    public String getCode() {
        return aCode;
    }

    @Override
    public String getSecondaryCode() {
        return aSecondaryCode;
    }

    @Override
    public String toString() {
        return "CheckStructureImpl [aCode=" + aCode + ", aSecondaryCode=" + aSecondaryCode + "]";
    }
}