package sop.testsuite.external;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("External SOP Tests")
@SelectPackages("sop.testsuite.operation")
@IncludeClassNamePatterns(".*Test")
public class ExternalTestSuite {

}
