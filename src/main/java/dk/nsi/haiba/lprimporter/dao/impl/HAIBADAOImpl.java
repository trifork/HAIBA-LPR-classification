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
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import dk.nsi.haiba.lprimporter.dao.CommonDAO;
import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.log.Log;
import dk.nsi.haiba.lprimporter.model.haiba.ShakRegionValues;

public class HAIBADAOImpl extends CommonDAO implements HAIBADAO {
    private static Log log = new Log(Logger.getLogger(HAIBADAOImpl.class));

    @Autowired
    @Qualifier("haibaJdbcTemplate")
    JdbcTemplate jdbc;

    @Value("${jdbc.haibatableprefix:}")
    String tableprefix;

    @Value("${jdbc.fgrtableprefix:fgr.}")
    String fgrtableprefix;

    @Override
    public String getSygehusInitials(String sygehuscode, String afdelingsCode, Date in) throws DAOException {
        String sql = null;
        if (MYSQL.equals(getDialect())) {
            sql = "SELECT Navn FROM klass_shak WHERE Nummer=? AND ValidFrom <= ? AND ValidTo >= ?";
        } else {
            // MSSQL
            sql = "SELECT Navn FROM " + fgrtableprefix + "klass_shak WHERE Nummer=? AND ValidFrom <= ? AND ValidTo >= ?";
        }

        try {
            String name = jdbc.queryForObject(sql, new Object[] { sygehuscode + afdelingsCode, in, in }, String.class);
            if (name != null && name.length() > 3) {
                return name.substring(0, 3);
            } else {
                return name;
            }
        } catch (EmptyResultDataAccessException e) {
            // no name found
            log.warn("No SygehusInitials found for Code:" + sygehuscode + ", department:" + afdelingsCode + " and date:" + in);
            return "";
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching initials for hospital from FGR", e);
        }
    }

    @Override
    public Collection<ShakRegionValues> getShakRegionValuesForSygehusNumre(Collection<String> sygehusNumre) {
        List<ShakRegionValues> returnValue = new ArrayList<ShakRegionValues>();
        for (String nummer : sygehusNumre) {
            // 3800-sygehuse has an extra sygehus extension that doesn't exist in the shak table
            String truncatedSygehusNummer = nummer.length() > 4 ? nummer.substring(0, 4) : nummer;
            RowMapper<ShakRegionValues> rowMapper = new RowMapper<ShakRegionValues>() {
                @Override
                public ShakRegionValues mapRow(ResultSet rs, int rowNum) throws SQLException {
                    ShakRegionValues returnValue = new ShakRegionValues();
                    returnValue.setEjerForhold(rs.getString("Ejerforhold"));
                    returnValue.setInstitutionsArt(rs.getString("Institutionsart"));
                    returnValue.setRegionsKode(rs.getString("Regionskode"));
                    return returnValue;
                }
            };
            try {
                ShakRegionValues shakRegionValues = jdbc.queryForObject(
                        "SELECT DISTINCT Ejerforhold,Institutionsart,Regionskode FROM " + fgrtableprefix + "klass_shak WHERE nummer = ?",
                        rowMapper, truncatedSygehusNummer);
                // but keep the original nummer here
                shakRegionValues.setNummer(nummer);
                returnValue.add(shakRegionValues);
            } catch (RuntimeException e) {
                log.error("Error fetching shakregion values from sygehus nummer " + nummer, e);
            }
        }
        return returnValue;
    }
}
