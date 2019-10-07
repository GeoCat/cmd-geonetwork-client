package com.geocat.gnclient.gnservices.metadata;

import com.geocat.gnclient.exporter.Exporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ExportMetadataProcessor implements IMetadataProcessor {
    private static final Logger logger = LogManager.getLogger(Exporter.class.getName());

    private Path outputPath;

    public ExportMetadataProcessor(Path outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void process(Element metadata) {
        // Extract UUID
        XPathFactory xpfac = XPathFactory.instance();
        XPathExpression<Element> xp  =xpfac.compile("gmd:fileIdentifier",
            Filters.element(),
            null,
            Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd"));

        List<Element> uuids = xp.evaluate(metadata);
        String uuid = ((Element) uuids.get(0)).getValue().trim();

        XMLOutputter outp = new XMLOutputter();
        String text = outp.outputString(metadata);


        String fileName = uuid + ".xml";

        try {
            Files.write( outputPath.resolve(fileName), text.getBytes(Charset.forName("UTF8")), StandardOpenOption.CREATE);

        } catch (IOException ex) {
            logger.error("ExportMetadataProcessor", ex);
        }
    }
}
