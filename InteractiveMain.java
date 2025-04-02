import java.io.IOException;
import java.util.Scanner;

public class InteractiveMain {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Summer Olympics Wikipedia Scraper!");
        System.out.println("We have scraped the data. You can:");
        System.out.println("1. List all past and present Olympic sports that start with the letter ___.");
        System.out.println("2. List all countries (full name, not country code) that have participated in the Olympic1s, " +
                "but are now considered “obsolete”.");
        System.out.println("3. List all countries that have won at least ___ silver medals in ___");
        System.out.println("4. List all countries that had podium sweeps in ___");
        System.out.println("5. How many total medals has ___ won in ___?");
        System.out.println("6. How many governing bodies of the past or present sports from the Summer Olympics are" +
                " headquartered in ___?");
        System.out.println("7. Among all Summer Olympics hosted in ___ since ___, how many countries did the\n" +
                "torch relay that covered the longest total distance pass through?");
        System.out.println("8. How many total (gold/silver/bronze) medals have been awarded in (sport)?");
        System.out.println("Type 'exit' to quit.\n");

        while (true) {
            System.out.print("Enter a number (1-8) or 'exit': ");

            // Take in user input on the next line
            String input = scanner.nextLine();

            // match on or use the user input with the 'input' variable
            if (input.equals("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            switch (input) {
                case "1":
                    System.out.print("Enter a letter: ");
                    String letter = scanner.nextLine().toUpperCase();

                    if (letter.length() == 1 && Character.isLetter(letter.charAt(0))) {
                        System.out.println("Listing all Olympic sports that start with the letter " + letter + ":");
                        WebParser.getSportsByLetter(letter);
                    } else {
                        System.out.println("Invalid input. Please enter a single letter.");
                    }
                    break;

                case "2":
                    System.out.println("Question 2 answer:");

                    WebParser.obsoleteCountries();
                    break;

                case "3":
                    System.out.print("Enter the minimum number of silver medals: ");
                    String medalsInput = scanner.nextLine();
                    System.out.print("\nEnter the year: ");
                    String yearSilverInput = scanner.nextLine();

                    int medals = Integer.parseInt(medalsInput);
                    int yearSilver = Integer.parseInt(yearSilverInput);

                    System.out.println("Listing countries with at least " + medals + " silver medals in " + yearSilver + ":");

                    WebParser.silverMedalCountries(medals, yearSilverInput);
                    break;

                case "4":
                    System.out.print("Enter the year: ");
                    String yearPodiumInput = scanner.nextLine();

                    WebParser.podiumSweeps(yearPodiumInput);
                    break;

                case "5":
                    System.out.print("Enter the country name: ");
                    String countryMedals = scanner.nextLine();
                    System.out.print("\nEnter the sport: ");
                    String sport = scanner.nextLine();

                    WebParser.sportMedalCount(countryMedals, sport);
                    break;

                case "6":
                    System.out.print("Enter the country name: ");
                    String countryHq = scanner.nextLine();

                    WebParser.governingBodies(countryHq);
                    break;

                case "7":
                    System.out.print("Enter the country name: ");
                    String countryTorch = scanner.nextLine();
                    System.out.print("\nEnter the year: ");
                    String yearTorchInput = scanner.nextLine();
                    int yearTorch = Integer.parseInt(yearTorchInput);

                    WebParser.torchRelay(countryTorch, yearTorch);
                    break;

                case "8":
                    System.out.print("Enter the sport: ");
                    String sportName = scanner.nextLine();
                    System.out.print("\nEnter the type of medal (Gold, Silver, or Bronze): ");
                    String medalInput = scanner.nextLine();

                    WebParser.totalMedalCount(medalInput, sportName);
                    break;
            }
        }
        scanner.close();
    }
}
