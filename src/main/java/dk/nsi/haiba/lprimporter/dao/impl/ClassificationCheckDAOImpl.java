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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import dk.nsi.haiba.lprimporter.dao.ClassificationCheckDAO;
import dk.nsi.haiba.lprimporter.dao.Codes;
import dk.nsi.haiba.lprimporter.dao.CommonDAO;
import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.model.haiba.ShakRegionValues;

public class ClassificationCheckDAOImpl extends CommonDAO implements ClassificationCheckDAO {
    private static Log log = new Log(Logger.getLogger(ClassificationCheckDAOImpl.class));
    private JdbcTemplate aClassificationJdbc;

    @Value("${jdbc.classificationtableprefix:}")
    String tableprefix;

    public ClassificationCheckDAOImpl(JdbcTemplate classificationJdbc) {
        aClassificationJdbc = classificationJdbc;
    }

    @Override
    public void storeClassifications(Collection<CheckStructure> checkStructures) {
        for (CheckStructure unknownStructure : checkStructures) {
            String sql = "INSERT INTO " + tableprefix + unknownStructure.getClassificationTableName() + "("
                    + unknownStructure.getCodeClassificationColumnName() + ","
                    + unknownStructure.getSecondaryCodeClasificationColumnName() + ") VALUES (?,?)";
            log.debug("checkClassifications: insert sql=" + sql);
            aClassificationJdbc.update(sql, unknownStructure.getCode(), unknownStructure.getSecondaryCode());
        }
    }

    @Override
    public void storeShakRegionValues(Collection<ShakRegionValues> shakRegionValuesForSygehusNumre) {
        for (ShakRegionValues srv : shakRegionValuesForSygehusNumre) {
            log.debug("updating shakregion for " + srv.getNummer());
            String sql = "UPDATE " + tableprefix
                    + "anvendt_klass_shak SET Ejerforhold=?, Institutionsart=?, Regionskode=? WHERE sygehuskode=?";
            aClassificationJdbc.update(sql, srv.getEjerForhold(), srv.getInstitutionsArt(), srv.getRegionsKode(),
                    srv.getNummer());
        }
    }

    @Override
    public Collection<Codes> getRegisteredSygehusKoder() {
        String sql = "SELECT DISTINCT sygehuskode,afdelingskode FROM " + tableprefix + "anvendt_klass_shak";
        return getKoder(sql, "sygehuskode", "afdelingskode");
    }

    private Collection<Codes> getKoder(String sql, final String f1, final String f2) {
        Collection<Codes> returnValue = new ArrayList<Codes>();
        try {
            returnValue = aClassificationJdbc.query(sql, new RowMapper<Codes>() {
                @Override
                public Codes mapRow(ResultSet rs, int rowNum) throws SQLException {
                    String code = rs.getString(f1);
                    String secondaryCode = rs.getString(f2);
                    return new CodesImpl(code, secondaryCode);
                }
            });
        } catch (EmptyResultDataAccessException e) {
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching for sql " + sql, e);
        }
        return returnValue;
    }

    @Override
    public Collection<Codes> getRegisteredDiagnoseKoder() {
        String sql = "SELECT DISTINCT Diagnoseskode,tillaegskode FROM " + tableprefix + "anvendt_klass_diagnoser";
        return getKoder(sql, "Diagnoseskode", "tillaegskode");
    }

    @Override
    public Collection<Codes> getRegisteredProcedureKoder() {
        String sql = "SELECT DISTINCT procedurekode,tillaegskode FROM " + tableprefix + "anvendt_klass_procedurer";
        return getKoder(sql, "procedurekode", "tillaegskode");
    }
}
