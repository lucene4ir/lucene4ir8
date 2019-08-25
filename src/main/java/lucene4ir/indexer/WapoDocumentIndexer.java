/**
 *
 * This Class is Used to index CAR - CAST - CommonCore 2018 (WAPO) data files
 *    INDEXERS - GITHUB REFERENCE PAGE
 *     URL : https://github.com/ABDULAZIZALQATAN/Indexers
 * Created by Abdulaziz on 19/03/2019.
 * Edited by Abdulaziz on 28/06/2019.
 */

package lucene4ir.indexer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lucene4ir.Lucene4IRConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.DocValuesNumbersQuery;

import java.io.BufferedReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class WapoDocumentIndexer extends DocumentIndexer {
    // Properties
    ArrayList<Field> fields;
    String contentFieldName = Lucene4IRConstants.FIELD_BODY;
    int lineNumber;

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
            aField = new TermVectorEnabledTextField(fldName, "", Field.Store.YES);
        else
            aField = new TextField(fldName, "", Field.Store.YES);
        fields.add(aField);
    }
    private void initFields()
    {
        /*
        Initialize Fields Collection in the Following Sequence :
        1- Document Number
        2- Title
        3- Paragraph
        4- ALL
        Then Add them to the fields Collection
 */
        fields = new ArrayList<Field>();
        addFieldToDocument(Lucene4IRConstants.FIELD_DOCNUM,'s');
        addFieldToDocument(Lucene4IRConstants.FIELD_TITLE,' ');
        addFieldToDocument(Lucene4IRConstants.FIELD_PUBDATE,' ');
        addFieldToDocument("contentID",' ');
        addFieldToDocument(contentFieldName,' ');
        addFieldToDocument(Lucene4IRConstants.FIELD_RAW,' ');

      //  addFieldToDocument(Lucene4IRConstants.FIELD_AUTHOR,' ');
       // addFieldToDocument(Lucene4IRConstants.FIELD_SOURCE,' ');
    }

    // Constructor Method (Initialization)
    public WapoDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional){
        super(indexPath, tokenFilterFile, positional);
        {
             // Constructor Method to initialize the Fields Collection
         initFields();
        }
    }

    private String extractContent(JsonElement contentElement)
    {
        // This Function is Used to get the contents of current Paragraph fields
        String result = "";
        if (contentElement.isJsonObject()) {
            if (contentElement.getAsJsonObject().has("content"))
                result = contentElement.getAsJsonObject().get("content").toString();
           // else if (jObject.has("fullcaption"))
             //   result = jObject.get("fullcaption").toString();
            } // End IF
        return result;
    }

    private Field getField (String fldName)
    {
        // This Function is to get a Field Given its name

        Field result = fields.get(0);
        for (int i = 0 ; i < fields.size() ; i++)
            if (fields.get(i).name() == fldName)
                {
                    result = fields.get(i);
                    break;
                }
        return  result;
    }

    private String getPubDate (String inDate)
    {
         // Given date in Milli Seconds - return String Date formatted by ( dd/mm/yyyy )

        String result = "";

         if (StringUtils.isNumeric(inDate.trim()))
         {
             Date aDate = new Date(Long.parseLong(inDate));
             SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
             result = df.format(aDate);
         }
        return result;
    }

   public void parseLine(String line)
    {
         /*
        This Function is used to  :
        1- Parse a Single Json Line From Washingtop POST (WAPO)  Corpus Document
        2- Extract the required properties from the Line
        3- Add The Properties to Corpus Fields Collection
        4- Add The corpusFields Collection in the Corpus Document inside the Fields Collection
        5- Index The Resultant Document

        It is very important to mention the order of Fields in the Corpus Document Line
        1- Document Number (id)
        2- Title
        3- PubDate
        4- Content ID
        5- Content
        6- ALL (RAW)
        */
        // Local Variables
        Document doc;
        Field fld ;
        JsonParser jParser;
        JsonElement jElement ;
        JsonArray contentField;
        String  fldName , // Current Field Name
                fldValue, // Current Field Value
                allValue, // Gathering All Value
                content , // Current Content
                StaticAllValue,
                outLine ,
                staticFldNames[] = {Lucene4IRConstants.FIELD_DOCNUM ,
                                    Lucene4IRConstants.FIELD_TITLE
                                    , Lucene4IRConstants.FIELD_PUBDATE };

        short i , contentID ; // iterator
        // Parse The input Line
        jParser = new JsonParser();
        jElement = jParser.parse(line);
        StaticAllValue = "";
        outLine = "";
        // Check If the Line is a Valid Json Object
        if (jElement.isJsonObject()) {

            /* Fill Main Stable Fields
            1- DocNum
            2- Title
            3- PubDate

            and Save Static Fields ( Standard Fields For All Contents )
            and Make StaticAllValue from Combining Static Fields
            */

            for (i = 0; i < staticFldNames.length; i++) {
                fldName = staticFldNames[i];
                if (i == 2)
                    // PubDate
                {
                    fldValue = jElement.getAsJsonObject().get("published_date").toString();
                    fldValue = getPubDate(fldValue);
                } // End IF
                else
                {
                    fldValue = jElement.getAsJsonObject().get(fldName).toString().replaceAll("\"","");
                    outLine += fldName + ": " + fldValue + ", ";
                }
                StaticAllValue += fldValue + " ";
                fld = getField(fldName);
                fld.setStringValue(fldValue);
            } // End For

            // Get Contents Array
            contentField = jElement.getAsJsonObject().get(Lucene4IRConstants.FIELD_BODY).getAsJsonArray();
            contentID = 0;
            outLine = lineNumber++ + "- Indexing " + contentField.size() +
                    " contents for " + outLine.substring(0 , outLine.length() - 2);
            System.out.println(outLine);
            for (i = 0; i < contentField.size(); i++) {
                // Get Current Content
                content = extractContent(contentField.get(i));
                // If Content is not Available Skip
                if (content.isEmpty())
                    continue;

                // Insert Static Fields
                    doc = new Document();
                for (int j = 0 ; j < staticFldNames.length ; j++ )
                {
                    fld = getField(staticFldNames[j]);
                    doc.add(fld);
                } // End For

                allValue = StaticAllValue;

                // Insert Content ID Field
                fldName = "contentID";
                fld = getField(fldName);
                fldValue = String.valueOf(contentID++);
                allValue += fldValue + " ";
                fld.setStringValue(fldValue);
                doc.add(fld);

                // Insert Content Field
                fldName = contentFieldName;
                fld = getField(fldName);
                fldValue = content;
                allValue += fldValue + " ";
                fld.setStringValue(fldValue);
                doc.add(fld);

                // Insert All Field
                fldName = Lucene4IRConstants.FIELD_RAW;
                fld = getField(fldName);
                fldValue = allValue.trim();
                fld.setStringValue(fldValue);
                doc.add(fld);


                addDocumentToIndex(doc);

            } // End For
        } // End IF jElement.isJsonObject()
    }
        public void indexDocumentsFromFile (String fileName){
            // Local Variables for a Single Line and Line Separator
            String line;
            // Read The Input File into a Buffer
            try {
                BufferedReader br = openDocumentFile(fileName);
                lineNumber = 1;
                // Iterate Through Lines and Parse Lines each by each
                while ((line = br.readLine()) != null)
                    parseLine(line); // Send The Current Line To The Parse Line Function

            }  // End Try
            catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            } // End CATCH
        } // End Function
}
