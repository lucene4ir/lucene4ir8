package lucene4ir.indexer;

import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.io.BufferedReader;
import java.io.FileReader;

import org.xml.sax.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Created by leif on 26/06/2017.
 */

public class PubMedDocumentIndexer extends DocumentIndexer {

    public DocumentBuilderFactory builderFactory;
    public DocumentBuilder builder;
    public XPath xPath;

    public PubMedDocumentIndexer(String indexPath, String tokenFilterFile, Boolean positional){
        super(indexPath, tokenFilterFile, positional);
        builderFactory = DocumentBuilderFactory.newInstance();
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
        xPath =  XPathFactory.newInstance().newXPath();
    }

    public void indexDocumentsFromFile(String filename){

        String line = "";
        StringBuilder text = new StringBuilder();


        try {
            BufferedReader br = openDocumentFile(filename);
            try {

                line = br.readLine();

                while (line != null){
                    line = line.replaceAll("^\\s+","");
                    if (line.startsWith("<PubmedArticle>")) {
                        text = new StringBuilder();
                    }
                    text.append(line + "\n");

                    if (line.startsWith("</PubmedArticle>")){

                        indexPubMedDocument(text.toString());

                        text.setLength(0);
                    }
                    line = br.readLine();
                }

            } finally {
                br.close();
            }
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    public void indexPubMedDocument(String text){
        try {

        //System.out.println(text);
        org.w3c.dom.Document xmlDocument = builder.parse(new InputSource(new StringReader(text)));
        Document doc = new Document();

        String docid = getStringFromXml(xmlDocument,"/PubmedArticle/MedlineCitation/PMID");

        Field docnumField = new StringField(Lucene4IRConstants.FIELD_DOCNUM, docid, Field.Store.YES);
        doc.add(docnumField);

        String pubyear = getStringFromXml(xmlDocument,"/PubmedArticle/MedlineCitation/DateCreated/Year");
        if (pubyear.isEmpty()) {
            System.out.println(docid + " " + pubyear);
        }
        //Field yearIntField = new IntPoint("year", Integer.parseInt(pubyear));
        //doc.add(yearIntField);
        Field yearField = new StringField(Lucene4IRConstants.FIELD_PUBDATE, pubyear, Field.Store.YES);
        doc.add(yearField);

        String title = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/Article/ArticleTitle");
        addTextFieldToDoc(doc, Lucene4IRConstants.FIELD_TITLE, title);

        String content = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/Article/Abstract");
        addTextFieldToDoc(doc, Lucene4IRConstants.FIELD_BODY, content);

        String journal = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/Article/Journal/Title");
        addTextFieldToDoc(doc, Lucene4IRConstants.FIELD_SOURCE, journal);

        String authors = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/Article/AuthorList");
        addTextFieldToDoc(doc, Lucene4IRConstants.FIELD_AUTHOR, authors);

        addTextFieldToDoc(doc, Lucene4IRConstants.FIELD_RAW, title + " " + authors + " "+ journal + " " + content );

        System.out.println("Indexing: "+ docid);
        addDocumentToIndex(doc);
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }

    }

    public void addTextFieldToDoc(Document doc, String fieldname, String fielddata){
        Field field;
        if (indexPositions) {
            field = new TermVectorEnabledTextField(fieldname, fielddata, Field.Store.YES);
        } else {
            field = new TextField(fieldname, fielddata, Field.Store.YES);
        }
        doc.add(field);
    }

    public String getStringFromXml(org.w3c.dom.Document xmlDocument, String expression){

        String text = "";
        try {
            text = xPath.compile(expression).evaluate(xmlDocument).trim();
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
        return text;

    }



}
