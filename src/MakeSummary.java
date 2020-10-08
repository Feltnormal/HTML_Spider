import java.util.*;

public class MakeSummary {

    /**returns number of pages that aren't 30x, 404 or any other error pages*/
  public int getNumberOfPages(ArrayList<Webpage> list) {
    int counter = 0;
    for (Webpage w : list) {
        int code = w.getResponseCode();
      if (code < 300 && code != 0) counter++;
    }
    return counter;
  }

    /**returns number of pages that are 30x response code pages*/
  public ArrayList<Webpage> getRedirects(ArrayList<Webpage> list) {
    ArrayList<Webpage> redirectPages = new ArrayList<>();
    for (Webpage w : list) {
      if (w.getResponseCode() == 302) redirectPages.add(w);
    }
    return redirectPages;
  }

    /**returns number of pages that are 404 response code pages*/
  public ArrayList<Webpage> getNotFound(ArrayList<Webpage> list) {
      ArrayList<Webpage> notFoundPages = new ArrayList<>();
    for (Webpage w : list) {
      if (w.getResponseCode() == 404) notFoundPages.add(w);
    }
    return notFoundPages;
  }
  
  /**returns the largest web page*/
  public Webpage getLargestPage(ArrayList<Webpage> list) {
  	Collections.sort(list, new SortByContentSize()); //sort by content size (ascending)
    return list.get(0);
  }

    /**returns the most recently update web page*/
  public Webpage getEarliestPage(ArrayList<Webpage> list) {
  	Collections.sort(list, new SortByDate()); //sort by content size (earliest to latest)
    return list.get(0);
  }
  
    /**filters out all 404 response coed pages from a list of web pages*/
  public ArrayList<Webpage> filterOut404AndBrokenLinks(ArrayList<Webpage> in) {
      Iterator<Webpage> i = in.iterator();
      while (i.hasNext()) {
          if (i.next().getResponseCode() == 404) {
              i.remove();
          }
      }
      return in;
    }
  }

  //***********************CLASSES GO HERE********************
    class SortByContentSize implements Comparator<Webpage> {
      public int compare(Webpage a, Webpage b) {
          return b.getContentLength() - a.getContentLength();
      }
  } 

  class SortByDate implements Comparator<Webpage> {
      public int compare(Webpage a, Webpage b) {
          Date aDate = a.getDateAndTime();
          Date bDate = b.getDateAndTime();

          if (bDate == null) { return (aDate == null) ? 0 : 1;}
          if (aDate == null) return 1;

          return bDate.compareTo(aDate);
      }
  }

