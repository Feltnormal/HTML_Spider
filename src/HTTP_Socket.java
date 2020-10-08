import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class HTTP_Socket {
    private static ArrayList<Webpage> pagesVisited = new ArrayList<>();
    private static ArrayList<String> pagesToVisit = new ArrayList<>();
    private static final int port = 9780;

    //Remember to execute the makefile in the form: make run URL=http://3310exp.hopto.org:9780/
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        int numberOfPages = 100;    //total number of pages we are able to search
        pagesToVisit.add(args[0]);

        for (int i =0; i <= numberOfPages; i++) {
            if (pagesToVisit.size() == 0)  break;   //if we finish crawling before numberOfPages is reached
            Thread.sleep(2100);               //to avoid any 503 errors
            System.out.println("Processing page number " + i);
            sendGET(pagesToVisit.get(0));
        }
        System.out.println("*****************DONE*******************");
        printReport();
    }

    private static void sendGET(String inputURL) throws IOException, ParseException{
        if (inputURL == null) return;

       try { //attempt to send a GET to a web server
           Socket sock = new Socket(Parser.getHostName(inputURL), port);
           OutputStream output = sock.getOutputStream();

           //Retrieve the web page
           PrintWriter setUp = new PrintWriter(output, false);
           setUp.print("GET " + inputURL + " HTTP/1.0\r\n");
           setUp.print("Accept: text/plain, text/html, text/*\r\n");
           setUp.print("\r\n");
           setUp.flush();

           //start listening to the response
           BufferedReader in = new BufferedReader(new InputStreamReader(
                   sock.getInputStream()));
           String inputLine = in.readLine();

           //initialise variables to store in web page object
           String ipAddress = Parser.getIP(inputURL);
           int contentLength = 0;
           Date dateAndTime = null;
           int responseCode = 0;
           HashSet<String> pointers = new HashSet<>();

           //begin scraping information from the page
           while ((inputLine) != null) {

               Date potentialDate = Parser.getDateModified(inputLine);
               int potentialContentLength = Parser.getContentLength(inputLine);
               String potentialURL = Parser.getURLLinks(inputLine);

               if (responseCode == 0) { //if we still haven't encountered the first response code line
                   int rCode = Parser.getResponseCode(inputLine);
                   if (rCode == 404) {
                       responseCode = 404;
                       //only add the page to the queue if the current url has already been visited
                       if (potentialURL != null && !checkIfVisited(potentialURL, true) && !checkIfVisited(potentialURL, false)) {
                           pagesToVisit.add(potentialURL);
                       }
                       break;
                   } else {
                       responseCode = rCode;
                   }
               }
               //add the date and time
               if (potentialDate != null && dateAndTime == null) dateAndTime = potentialDate;

               //add content length
               if (potentialContentLength != 0 && contentLength == 0) contentLength = potentialContentLength;

               //add the url to the queue if it isn't already in it
               if (potentialURL != null && !checkIfVisited(potentialURL, true) && !checkIfVisited(potentialURL, false)) {
                   pagesToVisit.add(potentialURL);
                   pointers.add(potentialURL); //add the url to the web page's child url links
               } else if (potentialURL != null) {
                   pointers.add(potentialURL);
               }
               inputLine = in.readLine();
           }
           in.close();

           Webpage webpage = new Webpage(responseCode, Parser.getPureURL(inputURL),inputURL, dateAndTime, contentLength, ipAddress, pointers);

           //only add to our list of pages visited if it isn't already in pagesVisited
           boolean alreadyVisited = checkIfVisited(webpage.getPureURL(), true);
           if (!alreadyVisited) pagesVisited.add(webpage);
           pagesToVisit.remove(0); //remove from the queue

       } catch (UnknownHostException ex) { //if the web page is broken

           Webpage webpage = new Webpage(0, "0",inputURL, null, 0, "0", null);
           //only add to our list of pages visited if it isn't already in pagesVisited
           boolean alreadyVisited = false;
           for (Webpage w : pagesVisited) {
               if (w.getPureURL().equals(webpage.getPureURL())) {
                   alreadyVisited = true;
                   break; //add to list of pages visited
               }
           }
           if (!alreadyVisited) pagesVisited.add(webpage);
           System.out.println("Unkown host: " + inputURL);
           pagesToVisit.remove(0);

       }
    }

    public static boolean checkIfVisited(String inputURL, boolean pagesVisitedList) throws MalformedURLException, UnknownHostException {
        inputURL = Parser.getPureURL(inputURL);
        if (!pagesVisitedList) { //boolean used to see which list we need to compare it to
            for (String s : pagesToVisit) {
                if (Parser.getPureURL(s).equals(inputURL)) {
                    return true;
                }
            }
            return false;
        } else {
            for (Webpage w : pagesVisited) {
                if (w.getPureURL().equals(inputURL)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static void printReport() {
        MakeSummary summary = new MakeSummary();
        ArrayList<Webpage> notFound = summary.getNotFound(pagesVisited); //find 404 first to avoid side effect after filtering (below)
        ArrayList<Webpage> filteredList = summary.filterOut404AndBrokenLinks(pagesVisited);

        //initialise variables for readabilities
        int numberOfPages = summary.getNumberOfPages(filteredList);
        Webpage largestPage = summary.getLargestPage(filteredList);
        Webpage earliestPage = summary.getEarliestPage(filteredList);
        ArrayList<Webpage> redirectPages = summary.getRedirects(filteredList);

        System.out.println("Report Summary: ");
        System.out.println("- Total pages (Not counting Redirects, 404s, or other ): " + numberOfPages);
        System.out.println("- Largest page: "+ largestPage + " with " + largestPage.getContentLength() + " Bytes");
        System.out.println("- Most recently modified: "+ earliestPage + " at " + earliestPage.getDateAndTime());
        System.out.println("- 404 Not Found pages: "+ notFound);
        System.out.println("- Redirect pages: ");
        for (Webpage w: redirectPages) { //print all redirect pages and where they point to
            System.out.println("- "+ w + " points to the following page(s): " + w.getPointer());
        }
    }
}


