import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.Collections.max;

public class WebParser {
    // Fetches and returns the Document for a given URL
    public static Document fetchPage(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            System.out.println("Failed to fetch page: " + e.getMessage());
            return null;
        }
    }

    // Question 1: Returns olympic sports starting with a certain letter

    public static List<String> getSportsByLetter(String letter) throws IOException {
        List<String> sportsList = new ArrayList<>();

        // fetch the core url that I find everything from. This part is the same for all the questions
        Document doc = fetchPage("https://en.wikipedia.org/wiki/Summer_Olympic_Games");
        if (doc == null){
            return null;
        }

        //the table listing out all the sports is the first one in the article with sortable and wikitable classifications
        Element sportsTable = doc.select("table.sortable.wikitable").first();
        if (sportsTable == null){
            System.out.println("No sports table found");
            return sportsList;
        }

        // traverse through the rows of the table to match the first letter of the sport with the inputted letter
        Elements rows = sportsTable.select("tr");
        for (Element row : rows) {
            Elements cells = row.select("td");
            if (!cells.isEmpty()){
                Element leftCell = cells.get(0);
                String sportsName = leftCell.text();
                if (sportsName.startsWith(letter)) {
                    sportsList.add(sportsName);
                }
            }
        }

        //prints out results
        if (sportsList.isEmpty()){
            System.out.println("No sports starting with letter " + letter.toUpperCase() + " found.");
        }
        System.out.println(sportsList.toString());
        return sportsList;
    }

    // Question 2: List all countries (full name, not country code) that have participated in the Olympics, but are
    // now considered “obsolete”

    public static List<String> obsoleteCountries() {
        List<String> obCountriesList = new ArrayList<>();
        Document doc = fetchPage("https://en.wikipedia.org/wiki/Summer_Olympic_Games");
        if (doc == null){
            return null;
        }

        //finds link with this specific title
        Element link = doc.select("a[title='List of participating nations at the Summer Olympic Games']").first();

        // Get the href attribute for the link and opens new document with that link
        String href = link.attr("href");
        String participatingNationsUrl = "https://en.wikipedia.org/" + href;

        Document doc2 = fetchPage(participatingNationsUrl);
        if (doc2 == null){
            return null;
        }

        // find the table with the country names, identify that the cell has a country name based on the background
        // color of the cell
        Element countriesTable = doc2.select("table.wikitable").first();
        if (countriesTable == null){
            System.out.println("No countries table found");
        }
        Elements grayCells = doc2.select("td[bgcolor='#e0e0e0']");

        // iterate through cells to check if the country is obsolete and print out results
        for (Element td : grayCells) {
            // Check if this is the first cell in the row (i.e., a country name cell)
            Element parentRow = td.parent();

            if (parentRow != null) {
                Elements tds = parentRow.select("td");

                if (!tds.isEmpty() && tds.get(0).equals(td)) {
                    // get the link or text inside the first <td>
                    Element a = td.selectFirst("a");

                    if (a != null) {
                        String countryName = a.text().trim();
                        obCountriesList.add(countryName);
                    }
                }
            }
        }
        System.out.println(obCountriesList);
        return obCountriesList;
    }

    // Question 3: List all countries that have won at least # silver medals in (Year).

    public static List<String> silverMedalCountries(int silverMedals, String silverYear){
        List<String> silverMedalsList = new ArrayList<>();

        Document doc = fetchPage("https://en.wikipedia.org/wiki/Summer_Olympic_Games");
        if (doc == null){
            return null;
        }

        // find the specific competition years table by iterating through the tables
        Elements tables = doc.select("table.wikitable");
        Element yearsTable = null;

        for (Element table : tables) {
            if (!table.select("ul li").isEmpty()) {
                yearsTable = table;
                break;
            }
        }

        if (yearsTable == null){
            System.out.println("No table of competition years found");
        }

        //Iterate through the table to find the links to the metal tables based on the year
        Elements listItems = yearsTable.select("ul li");
        String medalTableUrl = null;

        for (Element li : listItems) {
            Element firstLink = li.selectFirst("a");
            if (firstLink != null && firstLink.text().equals(silverYear)) {
                // Return absolute URL to medal table
                medalTableUrl = firstLink.attr("abs:href");
            }
        }

        if (medalTableUrl == null) {
            System.out.println("No medal table found for year: " + silverYear);
        }

        Document doc2 = fetchPage(medalTableUrl);
        if (doc2 == null){
            return null;
        }
        Element table = doc2.select("table.wikitable.sortable").first();

        // with the new table go through the rows to find all the countries that have won at least # silver medals
        // add these countries to a list and print it out
        Elements rows = table.select("tr");
        for (Element row : rows) {
            Element countryNameCell = row.selectFirst("th[scope=row]");
            Elements cells = row.select("td");

            // Skip rows without a country name or not enough medal columns
            if (countryNameCell == null || cells.size() < 4) {
                continue;
            }
            String countryName = countryNameCell.text().trim();
            String silverText = cells.get(2).text().trim();

            try {
                int silverMedalCount = Integer.parseInt(silverText);
                if (silverMedalCount >= silverMedals) {
                    silverMedalsList.add(countryName);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid silver medal count: " + silverText + " for " + countryName);
            }
        }

        System.out.println(silverMedalsList);
        return silverMedalsList;
    }

    //Question 4: List all countries that had podium sweeps in (Year).

    public static List<String> podiumSweeps(String podiumYear) {
        List<String> podiumSweepsList = new ArrayList<>();
        Document doc = fetchPage("https://en.wikipedia.org/wiki/Summer_Olympic_Games");
        if (doc == null){
            return null;
        }

        // find the table that has links to each year's summer olympic games
        Elements tables = doc.select("table.sortable.wikitable");
        Element yearsTable = tables.get(2);
        if (yearsTable == null) {
            System.out.println("No table of competition years found");
        }

        // get the link to each year's olympic page from this table based on whether the year number matches the
        // inputted year. open this link as a new document
        String link = null;
        if (yearsTable != null) {
            Elements rows = yearsTable.select("tr");
            for (int i = 2; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cells = row.getAllElements();
                Element yearCell = cells.get(0);
                if (yearCell == null) {
                    System.out.println("yearcell null!");
                }
                if (yearCell != null && yearCell.text().contains(podiumYear)) {
                    Element linkElement = row.select("td").get(1);
                    Element linkText = linkElement.selectFirst("a");
                    link = linkText.attr("abs:href");
                }
            }
        }

        Document doc2 = fetchPage(link);
        if (doc2 == null){
            return null;
        }

        // find the podium sweeps table on this page. we assume that this table exists for all relevant years
        Elements tables2 = doc2.select("table.wikitable");
        Element sweepsTable = null;
        for (Element table : tables2) {
            Element header = table.selectFirst("tr");
            Elements cols = header.select("th");

            if(cols.size() == 7){
                sweepsTable = table;
            }
        }

        //print out the list of all countries in the podium sweeps table as our result
        if (sweepsTable == null){
            System.out.println("No sweeps table found for year: " + podiumYear);
        } else {
            Elements rows = sweepsTable.select("tr");
            int maxRow = rows.size();

            for (int i = 1; i < maxRow; i++) {
                Element row = rows.get(i);

                Element countryCell = row.select("td").get(3);
                String countryName = countryCell.text();
                if (!countryName.isEmpty() && !countryName.equals("NOC")) {
                    podiumSweepsList.add(countryName);
                }
            }

            System.out.println("Podium sweep countries in " + podiumYear + " is: " + podiumSweepsList);
        }
        return podiumSweepsList;
    }

    // Question 5: How many total medals has (Country) won in (Sport)?

    public static void sportMedalCount(String country, String sportName) {
        Document doc = fetchPage("https://en.wikipedia.org/wiki/Summer_Olympic_Games");

        Element sportsTable = doc.select("table.sortable.wikitable").first();
        if (sportsTable == null){
            System.out.println("No sports table found");
        }

        //find and open the link to the particular sport from the sports table from question 1
        Elements rows = sportsTable.select("tr");
        String link = null;

        for (Element row : rows) {
            Elements cells = row.select("td");

            if (!cells.isEmpty()){
                Element leftCell = cells.get(0);
                String sportsName = leftCell.text();

                if (sportsName.contains(sportName)) {
                    Element sportsLink = leftCell.selectFirst("a");
                    link = sportsLink.attr("abs:href");
                }
            }
        }

        Document doc2 = fetchPage(link);
        if (doc2 == null){
            System.out.println("No website found");
        }

        // in the table of medal counts find the name of the country and return the total number of medals corresponding
        Element table2 = doc2.select("table.wikitable.sortable").first();
        Elements rows2 = table2.select("tr");
        for (Element row : rows2) {
            Element countryCell = row.selectFirst("th");
            String countryName = countryCell.select("a").text();
            if (!countryName.isEmpty() && countryName.contains(country)) {
                Element medalCell = row.select("td").get(4);
                String medalCount = medalCell.text();
                System.out.println(country + " won, in total " + medalCount + " medal(s) in " + sportName);
            }
        }
    }

    // Question 6: How many governing bodies of the past or present sports from the Summer Olympics are
    // headquartered in (country)?

    public static int governingBodies(String country) {
        int count = 0;
        Document doc = fetchPage("https://en.wikipedia.org/wiki/Summer_Olympic_Games");
        List<String> sportsLinksList = new ArrayList<>();
        List<String> governingLinksList = new ArrayList<>();

        // get the links to each of the sports pages from the sports table. add all of these links to a list
        Element sportsTable = doc.select("table.sortable.wikitable").first();
        if (sportsTable == null){
            System.out.println("No sports table found");
        }

        Elements rows = sportsTable.select("tr");
        String link = null;

        for (Element row : rows) {
            Elements cells = row.select("td");

            if (!cells.isEmpty()){
                Element leftCell = cells.get(0);
                Element sportsLink = leftCell.selectFirst("a");
                link = sportsLink.attr("abs:href");
                sportsLinksList.add(link);

            }
        }

        // traverse through the list of the links and open each as a document. in each one, find the next link
        // to the page for the governing body of the sport. add all these links to a different list
        String link2 = null;
        for (int i = 0; i < sportsLinksList.size(); i++) {
            Document doc2 = fetchPage(sportsLinksList.get(i));
            if (doc2 == null){
                System.out.println("No website found");
            }

            Element table2 = doc2.select("table.infobox").first();
            Elements rows2 = table2.select("tr");
            for (Element row : rows2) {
                Element td = row.selectFirst("td");
                Element th = row.selectFirst("th");
                if (th != null && th.text().contains("Governing body")){
                    Element orgLink = td.selectFirst("a");
                    link2 = orgLink.attr("abs:href");
                    governingLinksList.add(link2);
                }
            }

        }

        // now cycle through the governing body links. in the description of the headquarters, see if the country
        // matches with the user input. if so, add one to count. United States and U.S. are stated differently so
        // this is an edge case that we handle. note that the UK and England are different countries.
        for (int i = 0; i < governingLinksList.size(); i++) {
            Document doc2 = fetchPage(governingLinksList.get(i));
            if (doc2 == null){
                System.out.println("No website found");
            }

            Element table2 = doc2.select("table.infobox").first();
            Elements rows2 = table2.select("tbody > tr");

            for (Element row : rows2) {
                Element th = row.selectFirst("th");
                if (th != null && th.text().contains("Headquarters")){
                    Element td = row.selectFirst("td");
                    if (td != null) {
                        if (td.text().contains(country)) {
                            count++;
                        } else if (country.equals("United States") && td.text().contains("U.S.")){
                            count++;
                        } else if (country.equals("U.S.") && td.text().contains("United States")) {
                            count++;
                        }
                    }
                }
            }
        }
        System.out.println("There are " + count + " governing bodies headquartered in " + country);
        return count;
    }

    // Question 7: Among all Summer Olympics hosted in (country) since (year), how many countries did the
    // torch relay that covered the longest total distance pass through?

    public static int torchRelay(String country, int yearInput) {
        int count = 0;
        Map<Integer, Integer> map = new HashMap<>();
        Document doc = fetchPage("https://en.wikipedia.org/wiki/Summer_Olympic_Games");

        // get the link from the first table that goes to the torch relays wikipedia page. open this as a new document
        Element firstTable = doc.select("table.sidebar").first();
        Element tableTr = firstTable.select("tr").get(3);
        Elements links = tableTr.select("li");
        String relayLink = null;

        for (Element link : links) {
            if (link.text().contains("Torch relays")) {
                Element href = link.selectFirst("a");
                relayLink = href.attr("abs:href");
            }
        }
        Document doc2 = fetchPage(relayLink);
        if (doc2 == null){
            System.out.println("No relay website found");
        }

        // go to the table of all the relay information
        Element relayTable = doc2.select("table.wikitable.sortable").first();
        Elements rows = relayTable.select("tbody > tr");
        int rowCount = rows.size();
        List<Integer> distances = new ArrayList<>();
        List<Integer> rowNums = new ArrayList<>();

        // cycle through this table each row at a time, not including the header row or the three at the end
        // that do not have relay information
        for (int i = 1; i < rowCount - 3; i++) {
            Element row = rows.get(i);
            Element lengthCell = row.select("td").get(2);
            String lengthText = null;
            Integer length = null;

            // get the length as an Integer instead of String
            if (!lengthCell.text().isEmpty() && !lengthCell.text().equals("-")) {
                lengthText = lengthCell.text().replaceAll("[,]", "");
                length = Integer.parseInt(lengthText);
            }

            // get the country name two different ways to make sure I cover everything, from the last country
            // to be named in the parentheses in the right most cell as well as from the emoji of the flag in
            // the left most cell
            Element rightCell = row.select("td").get(4);
            String rightCellText = rightCell.text();
            Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
            Matcher matcher = pattern.matcher(rightCellText);
            String lastMatch = null;
            Element leftCell = row.selectFirst("td");
            Element flagPic = leftCell.selectFirst("img");
            String flag = flagPic.attr("src");

            while (matcher.find()) {
                lastMatch = matcher.group(1);
            }

            // get the year as an integer form the left most cell
            Element yearText = leftCell.select("a").get(1);
            String year = yearText.text().trim();
            String yearNum = year.replaceAll("[^0-9]", "");
            Integer yearInt = null;
            if (!yearNum.isEmpty()) {
                yearInt = Integer.parseInt(yearNum);
            }

            // if the name of the country matches either the flag or the last country in parentheses, add the
            // length of the relay as well as the number i of the row to these map
            if ((flag.contains(country) || lastMatch != null && lastMatch.contains(country)) && yearInt >= yearInput) {
                map.put(i, length);
                rowNums.add(i);
                distances.add(length);
            }
        }
        if (distances.isEmpty()){
            System.out.println("Invalid: this country and year combo does not produce any Olympic games.");
        }

        // get the maximum distance
        Integer maxDistance = max(distances);
        Integer rowNum = null;

        // match the maximum distance to the row number i
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            Integer rowNumMap = entry.getKey();
            Integer distance = entry.getValue();

            if (distance == maxDistance) {
                rowNum = rowNumMap;
            }
        }

        // find that row i and get the text from the right most cell
        Element row = rows.get(rowNum);
        Element rightCell = row.select("td").get(4);
        rightCell.select("i, em").remove();
        String rightCellText = rightCell.text();

        // find the number of countries from the text with parentheses around it. remove any that are italicized,
        // contain the word "by", or California as edge cases. add these countries to a list
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(rightCellText);
        Set<String> countries = new HashSet<>();

        while (matcher.find()) {
            String countryFound = matcher.group(1).trim();
            if (!countryFound.contains("by ") && !countryFound.contains("California")) {
                countries.add(countryFound);
            }
        }

        //print out the size of the list as our result.
        count = countries.size();
        System.out.println("Number of countries visited is: " + count);
        return count;
    }

    // Question 8: How many total (Gold/Silver/Bronze) medals have been awarded in (sport)?

    public static int totalMedalCount(String medalType, String sportName) {
        int count = 0;
        Document doc = fetchPage("https://en.wikipedia.org/wiki/Summer_Olympic_Games");

        // get the link to the sport from the sports table and open it as a new document
        Element sportsTable = doc.select("table.sortable.wikitable").first();
        if (sportsTable == null){
            System.out.println("No sports table found");
        }

        Elements rows = sportsTable.select("tr");
        String link = null;

        for (Element row : rows) {
            Elements cells = row.select("td");
            if (!cells.isEmpty()){
                Element leftCell = cells.get(0);
                String sportsName = leftCell.text();
                if (sportsName.contains(sportName)) {
                    Element sportsLink = leftCell.selectFirst("a");
                    link = sportsLink.attr("abs:href");
                }
            }
        }

        Document doc2 = fetchPage(link);
        if (doc2 == null){
            System.out.println("No website found");
        }

        //find the table with all the previously issued medals in that sport.
        Element table2 = doc2.select("table.wikitable.sortable").first();
        Element footer = table2.selectFirst("tr.sortbottom");
        Elements cells = footer.select("th");

        //match the input text with the type of medal. print out the result of the total amount of that type of
        // medal that has been awarded in the past to any country.
        if (medalType.equals("Gold")) {
            String goldCell = cells.get(1).text();
            System.out.println("Total " + medalType + " medals in " + sportName + " is: " + goldCell);
        } else if (medalType.equals("Silver")) {
            String silverCell = cells.get(2).text();
            System.out.println("Total " + medalType + " medals in " + sportName + " is: " + silverCell);
        } else if (medalType.equals("Bronze")) {
            String bronzeCell = cells.get(3).text();
            System.out.println("Total " + medalType + " medals in " + sportName + " is: " + bronzeCell);
        } else {
            System.out.println("Medal input type must be 'Gold', 'Silver', or 'Bronze'.");
        }
        return count;
    }
}