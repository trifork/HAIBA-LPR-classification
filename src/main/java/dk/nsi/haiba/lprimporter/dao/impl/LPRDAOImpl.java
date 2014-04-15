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

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import dk.nsi.haiba.lprimporter.dao.Codes;
import dk.nsi.haiba.lprimporter.dao.CommonDAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.exception.DAOException;
import dk.nsi.haiba.lprimporter.log.Log;

public class LPRDAOImpl extends CommonDAO implements LPRDAO {
    private static Log log = new Log(Logger.getLogger(LPRDAOImpl.class));

    private JdbcTemplate jdbcTemplate;
    private String hr_tableprefix;

    public LPRDAOImpl(DataSource ds, String haibareplicaPrefix) {
        jdbcTemplate = new JdbcTemplate(ds);
        hr_tableprefix = haibareplicaPrefix;
    }

    @Override
    public Collection<Codes> nyGetSygehusKoder() {
        String sql = "SELECT DISTINCT c_sgh,c_afd FROM " + hr_tableprefix + "T_ADM";
        return getKoder(sql, "c_sgh", "c_afd");
    }

    @Override
    public Collection<Codes> nyGetProcedureKoder() {
        String sql = "SELECT DISTINCT c_kode,c_tilkode FROM " + hr_tableprefix + "T_KODER WHERE v_type!='dia'";
        return getKoder(sql, "c_kode", "c_tilkode");
    }

    @Override
    public Collection<Codes> nyGetDiagnoseKoder() {
        String sql = "SELECT DISTINCT c_kode,c_tilkode FROM " + hr_tableprefix + "T_KODER WHERE v_type='dia'";
        return getKoder(sql, "c_kode", "c_tilkode");
    }

    private Collection<Codes> getKoder(String sql, String f1, String f2) {
        Collection<Codes> returnValue = new ArrayList<Codes>();
        try {
            returnValue = jdbcTemplate.query(sql, new RowMapper<Codes>() {
                @Override
                public Codes mapRow(ResultSet rs, int rowNum) throws SQLException {
                    String code = rs.getString("c_kode");
                    String secondaryCode = rs.getString("c_tilkode");
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
    public Collection<Date> getInDatesForSygehusKoder(String code, String secondaryCode) {
        Collection<Date> returnValue = null;
        String sql = "SELECT DISTINCT D_INDDTO FROM " + hr_tableprefix + "T_ADM WHERE C_SGH=? AND C_AFD=?";
        try {
            returnValue = jdbcTemplate.queryForList(sql, Date.class, code, secondaryCode);
        } catch (EmptyResultDataAccessException e) {
        } catch (RuntimeException e) {
            throw new DAOException("Error fetching for sql " + sql, e);
        }
        return returnValue;
    }
}
