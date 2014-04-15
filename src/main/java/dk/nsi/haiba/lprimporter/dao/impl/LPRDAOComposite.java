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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import dk.nsi.haiba.lprimporter.dao.Codes;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.log.Log;

public class LPRDAOComposite implements LPRDAO {
    private static Log log = new Log(Logger.getLogger(LPRDAOComposite.class));
    public static final int SSI_DB = 1;
    public static final int MINIPAS_DB = 2;

    @Autowired
    @Qualifier(value = "ssiLPRDAO")
    private LPRDAO ssiLPRDAO;

    @Autowired
    @Qualifier(value = "minipasLPRDAO")
    private LPRDAO minipasLPRDAO;

    public static String getDbIdText(int dbId) {
        String returnValue = "NA";
        if (dbId == SSI_DB) {
            returnValue = "LPR";
        } else if (dbId == MINIPAS_DB) {
            returnValue = "MINIPAS";
        }
        return returnValue;
    }

    @Override
    public Collection<Codes> nyGetSygehusKoder() {
        Set<Codes> returnValue = new HashSet<Codes>();
        returnValue.addAll(minipasLPRDAO.nyGetSygehusKoder());
        returnValue.addAll(ssiLPRDAO.nyGetSygehusKoder());
        return new ArrayList<Codes>(returnValue);
    }

    @Override
    public Collection<Codes> nyGetDiagnoseKoder() {
        Set<Codes> returnValue = new HashSet<Codes>();
        returnValue.addAll(minipasLPRDAO.nyGetDiagnoseKoder());
        returnValue.addAll(ssiLPRDAO.nyGetDiagnoseKoder());
        return new ArrayList<Codes>(returnValue);
    }

    @Override
    public Collection<Codes> nyGetProcedureKoder() {
        Set<Codes> returnValue = new HashSet<Codes>();
        returnValue.addAll(minipasLPRDAO.nyGetProcedureKoder());
        returnValue.addAll(ssiLPRDAO.nyGetProcedureKoder());
        return new ArrayList<Codes>(returnValue);
    }

    @Override
    public Collection<Date> getInDatesForSygehusKoder(String code, String secondaryCode) {
        Set<Date> returnValue = new HashSet<Date>();
        returnValue.addAll(minipasLPRDAO.getInDatesForSygehusKoder(code, secondaryCode));
        returnValue.addAll(ssiLPRDAO.getInDatesForSygehusKoder(code, secondaryCode));
        return new ArrayList<Date>(returnValue);
    }
}
