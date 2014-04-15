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

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
import dk.nsi.haiba.lprimporter.dao.LPRDAO;
import dk.nsi.haiba.lprimporter.integrationtest.LPRIntegrationTestConfiguration;
import dk.nsi.haiba.lprimporter.status.ImportStatusRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional("haibaTransactionManager")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ImportExecutorTest {

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
    JdbcTemplate jdbc;

    @Test
    public void test3800() {
        jdbc.update("insert into klass_shak (Nummer, Navn, Organisationstype, CreatedDate, ValidFrom, ValidTo) values ('3800999', 'TST Testafdeling', 'test', '2009-01-01', '2009-01-01', '2045-01-01')");
        String sygehusInitials = haibaDao.getSygehusInitials("3800", "999", new Date());
        assertEquals("TST", sygehusInitials);
    }

    @Test
    public void testExecutor() {
        executor.doProcess(true);
        assertTrue(true);
    }
}
