Hi! Thanks for taking the time to grade this assignment. To run the program, run InteractiveMain.java. That
is also where I prompt the user for input. WebParser.java is where most of my code is for each question.
URLGetter.java and URLTester.java are also required for this program, although they just contain the example
code that Prof. Swap demoed in class.

Additionally, due to the length and complexity of the code, I included explanations of each component of
my code in as comments in WebParser.java. Please reference those comments for my thought process.

General assumptions:
- User inputs country names, sport names, etc. with correct capitalization (first letter of first word
uppercased for sports, all first letters uppercased for countries). If something outputs an error, try
double-checking the correct capitalization used in the wikipedia articles.

Specific assumptions:

Question 3: Sometimes the code will print out the names of countries with some special character symbol at the
end based on what the wikipedia article includes. During lecture, Professor Swap mention that this is alright.

Question 4: I assume that if there exists podium sweeps, there will be a table for podium sweeps
in the wikipedia article I go to. Also, my output will include all the countries that had podium
sweeps in that year including potential repeats.

Question 6: Since there is both "U.S." and "United States", I allow these two country names to be equal
to one another when outputting the total count. There are also a lot of loops in my solution for 6, so it
may take a little bit of time before being able to print out (please have patience)

Question 7: For the United States, for some reason, all the state names are links apart from one "California," so
I handled that edge case separately. In general, I assume that any location name in parentheses that is also italicized
is a city/state/province and not a country. For this question, please also input "US" instead of "United States".



