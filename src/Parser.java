import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    /**Returns the date from a string (if it has a date)*/
    public static Date getDateModified(String inputString) throws ParseException {
        DateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        Pattern p = Pattern.compile("Last-Modified: ");
        Matcher m = p.matcher(inputString);
        String toGetParsed;

        if (m.find()) {
            toGetParsed = inputString.substring(15); //retrieve everything past "Last-Modified: "
            return format.parse(toGetParsed);
        } else {
            return null;
        }

    }

    /** This is to find any HTTP response codes in a string*/
    public static int getResponseCode(String inputString) {
        Pattern p = Pattern.compile("HTTP/1.1 ");
        Matcher m = p.matcher(inputString);

        if (m.find()) {
            inputString = inputString.substring(9).substring(0,3);
            int out = Integer.parseInt(inputString);
            return out; //All http response codes are 3 digits
        } else {
            return 0;
        }
    }

    /** Finds the content length of the GET response*/
    public static int getContentLength(String inputString) {
        Pattern p = Pattern.compile("Content-Length: ");
        Matcher m = p.matcher(inputString);

        if (m.find()) {
            inputString = inputString.substring(16);
            int out = Integer.parseInt(inputString);
            return out;
        } else {
            return 0;
        }
    }

    /** Parses and returns any URLs found in a string*/
    public static String getURLLinks (String inputString) {
        Pattern p1 = Pattern.compile("href="); //regular href lines
        Pattern p2 = Pattern.compile("Location:"); //for redirect lines
        Pattern p3 = Pattern.compile("http://"); //for the very first page to crawl

        Matcher m1 = p1.matcher(inputString);
        Matcher m2 = p2.matcher(inputString);
        Matcher m3 = p3.matcher(inputString);

        if (m1.find()) {
            String url = "";
            int index = inputString.indexOf('"');
            inputString = inputString.substring(index+1); //skip straight to the start of the url

            for (char c : inputString.toCharArray()) {
                if (c != '"') {
                    url += c;
                } else {
                    break;
                }
            }
            return url;
        } else if (m2.find()) { //FOR 302 Response header layouts
            String url = "";
            inputString = inputString.substring(10); //skip straight to the start of the url

            for (char c : inputString.toCharArray()) {
                if (c != '"') {
                    url += c;
                } else {
                    break;
                }
            }
            return url;
        } else if (m3.find())  { //where this would be the case of our very first "crawl"
            return inputString;
        } else {
            return null;
        }
    }

    /** Parses any URLs found in a string and converts it into an IP address */
    public static String getIP (String inputString) throws MalformedURLException{
        try {
            if (getURLLinks(inputString) != null) {
                InetAddress address = InetAddress.getByName(getHostName(inputString));
                String ip = address.getHostAddress();
                return ip;
            } else {
                return "NO_IP_FOUND";
            }
        } catch (UnknownHostException u) {
            return "NO_IP_FOUND";
        }

    }

    /** Parses any URLs found in a string and returns the hostname */
    public static String getHostName(String inputString) {
        inputString = getURLLinks(inputString);
        Pattern p = Pattern.compile("http://");
        Matcher m = p.matcher(inputString);

        if (m.find()) {
            String url = "";
            inputString = inputString.substring(7); //skip straight to the start of the url

            for (char c : inputString.toCharArray()) {
                if (c != ':') {
                    url += c;
                } else {
                    break;
                }
            }
            return url;
        } else {
            return null;
        }
    }

    /** Parses any URLs found in a string and converts the hostname portion into it's IP
     * e.g http://3310exp.hopto.org:9780/60/62.html becomes http://52.65.194.50/60/62.html
     * Normalising URLs in this way makes comparisons simpler so we may avoid repeating pages*/
    public static String getPureURL (String inputString) throws MalformedURLException, UnknownHostException {
        inputString = getURLLinks(inputString); //clean up the the read line
        String hostIP = getIP(inputString);
        if (hostIP == null) return inputString; //in case a broken link is found
        Pattern p1 = Pattern.compile("http://"); //identify start of url
        Pattern p2 = Pattern.compile(".html"); //identify end of url
        Matcher m1 = p1.matcher(inputString);
        Matcher m2 = p2.matcher(inputString);

        if (m1.find()) {
            inputString = inputString.substring(7); //skip straight to the start of the url
            int index = 0;
            for (char c : inputString.toCharArray()) {
                if (c != '/') {
                    index++;
                } else {
                    break;
                }
            }
            if (m2.find()) //some urls may not contain and / character and this will append one for consistency
                return "http://" + hostIP + inputString.substring(index);
            else {
                if (inputString.charAt(inputString.length() -1) == '/') {
                    return "http://" + hostIP + inputString.substring(index);
                }
                return "http://" + hostIP + inputString.substring(index) + "/";
            }
        } else
            return null;

    }

}
