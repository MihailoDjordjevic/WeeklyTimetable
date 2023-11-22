package mainTest;

import dataModel.TermAttribute;
import dataRepresentation.WeeklyTerm;
import implementation.WeeklyTimetable;
import specification.Timetable;
import specification.TimetableManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {


        try {
            Class.forName("implementation.WeeklyTimetable");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Timetable timetable = TimetableManager.getRegisteredTimetable();


        timetable.loadClassrooms("src/main/resources/classrooms.csv");
        timetable.loadData("src/main/resources/rafTimetable.csv");

        //System.out.println(((WeeklyTimetable) timetable).getTerms());

//        WeeklyTerm weeklyTerm = new WeeklyTerm(((WeeklyTimetable) timetable).getClassrooms().get("Raf20 (a)"));
//        weeklyTerm.setStartTime(LocalTime.parse("09:15"));
//        weeklyTerm.setEndTime(LocalTime.parse("11:00"));
//        weeklyTerm.setActiveFrom(LocalDate.parse("2023-05-04"));
//        weeklyTerm.setActiveUntil(LocalDate.parse("2023-05-04"));
//
//        weeklyTerm.getTermAttributes().put("Predmet", new TermAttribute("Predmet", "Likovno"));
//        weeklyTerm.getTermAttributes().put("Tip", new TermAttribute("Tip", "V"));
//        weeklyTerm.getTermAttributes().put("Grupe", new TermAttribute("Grupe", "202"));
//        weeklyTerm.getTermAttributes().put("Nastavnik", new TermAttribute("Nastavnik", "Mikica"));
//        weeklyTerm.getTermAttributes().put("Dan", new TermAttribute("Dan", "CET"));
//        weeklyTerm.getTermAttributes().put("Ucionica", new TermAttribute("Ucionica", "Raf20 (a)"));

        //((WeeklyTimetable) timetable).addTermFromCSV(weeklyTerm);
        System.out.println(System.getProperty("user.dir"));
        //Files.createFile(Path.of(System.getProperty("user.dir") + "miki.png"));
        //((WeeklyTimetable) timetable).printFreeTimes(((WeeklyTimetable) timetable).getTerms(), LocalDate.parse("2023-05-03"), LocalDate.parse("2023-05-25"));

        Scanner scanner = new Scanner(System.in);
        String word = scanner.nextLine();
        while (!word.equals("stop")){
            timetable.search(word);
            word = scanner.nextLine();
        }

    }
}
