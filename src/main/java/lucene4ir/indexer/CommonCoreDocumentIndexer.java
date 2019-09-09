/*
    This Class is Used to index CommonCore 2017 data files
   TREC - CommonCore 2017 Files
    URL : https://trec.nist.gov/data/core2017.html

   * Created by Abdulaziz on 16/03/2019.
   * Edited by Abdulaziz AlQattan on 09/09/2019.
*/

package lucene4ir.indexer;
import com.sun.deploy.security.SelectableSecurityManager;
import lucene4ir.Lucene4IRConstants;
import lucene4ir.utils.TokenAnalyzerMaker;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.safety.Whitelist;

import java.io.BufferedReader;
import java.util.ArrayList;
public class CommonCoreDocumentIndexer extends DocumentIndexer {

    // Properties

    Whitelist whiteList;
    private org.jsoup.nodes.Document jdoc;
    ArrayList<Field> fields;

    // Sub- Private Functions
    private void initWhiteList()
    {
        // Add White List Of The Document
        // The White List Are The Tags to keep in the Document after Reading From Jsoup
        try {
            // Add The popular tags in the white list
            whiteList = Whitelist.relaxed();

            // Add each allowed tag with its allowed attributes
            whiteList.addTags("title");

            // *******************

            whiteList.addTags("doc-id");
            whiteList.addAttributes("doc-id","id-string");

            // *******************

            whiteList.addTags("doc.copyright");
            whiteList.addAttributes("doc.copyright","year");
            whiteList.addAttributes("doc.copyright","holder");

            // *******************

            whiteList.addTags("classifier");

            // *******************

            whiteList.addTags("pubdata");
            whiteList.addAttributes("pubdata","name");
            whiteList.addAttributes("pubdata","date.publication");

            whiteList.addTags("hl1");

        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    } // End Function
    private String getFieldAttribute(String TagName  , String AttributeName )
    {
        /*
        This Function is used to get the attribure value of a specific tag
        in a specific jsoup Document
         */
        return  jdoc.getElementsByTag(TagName).attr(AttributeName);
    } // End Function
    private String getFieldText(String TagName){
           /*
           This function is used to main gathered text from  all elements with a specific tag
           in the input jsoup document
            */
        String fieldText = "";
        org.jsoup.select.Elements dns = jdoc.getElementsByTag(TagName);
        if (dns.size() > 0)
            fieldText = dns.text();

        return fieldText;
    } // End Function

    private String getPubDate()
    {
        /*
        This function is Used to get the Publication Date :
         */
        String result = "" ;
        result = getFieldAttribute( "pubdata" , "date.publication");
        if (!result.isEmpty())
            result = result.substring(0,4) + "/" + result.substring(4,6) + "/" + result.substring(6,8);
        return result;
    } // End Function

    private void addFieldToDocument(String fldName , char fldType)
    {
         /*
        This Function is Used to Create a Field based on the input FieldName and FieldType
            if The Field Type = "s" then String Field
            Else ( TextField or TermVectorTextField According to the current indexPositions
            Then add the resultant field to the Document
         */
        Field aField;
        if (fldType == 's')
            aField = new StringField(fldName, "", Field.Store.YES);
        else if (indexPositions)
            aField = new lucene4ir.indexer.TermVectorEnabledTextField(fldName, "", Field.Store.YES);
        else
            aField = new TextField(fldName, "", Field.Store.YES);
        fields.add(aField);
    } // End Function
    private void initFields()
    {
        /*
        Initialize Fields Collection in the Following Sequence :
        1= Document Number - String Field
        2- Publish Date - String Field
        3- Title - Text Field
        4- Content - Text Field
        5- All - Text Field
 */
        fields = new ArrayList<Field>();
        addFieldToDocument(Lucene4IRConstants.FIELD_DOCNUM,'s');
        addFieldToDocument(Lucene4IRConstants.FIELD_PUBDATE,'s');
        addFieldToDocument(Lucene4IRConstants.FIELD_TITLE,' ');
        addFieldToDocument(Lucene4IRConstants.FIELD_BODY,' ');
        addFieldToDocument(Lucene4IRConstants.FIELD_RAW,' ');
    } // End Function

    private Field getBigramField(String all)
    {
        String tokenFilterFile = "params/index/TokenFilterFile_Bigram.xml";
        String fldName = "biraw";
        Field result;
        TokenStream ts;
        TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
        Analyzer an = tam.createAnalyzer(tokenFilterFile);
        ts= an.tokenStream(fldName,all);

        if(indexPositions)
             result = new lucene4ir.indexer.TermVectorEnabledTextField(fldName,ts);
        else
            result = new TextField(fldName,ts);
        return result;
    }

    // Constructor Method
    public CommonCoreDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional){
        super(indexPath, tokenFilterFile, positional);
        // Create String Corpus Fields and add them to the Document doc

        initFields();
        initWhiteList();
    } // End Function
    public void indexDocumentsFromFile(String filename){
        /*
        This method is used to  :
            1- read an input file (filepath) Line by Line
            2- Gather all of these lines in one Text String
            3- add a Line Separator between Lines in the Resultant Text
            4- Send The Resultant Text To The Method extractFieldsFromXmlAndIndex
                to identify the xml tags and Index them according to the needs
         */
        String line , XMLText = "";
        short lineNumber = 1;
        try {
            BufferedReader br = openDocumentFile(filename);
            try {
                line = br.readLine();
                while (line != null){
                    if (lineNumber > 3 && !line.startsWith("</nitf>"))
                        XMLText += line + System.lineSeparator();
                    line = br.readLine();
                    lineNumber++;
                }

            } finally {
                if (!XMLText.isEmpty())
                    extractFieldsFromXmlAndIndex(XMLText);
                br.close();
            }
        }
        catch (Exception e){
            System.out.println(" caught a " + e.getClass() +  e.getLocalizedMessage() +
                    "\n with message: " + e.getMessage());
            System.exit(0);
        }
    } // End Function
    public void extractFieldsFromXmlAndIndex(String xmlString){
            String   docnum,
                     title ,
                     content,
                     source="Unknown",
                     pubdate ="",
                     safeText = org.jsoup.Jsoup.clean(xmlString,whiteList),
                    values[] = new String[5] ,
                    all;
             Document doc;

            jdoc = org.jsoup.Jsoup.parse(safeText);

            // Get Document NUmber
            docnum = getFieldAttribute("doc-id" , "id-string").trim();
            // Get Title
            title = getFieldText("title");

            // Get PubDate
            pubdate = getPubDate();
            // Get Source (Publisher)
            source = getFieldAttribute( "pubdata" , "name");

            // Get Contents of the File
            content = pubdate;
            content += getFieldAttribute("doc.copyright","year" ) + " ";
            content += getFieldAttribute("doc.copyright","holder") + " ";
            content += getFieldText( "classifier") + " ";
            content += source + " ";
            content  += getFieldText( "hl1") + " ";
            content += getFieldText( "p") + " ";

            // Gather All in All (RAW) Field
            all = title + " " + content + " " + source + " " + pubdate;

          /*  The Fields Sequence
               It should be as initialization sequence in InitFields Function
            1= Document Number - String Field
           2- Publish Date - String Field
           3- Title - Text Field
           4- Content - Text Field
           5- All - Text Field
           */

          // Fill The Values in Array
            values[0] = docnum;
           values[1] = pubdate;
           values[2] = title;
           values[3] = content;
           values[4] = all;
           // Create New Document
            doc = new Document();
            // Move from Values Array to Fields Array and add to a new Document to Preparre it
            // Check Whether to add biGram RAW Field

            for (short i = 0 ; i < values.length ; i++)
            {
                fields.get(i).setStringValue(values[i]);
                doc.add(fields.get(i));
            }
            doc.add(getBigramField(all));
           System.out.println(String.format("Adding document: %s Title %s" , docnum , title));
           // Add the resultant document to the Indexer
           addDocumentToIndex(doc);
        } // End Function
} // End Class
