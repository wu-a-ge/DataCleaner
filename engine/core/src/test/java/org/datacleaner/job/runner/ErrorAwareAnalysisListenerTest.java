/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.job.runner;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.sql.SQLException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.test.TestHelper;
import org.junit.Ignore;

import junit.framework.TestCase;

// Ignored because this test is dependent on log setup that is not deterministic or self-contained in the test.
// The test _should_ work when run in isolation, but not necesarily as part of the build.
@Ignore
public class ErrorAwareAnalysisListenerTest extends TestCase {

    private ByteArrayOutputStream baos;
    private PrintStream oldOut;
    private PrintStream newOut;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // the logging config of this project logs to system out. to get those
        // log messages, we replace System.out
        baos = new ByteArrayOutputStream();
        oldOut = System.out;
        newOut = new PrintStream(baos);
        System.setOut(newOut);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setOut(oldOut);
    }

    public void testHandleErrorWithSqlNextException() throws Exception {
        final ErrorAwareAnalysisListener listener = new ErrorAwareAnalysisListener();
        final SQLException sqlException = new SQLException("foo", new IllegalStateException("bar"));
        sqlException.setNextException(new SQLException("baz"));

        listener.handleError(null, new MetaModelException(sqlException));

        final String string = FileHelper.readInputStreamAsString(new ByteArrayInputStream(baos.toByteArray()), "UTF8");
        TestHelper.assertStringContains(string, "org.apache.metamodel.MetaModelException: java.sql.SQLException: foo");
        TestHelper.assertStringContains(string,
                "ErrorAwareAnalysisListener - SQLException.getNextException() stack trace:");
        TestHelper.assertStringContains(string, "java.sql.SQLException: baz");

    }
}
