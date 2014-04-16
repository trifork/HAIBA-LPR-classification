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
package dk.nsi.haiba.lprimporter.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import dk.nsi.haiba.lprimporter.dao.Codes;
import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.dao.impl.CodesImpl;
import dk.nsi.haiba.lprimporter.email.EmailSender;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.status.ImportStatusRepository;

/*
 * Scheduled job, responsible for fetching new data from LPR, then send it to the RulesEngine for further processing
 */
public class ImportExecutor {
    private static Log log = new Log(Logger.getLogger(ImportExecutor.class));

    private boolean manualOverride;

    LPRDAO lprdao;

    @Autowired
    HAIBADAO haibaDao;

    @Autowired
    ImportStatusRepository statusRepo;

    @Autowired
    EmailSender emailSender;

    @Autowired
    ClassificationCheckHelper classificationCheckHelper;

    public ImportExecutor(LPRDAO lprdao) {
        this.lprdao = lprdao;
    }

    @Scheduled(cron = "${cron.import.job}")
    public void run() {
        if (!isManualOverride()) {
            log.debug("Running Importer: " + new Date().toString());
            doProcess(false);
        } else {
            log.debug("Importer must be started manually");
        }
    }

    /*
     * Separated into its own method for testing purpose, because testing a scheduled method isn't good
     */
    public void doProcess(boolean manual) {
        log.info("Started processing, manual=" + isManualOverride());
        // Fetch new records from LPR contact table
        try {
            statusRepo.importStartedAt(new DateTime());
            if (manual) {
                emailSender.sendHello();
            }

            Collection<Codes> allUsedSygehusKoder = classificationCheckHelper.getSygehusKoder();
            Collection<Codes> allRegisteredSygehusKoder = classificationCheckHelper.getRegisteredSygehusKoder();
            // test is generally done with 'startswith' as the registered codes may have an extension with sygehus
            // initials and the allused don't
            Collection<Codes> newSygehusKoder = testNewKoder(allUsedSygehusKoder, allRegisteredSygehusKoder);
            Collection<Codes> newDiagnoseKoder = new ArrayList<Codes>();
            Collection<Codes> newProcedureKoder = new ArrayList<Codes>();

            // for sygehuskoder, find the initials
            addSygehusInitials(newSygehusKoder);

            classificationCheckHelper.check(newSygehusKoder, newDiagnoseKoder, newProcedureKoder);

            statusRepo.importEndedWithSuccess(new DateTime());
            if (manual) {
                emailSender.sendDone(null);
            }
        } catch (Exception e) {
            log.error("", e);
            statusRepo.importEndedWithFailure(new DateTime(), e.getClass().getName());
            if (manual) {
                emailSender.sendDone(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    private void addSygehusInitials(Collection<Codes> newSygehusKoder) {
        Collection<Codes> temp = new ArrayList<Codes>();
        for (Codes codes : newSygehusKoder) {
            // these are new, so find out where they are used (find the date they are used to determine exact initials)
            String code = codes.getCode();
            if (code.startsWith("3800")) {
                temp.add(codes);
            }
        }
        newSygehusKoder.removeAll(temp);

        for (Codes codes : temp) {
            String code = codes.getCode();
            String secondaryCode = codes.getSecondaryCode();
            Collection<Date> inDates = lprdao.getInDatesForSygehusKoder(code, secondaryCode);
            // put the code back, now with initials (multiple initials are possible)
            for (Date in : inDates) {
                String sygehusInitials = haibaDao.getSygehusInitials(code, secondaryCode, in);
                log.debug("addSygehusInitials: added '" + sygehusInitials + "' to " + code + "/" + secondaryCode);
                newSygehusKoder.add(new CodesImpl(code + sygehusInitials, secondaryCode));
            }
        }
    }

    private Collection<Codes> testNewKoder(Collection<Codes> allUsed, Collection<Codes> allRegistered) {
        Collection<Codes> returnValue = new ArrayList<Codes>(allUsed);
        for (Codes used : allUsed) {
            for (Codes registered : allRegistered) {
                if (registered.getCode().startsWith(used.getCode())
                        && equals(registered.getSecondaryCode(), used.getSecondaryCode())) {
                    returnValue.remove(used);
                }
            }
        }
        log.debug("testNewKoder: returnValue=" + returnValue);
        return returnValue;
    }

    private boolean equals(String s1, String s2) {
        boolean returnValue = false;
        if (s1 == s2) {
            returnValue = true;
        } else if (s1 != null && s2 != null) {
            returnValue = s1.equals(s2);
        }
        return returnValue;
    }

    public boolean isManualOverride() {
        return manualOverride;
    }

    public void setManualOverride(boolean manualOverride) {
        this.manualOverride = manualOverride;
    }
}
