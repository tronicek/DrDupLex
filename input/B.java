package p6;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import nicad.Diff;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class B {

    public void testMarshal() throws JiBXException, SAXException, IOException {
        SortBy o = SortByFactory.create();
        IBindingFactory bfact = BindingDirectory.getFactory(SortBy.class);
        IMarshallingContext marshallingContext = bfact.createMarshallingContext();
        Writer outConsole = new BufferedWriter(new OutputStreamWriter(System.out));
        marshallingContext.setOutput(outConsole);
        marshallingContext.setIndent(3);
        marshallingContext.marshalDocument(o, "UTF-8", null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer out = new BufferedWriter(new OutputStreamWriter(outputStream));
        marshallingContext.setIndent(3);
        marshallingContext.setOutput(out);
        marshallingContext.marshalDocument(o, "UTF-8", null);
        InputSource marshallingResult = new InputSource(new ByteArrayInputStream(outputStream.toByteArray()));
        FileInputStream fis = new FileInputStream(new File("src/test/resources/filter/SortByTestData.xml"));
        InputSource expectedResult = new InputSource(fis);
        DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        Diff diff = new Diff(expectedResult, marshallingResult);
        diff.overrideDifferenceListener(differenceListener);
        assertTrue("Marshalled SortBy matches expected XML " + diff, diff.similar());
    }
}
