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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import dk.nsi.haiba.lprimporter.dao.ClassificationCheckDAO;
import dk.nsi.haiba.lprimporter.dao.ClassificationCheckDAO.CheckStructure;
import dk.nsi.haiba.lprimporter.dao.Codes;
import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.email.EmailSender;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.model.haiba.ShakRegionValues;

public class ClassificationCheckHelper {
    private static Log log = new Log(Logger.getLogger(ClassificationCheckHelper.class));

    @Autowired
    ClassificationCheckDAO classificationCheckDAO;

    @Autowired
    HAIBADAO haibaDAO;

    @Autowired
    @Qualifier(value = "compositeLPRDAO")
    LPRDAO lprDAO;

    @Autowired
    EmailSender emailSender;

    public void check(Collection<Codes> sygehusKoder, Collection<Codes> diagnoseKoder, Collection<Codes> procedureKoder) {
        log.debug("sygehusKoder=" + sygehusKoder + ", diagnoseKoder=" + diagnoseKoder + ", procedureKoder="
                + procedureKoder);
        Set<CheckStructure> newSygehusClassifications = new HashSet<ClassificationCheckDAO.CheckStructure>();
        Set<CheckStructure> newDiagnoseCheckClassifications = new HashSet<ClassificationCheckDAO.CheckStructure>();
        Set<CheckStructure> newProcedureCheckClassifications = new HashSet<ClassificationCheckDAO.CheckStructure>();

        for (Codes ncs : sygehusKoder) {
            CheckStructureImpl csi = new CheckStructureImpl(ncs.getCode(), ncs.getSecondaryCode(), "sygehuskode",
                    "afdelingskode", "anvendt_klass_shak");
            newSygehusClassifications.add(csi);
        }
        for (Codes ncs : diagnoseKoder) {
            CheckStructureImpl csi = new CheckStructureImpl(ncs.getCode(), ncs.getSecondaryCode(), "Diagnoseskode",
                    "tillaegskode", "Anvendt_klass_diagnoser");
            newDiagnoseCheckClassifications.add(csi);
        }
        for (Codes ncs : procedureKoder) {
            CheckStructureImpl csi = new CheckStructureImpl(ncs.getCode(), ncs.getSecondaryCode(), "procedurekode",
                    "tillaegskode", "Anvendt_klass_procedurer");
            newProcedureCheckClassifications.add(csi);
        }

        if (!newSygehusClassifications.isEmpty() || !newProcedureCheckClassifications.isEmpty()
                || !newDiagnoseCheckClassifications.isEmpty()) {
            log.debug("send email about new sygehuse=" + newSygehusClassifications.size() + " or new procedure="
                    + newProcedureCheckClassifications.size() + " or new diagnose="
                    + newDiagnoseCheckClassifications.size());
            emailSender.send(newSygehusClassifications, newProcedureCheckClassifications,
                    newDiagnoseCheckClassifications);
            // now the email is away. if we should die now, worst case is that this information would be send again on
            // the next run. better this than storing, then die and never make the notification
            classificationCheckDAO.storeClassifications(newSygehusClassifications);
            classificationCheckDAO.storeClassifications(newProcedureCheckClassifications);
            classificationCheckDAO.storeClassifications(newDiagnoseCheckClassifications);

            // addon, enrich shak with shakregion values
            Collection<String> sygehusNumre = getSygehusNumre(newSygehusClassifications);
            Collection<ShakRegionValues> shakRegionValuesForSygehusNumre = haibaDAO
                    .getShakRegionValuesForSygehusNumre(sygehusNumre);
            log.debug("updating " + shakRegionValuesForSygehusNumre.size() + " shakregions for " + sygehusNumre.size()
                    + " sygehusnumre");
            classificationCheckDAO.storeShakRegionValues(shakRegionValuesForSygehusNumre);
        }
    }

    private Collection<String> getSygehusNumre(Collection<CheckStructure> newSygehusClassifications) {
        Collection<String> returnValue = new HashSet<String>();
        for (CheckStructure checkStructure : newSygehusClassifications) {
            returnValue.add(checkStructure.getCode());
        }
        return returnValue;
    }

    public static class CheckStructureImpl implements CheckStructure {
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
            CheckStructureImpl other = (CheckStructureImpl) obj;
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

        String aCode;
        String aSecondaryCode;
        private String aCodeClasificationColumnName;
        private String aSecondaryCodeClasificationColumnName;
        private String aClassificationTableName;

        public CheckStructureImpl(String code, String secondaryCode, String codeClasificationColumnName,
                String secondaryCodeClasificationColumnName, String classificationTableName) {
            aCode = code;
            aSecondaryCode = secondaryCode;
            aCodeClasificationColumnName = codeClasificationColumnName;
            aSecondaryCodeClasificationColumnName = secondaryCodeClasificationColumnName;
            aClassificationTableName = classificationTableName;
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
        public String getCodeClassificationColumnName() {
            return aCodeClasificationColumnName;
        }

        @Override
        public String getSecondaryCodeClasificationColumnName() {
            return aSecondaryCodeClasificationColumnName;
        }

        @Override
        public String getClassificationTableName() {
            return aClassificationTableName;
        }

        @Override
        public String toString() {
            return "CheckStructureImpl [aCode=" + aCode + ", aSecondaryCode=" + aSecondaryCode
                    + ", aCodeClasificationColumnName=" + aCodeClasificationColumnName
                    + ", aSecondaryCodeClasificationColumnName=" + aSecondaryCodeClasificationColumnName
                    + ", aClassificationTableName=" + aClassificationTableName + "]";
        }
    }

    public Collection<Codes> getSygehusKoder() {
        return lprDAO.getSygehusKoder();
    }

    public Collection<Codes> getRegisteredSygehusKoder() {
        return classificationCheckDAO.getRegisteredSygehusKoder();
    }

    public Collection<Codes> getDiagnoseKoder() {
        return lprDAO.getDiagnoseKoder();
    }

    public Collection<Codes> getRegisteredDiagnoseKoder() {
        return classificationCheckDAO.getRegisteredDiagnoseKoder();
    }

    public Collection<Codes> getProcedureKoder() {
        return lprDAO.getProcedureKoder();
    }

    public Collection<Codes> getRegisteredProcedureKoder() {
        return classificationCheckDAO.getRegisteredProcedureKoder();
    }
}
