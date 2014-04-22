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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.dao.impl.LPRDAOImpl;
import dk.nsi.haiba.lprimporter.integrationtest.LPRIntegrationTestConfiguration;
import dk.nsi.haiba.lprimporter.status.ImportStatusRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional("haibaTransactionManager")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ImportExecutorTest {
    private static final Logger aLog = Logger.getLogger(ImportExecutorTest.class);

    @Configuration
    @Import({ LPRIntegrationTestConfiguration.class })
    static class TestConfiguration {
        @Bean(name = "minipasLPRDAO")
        public LPRDAO minipasLPRDAO() {
            return Mockito.mock(LPRDAO.class);
        }

        @Bean
        public ImportStatusRepository statusRepo() {
            return Mockito.mock(ImportStatusRepository.class);
        }
    }

    @Autowired
    ImportExecutor executor;

    @Autowired
    @Qualifier(value = "ssiLPRDAO")
    LPRDAO lprDao;

    @Autowired
    HAIBADAO haibaDao;

    @Autowired
    @Qualifier("haibaJdbcTemplate")
    JdbcTemplate haibaJdbc;

    @Autowired
    @Qualifier("lprJdbcTemplate")
    JdbcTemplate lprJdbc;

    @Before
    public void init() {
        Logger.getLogger(ImportExecutor.class).setLevel(Level.DEBUG);
        Logger.getLogger(LPRDAOImpl.class).setLevel(Level.TRACE);
        Logger.getLogger(ImportExecutorTest.class).setLevel(Level.TRACE);
        Logger.getLogger(ClassificationCheckHelper.class).setLevel(Level.TRACE);

        lprJdbc.update("truncate table t_adm");
        lprJdbc.update("truncate table t_koder");
    }

    @Test
    public void testNon3800() {
        // simulate fgr importer
        haibaJdbc
                .update("INSERT INTO klass_shak (Nummer, Navn, Organisationstype, CreatedDate, ValidFrom, ValidTo) VALUES ('1234999', 'PAP Testafdeling', 'test', '2009-01-01', '2009-01-01', '2045-01-01')");
        haibaJdbc
                .update("INSERT INTO klass_shak (Nummer, Organisationstype, Ejerforhold,Institutionsart,Regionskode, CreatedDate, ValidFrom, ValidTo) VALUES ('1234', 'test', 'Ejerforhold2','Institutionsart','Regionskode', '2009-01-01', '2009-01-01', '2045-01-01')");
        // then carecom
        lprJdbc.update("INSERT INTO T_ADM (V_RECNUM, C_SGH, C_AFD, C_PATTYPE, V_CPR, D_INDDTO, D_UDDTO) VALUES (12345, '1234', '999', '1', '1111111111', '2013-01-10', '2013-01-14')");
        assertTrue(haibaJdbc.queryForInt("select count(*) from anvendt_klass_shak") == 0);

        executor.doProcess(true);

        assertTrue(haibaJdbc.queryForInt("select count(*) from anvendt_klass_shak") == 1);
        String ejerforhold = haibaJdbc.queryForObject(
                "select Ejerforhold from anvendt_klass_shak where sygehuskode='1234' AND afdelingskode='999'",
                String.class);
        assertEquals("Ejerforhold2", ejerforhold);
    }
    
    @Test
    public void testNon3800AlreadyThere() {
        // simulate fgr importer
        haibaJdbc
        .update("INSERT INTO klass_shak (Nummer, Navn, Organisationstype, CreatedDate, ValidFrom, ValidTo) VALUES ('1234999', 'PAP Testafdeling', 'test', '2009-01-01', '2009-01-01', '2045-01-01')");
        haibaJdbc
        .update("INSERT INTO klass_shak (Nummer, Organisationstype, Ejerforhold,Institutionsart,Regionskode, CreatedDate, ValidFrom, ValidTo) VALUES ('1234', 'test', 'Ejerforhold2','Institutionsart','Regionskode', '2009-01-01', '2009-01-01', '2045-01-01')");
        haibaJdbc
        .update("INSERT INTO anvendt_klass_shak (Sygehuskode, Afdelingskode) VALUES ('1234', '999')");
        lprJdbc.update("INSERT INTO T_ADM (V_RECNUM, C_SGH, C_AFD, C_PATTYPE, V_CPR, D_INDDTO, D_UDDTO) VALUES (12345, '1234', '999', '1', '1111111111', '2013-01-10', '2013-01-14')");
        assertTrue(haibaJdbc.queryForInt("select count(*) from anvendt_klass_shak") == 1);
        
        executor.doProcess(true);
        
        assertTrue(haibaJdbc.queryForInt("select count(*) from anvendt_klass_shak") == 1);
    }
    
    @Test
    public void testProcedure() {
        lprJdbc.update("INSERT INTO T_KODER (c_kode, c_tilkode, v_type) VALUES ('x', 'y', 'und')");
        assertTrue(haibaJdbc.queryForInt("select count(*) from anvendt_klass_procedurer") == 0);
        
        executor.doProcess(true);
        
        assertTrue(haibaJdbc.queryForInt("select count(*) from anvendt_klass_procedurer") == 1);
    }
    
    @Test
    public void testDiagnose() {
        lprJdbc.update("INSERT INTO T_KODER (c_kode, c_tilkode, v_type) VALUES ('xy', 'y', 'dia')");
        assertTrue(haibaJdbc.queryForInt("select count(*) from anvendt_klass_diagnoser") == 0);
        
        executor.doProcess(true);
        
        assertTrue(haibaJdbc.queryForInt("select count(*) from anvendt_klass_diagnoser") == 1);
    }
    
    @Test
    public void test3800() {
        // simulate fgr importer
        haibaJdbc
        .update("INSERT INTO klass_shak (Nummer, Navn, Organisationstype, CreatedDate, ValidFrom, ValidTo) VALUES ('3800999', 'TST Testafdeling', 'test', '2009-01-01', '2009-01-01', '2045-01-01')");
        haibaJdbc
        .update("INSERT INTO klass_shak (Nummer, Organisationstype, Ejerforhold,Institutionsart,Regionskode, CreatedDate, ValidFrom, ValidTo) VALUES ('3800', 'test', 'Ejerforhold','Institutionsart','Regionskode', '2009-01-01', '2009-01-01', '2045-01-01')");
        // then carecom
        lprJdbc.update("INSERT INTO T_ADM (V_RECNUM, C_SGH, C_AFD, C_PATTYPE, V_CPR, D_INDDTO, D_UDDTO) VALUES (12345, '3800', '999', '1', '1111111111', '2013-01-10', '2013-01-14')");
        assertTrue(haibaJdbc.queryForInt("select count(*) from anvendt_klass_shak") == 0);
        
        executor.doProcess(true);
        
        assertTrue(haibaJdbc.queryForInt("select count(*) from anvendt_klass_shak") == 1);
//        String ejerforhold = haibaJdbc.queryForObject(
//                "select Ejerforhold from anvendt_klass_shak where sygehuskode='3800TST' AND afdelingskode='999'",
//                String.class);
        haibaJdbc.query("select * from anvendt_klass_shak", new RowMapper<String>(){
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                System.out.println(rs.getObject("sygehuskode"));
                System.out.println(rs.getObject("afdelingskode"));
                System.out.println(rs.getObject("Ejerforhold"));
                return "e";
            }});
//        assertEquals("Ejerforhold", ejerforhold);
    }

    @Test
    public void testTwo3800() {
        // simulate fgr importer
        haibaJdbc
                .update("INSERT INTO klass_shak (Nummer, Navn, Organisationstype, CreatedDate, ValidFrom, ValidTo) VALUES ('3800999', 'TST Testafdeling', 'test', '2009-01-01', '2009-01-01', '2010-01-01')");
        haibaJdbc
                .update("INSERT INTO klass_shak (Nummer, Navn, Organisationstype, CreatedDate, ValidFrom, ValidTo) VALUES ('3800999', 'HAK Testafdeling', 'test', '2009-01-01', '2010-01-01', '2045-01-01')");
        haibaJdbc
                .update("INSERT INTO klass_shak (Nummer, Organisationstype, Ejerforhold,Institutionsart,Regionskode, CreatedDate, ValidFrom, ValidTo) VALUES ('3800', 'test', 'Ejerforhold','Institutionsart','Regionskode', '2009-01-01', '2009-01-01', '2045-01-01')");
        // then carecom
        lprJdbc.update("INSERT INTO T_ADM (V_RECNUM, C_SGH, C_AFD, C_PATTYPE, V_CPR, D_INDDTO, D_UDDTO) VALUES (12345, '3800', '999', '1', '1111111111', '2013-01-10', '2013-01-14')");
        lprJdbc.update("INSERT INTO T_ADM (V_RECNUM, C_SGH, C_AFD, C_PATTYPE, V_CPR, D_INDDTO, D_UDDTO) VALUES (12345, '3800', '999', '1', '1111111111', '2009-01-10', '2009-01-14')");
        assertTrue(haibaJdbc.queryForInt("select count(*) from anvendt_klass_shak") == 0);

        executor.doProcess(true);

        List<String> sygehuskoder = haibaJdbc.query("select sygehuskode from anvendt_klass_shak", new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("sygehuskode");
            }
        });
        assertTrue(sygehuskoder.contains("3800TST"));
        assertTrue(sygehuskoder.contains("3800HAK"));

        assertTrue(haibaJdbc.queryForInt("select count(*) from anvendt_klass_shak") == 2);
        String ejerforhold = haibaJdbc.queryForObject(
                "select Ejerforhold from anvendt_klass_shak where sygehuskode='3800TST' AND afdelingskode='999'",
                String.class);
        assertEquals("Ejerforhold", ejerforhold);
    }
}
