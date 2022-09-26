package com.geocat.gnclient.util;


import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class HelperUtility {
    public static final String FILE_SYSTEM_SPECIAL_CHARS = "*<>|/:;?\"";
    public static final Character UNDERSCORE  = '_';

    /**
     * Joins a path to url dealing with / between both.
     *
     * @param url
     * @param path
     * @return
     */
    public static String addPathToUrl(String url, String path) {
        if(!url.endsWith("/")){
            url = url + "/";
        }

        if(path.startsWith("/")){
            path = path.substring(1);
        }

        return url + path;
    }


    public static String removeFileSystemSpecialChars(String pValue) {
        return getReplacedString(pValue, FILE_SYSTEM_SPECIAL_CHARS, UNDERSCORE);
    }


    public static String getReplacedString(String pValue, String pCharactersString, char pReplaceChar) {
        if(StringUtils.isNotEmpty(pValue) && (pCharactersString != null)) {
            char[] charsArray = pCharactersString.toCharArray();
            int length = charsArray.length;
            if(length > 0) {
                for(int index = 0; index < length; index++) {
                    pValue = pValue.replace(charsArray[index], pReplaceChar);
                }
            }
        }
        return pValue;
    }


    public static boolean isUUID(String uuidVal) {
        try {
            UUID.fromString(uuidVal);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String applyXslt(String xml, String importXslt) throws Exception {
        // Apply xslt to metadata
        if (StringUtils.isNotEmpty(importXslt) &&
            Files.exists( Paths.get(importXslt))) {

            //  read the XML to a JDOM2 document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder dombuilder = factory.newDocumentBuilder();

            InputSource inputSource = new InputSource( new StringReader( xml ) );

            org.w3c.dom.Document w3cDocument = dombuilder.parse(inputSource);
            DOMBuilder jdomBuilder = new DOMBuilder();
            Document jdomDocument = jdomBuilder.build(w3cDocument);

            // create the JDOMSource from JDOM2 document
            JDOMSource source = new JDOMSource(jdomDocument);

            // create the transformer
            Transformer transformer = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)
                .newTransformer(new StreamSource(importXslt));

            // create the JDOMResult object
            JDOMResult out = new JDOMResult();

            // perform the transformation
            transformer.transform(source, out);

            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            return outputter.outputString(out.getDocument());

        } else {
            return xml;
        }
    }
}
