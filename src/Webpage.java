import java.util.Date;
import java.util.HashSet;

public class Webpage{
    private int responseCode;
    private String url;
    private String pureURL;
    private HashSet<String> pointsTo; //uses hash set to avoid duplicates
    private Date dateAndTime;
    private int contentLength; //in bytes
    private String ipAddress;

    public Webpage(int responseCode, String pureURL,String url, Date dateAndTime, int contentLength, String ipAddress, HashSet<String> pointsTo) {
        this.responseCode = responseCode;
        this.url = url;
        this.pureURL = pureURL;
        this.dateAndTime = dateAndTime;
        this.contentLength = contentLength;
        this.ipAddress = ipAddress;
        this.pointsTo = pointsTo;
    }

    public String getURL() { return url;    }

    public String getPureURL() {return pureURL;}

    public String getIpAddress() {
        return ipAddress;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public int getContentLength() { return contentLength; }

    public HashSet<String> getPointer() { return pointsTo;}

    public Date getDateAndTime() {
        return dateAndTime;
    }

    @Override
    public String toString() {
        return url;
    }

}
