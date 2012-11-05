package com.ttsnetwork.elicitationtool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author farago
 */
public class UploadXmlServlet extends HttpServlet {

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean errorOccurred = true;
        String errorMessage = null;
        response.setContentType("text/html");
        try {

            if (ServletFileUpload.isMultipartContent(request)) {
                // Create a factory for disk-based file items
                DiskFileItemFactory factory = new DiskFileItemFactory();
                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);
                // Parse the request
                List items = upload.parseRequest(request);

                factory.setRepository(new File(getServletContext().getRealPath("tmp")));

                //Max file size
                upload.setFileSizeMax(Long.parseLong(
                        getServletContext().getInitParameter("maxXmlFileSize")));

                Iterator i = items.iterator();

                while (i.hasNext()) {
                    FileItem item = (FileItem) i.next();
                    //Check for size
                    if (item.getSize() > 0) {
                        //Check mimetype
                        if (getServletContext().getMimeType(item.getName()).equals("application/xml")) {
                            File xml = new File(FileViewData.getXmlFileRepository(),
                                    item.getName());
                            //Check file existence
                            if (!xml.exists()) {
                                //Create DOM document from item file
                                SAXBuilder builder = new SAXBuilder();
                                InputStream in = item.getInputStream();
                                Document doc = builder.build(in);
                                in.close();

                                //Validate the xml document
//                                System.out.println("Start validation");
                                List<String> validationErrorMessages = new ArrayList<String>();
                                validateXml(doc, validationErrorMessages);
//                                System.out.println("Error messages: " + validationErrorMessages);

                                if (validationErrorMessages.isEmpty()) {
                                    FileWriter writer = new FileWriter(xml);
                                    new XMLOutputter(Format.getPrettyFormat()).output(doc, writer);
                                    writer.close();
                                    System.out.println("File: " + xml.getName()
                                            + " successfully uploaded to Advance Elicitation Tool");
                                    errorOccurred = false;
                                    response.getWriter().write("<p "
                                            + "id=\"success-message\">"
                                            + item.getName() + "</p>");
                                    FileManager.getInstance().add(xml);
                                } else {

                                    StringBuilder sb = new StringBuilder();
                                    for (String s : validationErrorMessages) {
                                        sb.append(s).append("\n");
                                    }
                                    errorMessage = sb.toString();

                                }


                            } else {
                                errorMessage = getErrorMessage(5); //Name already exists
                            }

                        } else {
                            errorMessage = getErrorMessage(4); //Not an XML
                        }
                    } else {
                        errorMessage = getErrorMessage(3); //Size is 0MB
                    }
                }

            } else {
                errorMessage = getErrorMessage(1); //Not multipart content
            }
        } catch (FileUploadException ex) {
            System.out.println("Advance Elicitation Tool have thrown an exception "
                    + "on date: " + new Date());
            errorMessage = getErrorMessage(2); //Size exceeded
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Advance Elicitation Tool have thrown an exception "
                    + "on date: " + new Date());
            errorMessage = getErrorMessage(7); //Generic exception
            ex.printStackTrace();
        }

        if (errorOccurred) {
            /*
             * Set a custom error message for the client side's jQuery code
             */
            response.getWriter().write("<p id=\"error-message\">"
                    + errorMessage + "</p>");
        }
    }

    /**
     * Validates a document tree for the ELicitation Tool
     *
     * @param doc - JDOM Document to be validated
     * @return true if valid, false otherwise
     */
    private boolean isValidXml(Document doc) {
//        int validElementNeeded = 8;
//        int validElementCounted = 0;
        String floatRegEx = "[-+]?[0-9]*\\.?[0-9]+";
        Pattern pattern = Pattern.compile("\\((" + floatRegEx + ") (" + floatRegEx + ")\\)");
        ArrayList<Element> leafList = new ArrayList<Element>();
        Iterator i;
        Element currNode;

        //START VALIDATION
        if (doc.hasRootElement()) {

            Element root = doc.getRootElement();
//            return validateLayer(root.getChildren("node"), root.getChildren("node").size() == 2);
            return isValidLayer(root.getChildren("node"));
        } else {
            return false;
        }
//        return validElementCounted == validElementNeeded;
    }

    /**
     *
     * Validate a JDOM XML document for the Advance Elicitation Tool
     *
     * @param doc - JDOM document
     * @param errorMessages - List of String that will contains the error
     * messages relative to the validation
     * @return List with error messages. if empty, the document is valid
     */
    public void validateXml(Document doc, List errorMessages) {

//        System.out.println("----- START VALIDATION -----");
        if (doc.hasRootElement()) {

            Element root = doc.getRootElement();
            List rootChildren = root.getChildren("node");

            if (!rootChildren.isEmpty()) {
                errorMessages = validateXmlLayer(errorMessages, rootChildren);
            } else {
                errorMessages.add("Document without node tags.");
            }

        } else {
            errorMessages.add("Document without node tags.");
        }

    }

    /**
     * Return an error message string according to the error number passed as
     * parameter
     *
     * @param errorNum integer representing the error number
     * @return String
     */
    public String getErrorMessage(int errorNum) {
        String errorString;
        switch (errorNum) {
            case 1:
                errorString = "The request cannot be parsed.";
                break;
            case 2:
                errorString = "File size exceeded maximum limit [5MB].";
                break;
            case 3:
                errorString = "File has wrong size.";
                break;
            case 4:
                errorString = "File is not recognized as xml document.";
                break;
            case 5:
                errorString = "File already exists. Choose a different name";
                break;
            case 6:
                errorString = "Document is not valid. Check the xml tree"
                        + " for errors.";
                break;
            case 7:
                errorString = "The file cannot be uploaded.";
                break;
            default:
                errorString = "Unexpected error. Please, contact the administrator.";
                break;
        }

        return errorString;
    }

    /**
     * Validate all the layer of an XML doc for Advance Elicitation Tool
     * starting from the given layer and proceeding to the bottom layers.<br/>
     * <i>Layer</i> stands for the <i>list</i> of all nodes at the same level
     * into the document's tree
     *
     * @param layerNodes java.util.List containing all of the jdom.Element in
     * that layer
     * @param is2ChidrenLayer boolean that indicate which rules of validation
     * the function has to follow <ul style="margin: 2px 0px 2px 5px;
     * list-style-type: none;"> <li>true if an element with 2 children was
     * already found in the document</li> <li>false if an element with 2
     * children wasn't already found in the document</li></ul>
     * @return true if the document is valid, otherwise false
     */
    public boolean validateLayer(List layerNodes, boolean is2ChidrenLayer) {
        boolean is2ChildrenSubLayer = is2ChidrenLayer; //Flag for the layer underneath the current

        Iterator i = layerNodes.iterator();
        ArrayList<Element> subLayerNodes = new ArrayList<Element>();
        Element currNode;

        while (i.hasNext()) {

            currNode = (Element) i.next();
            System.out.println("Validating node: " + currNode.getAttributeValue("label"));
            List children = currNode.getChildren("node");
            Iterator j = children.iterator();

            if (is2ChidrenLayer) { //Node with 2 children already found
                switch (children.size()) {
                    case 0:
                        if (currNode.getAttribute("label") == null
                                || currNode.getAttribute("ri") == null
                                || currNode.getAttribute("question") == null) {
//                            System.out.println("Leaf with wrong attributes");
                            return false;
                        }
                        break;
                    case 2:
                        if (currNode.getAttribute("label") == null
                                || currNode.getAttribute("ri") == null) {
//                            System.out.println("Parent of 2 children with wrong attributes");
                            return false;
                        }
                        break;
                    default:
//                        System.out.println("Parent with wrong children amount");
                        return false;
                }
            } else { //Node with 2 children not found yet
                switch (children.size()) {
                    case 0:
                        if (currNode.getAttribute("label") == null
                                || currNode.getAttribute("ri") == null
                                || currNode.getAttribute("question") == null) {
//                            System.out.println("Leaf with wrong attributes");
                            return false;
                        }
                        break;
                    case 1:
                        if (currNode.getAttribute("label") == null) {
//                            System.out.println("Parent of 1 children with wrong attributes");
                            return false;
                        }
                        break;
                    case 2:
                        is2ChildrenSubLayer = true;
                        if (currNode.getAttribute("label") == null) {
//                            System.out.println("Parent of 2 children with wrong attributes");
                            return false;
                        }
                        break;
                    default:
//                        System.out.println("Parent with wrong children amount");
                        return false;
                }
            }

            while (j.hasNext()) {
                subLayerNodes.add((Element) j.next());
            }

        }

        if (subLayerNodes.size() > 0) {
            if (!validateLayer(subLayerNodes, is2ChildrenSubLayer)) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     * Validate the layers of an XML document Layer stands for all the tags on
     * the same level on the XML doc tree
     *
     * @param layerNodes
     * @return
     */
    public boolean isValidLayer(List layerNodes) {
        Iterator i = layerNodes.iterator();
        ArrayList<Element> subLayerNodes = new ArrayList<Element>();
        Element currNode;

        while (i.hasNext()) {

            currNode = (Element) i.next();
            List children = currNode.getChildren("node");
            Iterator j = children.iterator();

//            System.out.println("Validating node: " + currNode.getAttributeValue("label"));

            if (currNode.getAttribute("label") == null /*
                     * || currNode.getAttribute("code") == null
                     */) {
                currNode.addContent("This node doesn't have correct attributes!");
                return false;
            }

            if (currNode.getAttribute("ri") == null) {
//                System.out.println("Adding ri attribute");
                currNode.setAttribute("ri", "0");
            }

            if (children.isEmpty()) { //The node is a leaf
                if (currNode.getAttribute("question") == null
                        || currNode.getAttribute("value-mg") == null) {
                    currNode.addContent("This node doesn't have correct attributes!");
                    return false;
                }
            }

            while (j.hasNext()) {
                subLayerNodes.add((Element) j.next());
            }

        }

        if (!subLayerNodes.isEmpty()) {
            if (!isValidLayer(subLayerNodes)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validate a layer of an xml tree for Advance Elicitation Tool
     * Check for attribute
     * <ul>
     * <li>Label</li>
     * <li>Ri</li>
     * <li>Question - only for leaf nodes</li>
     * <li>Value-mg - only for leaf nodes</li>
     * <li>Values - only for leaf nodes</li>
     * <li>Generic-type - only for leaf nodes</li>
     * </ul>
     *
     * @param errorMessages
     * @param layerNodes
     * @return
     */
    public List validateXmlLayer(List errorMessages, List layerNodes) {
        Iterator i = layerNodes.iterator();
        ArrayList<Element> subLayerNodes = new ArrayList<Element>();
        Element currNode;

        while (i.hasNext()) {

            currNode = (Element) i.next();
            List children = currNode.getChildren("node");
            Iterator j = children.iterator();

//            System.out.println("Validating node: " + currNode.getAttributeValue("label"));
//            if (currNode.getAttribute("code") == null) {
//                errorMessages.add("Node without code.");
//            }

            if (currNode.getAttribute("label") == null) {
                errorMessages.add("Node " + currNode + " doesn't have label attribute.");
            }
            if (currNode.getAttribute("ri") == null && layerNodes.size() > 1) {
//                System.out.println("Adding ri attribute");
                errorMessages.add("Node " + currNode.getAttribute("label") + " doesn't have ri attribute.");
//                currNode.setAttribute("ri", "0");
            }

            if (children.isEmpty()) { //The node is a leaf
                if (currNode.getAttribute("question") == null) {
                    errorMessages.add("Node " + currNode.getAttribute("label") + " doesn't have a question attribute.");
                }
                if (currNode.getAttribute("value-mg") == null) {
                    errorMessages.add("Node " + currNode.getAttribute("label") + " doesn't have a value-mg attribute.");
                }
                if (currNode.getAttribute("values") == null) {
                    errorMessages.add("Node " + currNode.getAttribute("label") + " doesn't have a values attribute.");
                }
                if (currNode.getAttribute("generic-type") == null) {
                    errorMessages.add("Node " + currNode.getAttribute("label") + " doesn't have a generic-type attribute.");
                }

            }

            //Populate subLayerNodes with the children of the current node
            while (j.hasNext()) {
                subLayerNodes.add((Element) j.next());
            }

        }

        if (!subLayerNodes.isEmpty()) {
            validateXmlLayer(errorMessages, subLayerNodes);
        }

        return errorMessages;
    }
}
