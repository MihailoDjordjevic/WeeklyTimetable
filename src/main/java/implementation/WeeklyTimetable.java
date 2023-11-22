package implementation;

import dataModel.AbstractClassroom;
import dataModel.AbstractTerm;
import dataModel.ClassroomAttribute;
import dataModel.TermAttribute;
import dataRepresentation.Classroom;
import dataRepresentation.WeeklyTerm;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import specification.Timetable;
import specification.TimetableManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class WeeklyTimetable extends Timetable {

    private List<WeeklyTerm> terms;
    private LocalDate activeFrom;
    private LocalDate activeUntil;
    private HashMap<String, Classroom> classrooms;
    private List<WeeklyTerm> searchResult;

    static {
        TimetableManager.registerTimetable(new WeeklyTimetable());
    }

    private WeeklyTimetable(){
        init();
    }

    private void init(){
        terms = new ArrayList<>();
        classrooms = new HashMap<>();
        searchResult = new ArrayList<>();
        activeFrom = LocalDate.parse("2023-04-01");
        activeUntil = LocalDate.parse("2023-07-01");
    }

    @Override
    public void addClassroom(String classroomName) {
        if (!getClassrooms().containsKey(classroomName))
            getClassrooms().put(classroomName, new Classroom(classroomName));
    }

    @Override
    public void addTerm(String termData) {

        String[] parsed = termData.split("/");

        if (parsed.length < 7){
            System.out.println("Wrong number of arguments");
            return;
        }

        String predmet = parsed[0];
        String tip = parsed[1];
        String nastavnik = parsed[2];
        String grupe = parsed[3];
        String dan = parsed[4];
        String termin = parsed[5];
        String[] time = parseTime(termin);
        String ucionica = parsed[6];

        WeeklyTerm newTerm = new WeeklyTerm(classrooms.get(ucionica));
        newTerm.setStartTime(LocalTime.parse(time[0]));
        newTerm.setEndTime(LocalTime.parse(time[1]));

        newTerm.getTermAttributes().put("Predmet", new TermAttribute("Predmet", predmet));
        newTerm.getTermAttributes().put("Tip", new TermAttribute("Tip", tip));
        newTerm.getTermAttributes().put("Grupe", new TermAttribute("Grupe", grupe));
        newTerm.getTermAttributes().put("Nastavnik", new TermAttribute("Nastavnik", nastavnik));
        newTerm.getTermAttributes().put("Dan", new TermAttribute("Dan", dan));
        newTerm.getTermAttributes().put("Ucionica", new TermAttribute("Ucionica", ucionica));

        if (parsed.length > 8){
            newTerm.setActiveFrom(LocalDate.parse(parsed[7]));
            newTerm.setActiveUntil(LocalDate.parse(parsed[8]));
        } else{
            newTerm.setActiveFrom(this.getActiveFrom());
            newTerm.setActiveUntil(this.getActiveUntil());
        }

        List<AbstractTerm> testTerms = new ArrayList<>();

        getTerms().forEach(weeklyTerm -> {
            if (weeklyTerm.getTermAttributes().get("Dan").getData().equals(newTerm.getTermAttributes().get("Dan").getData()) &&
                    weeklyTerm.getTermAttributes().get("Ucionica").getData().equals(newTerm.getTermAttributes().get("Ucionica").getData())){
                testTerms.add(weeklyTerm);
            }
        });

        for (AbstractTerm term : testTerms) {
            if (newTerm.getStartTime().isBefore(((WeeklyTerm) term).getEndTime()) &&
                    newTerm.getEndTime().isAfter(((WeeklyTerm) term).getStartTime()) &&
                    newTerm.getActiveFrom().isBefore(((WeeklyTerm) term).getActiveUntil()) &&
                    newTerm.getActiveUntil().isAfter(((WeeklyTerm) term).getActiveFrom())) {
                System.out.println("Not able to add " + newTerm);
                return;
            }
        }

        getTerms().add(newTerm);
    }

    @Override
    public void deleteTerm(AbstractTerm abstractTerm) {
        getTerms().remove(abstractTerm);
    }

    @Override
    public void moveTerm(AbstractTerm abstractTerm) {

    }


    /**
     * Search using different commands
     * @param s search string to be parsed
     */
    @Override
    public void search(String s) {

        searchResult.clear();
        searchResult.addAll(getTerms());

        String[] parsed = s.split(" ");
        List<String> input = new ArrayList<>();
        for (String s1 : parsed) {
            input.add(s1);
        }

//        switch (input.get(0)){
//            case "free" -> printFreeTimes(getTerms(), getActiveFrom(), getActiveUntil());
//            case "addt" -> addTerm(input.get(1));
//            case "addc" -> addClassroom(input.get(1));
//        }

        switch (input.get(0)) {
            case "free" -> {
                if (input.size() > 2) {
                    printFreeTimes(getTerms(), LocalDate.parse(input.get(1)), LocalDate.parse(input.get(2)));
                } else printFreeTimes(getTerms(), getActiveFrom(), getActiveUntil());
                return;
            }
            case "print" -> System.out.println(searchResult);
            default -> {
                input.forEach(s1 -> {
                    String[] tokens = s1.split("/");
                    switch (tokens[0]){
                        case "tatt" -> searchByTermAttribute(tokens[1], tokens[2]);
                        case "taf" -> searchTermActiveFrom(tokens[1], tokens[2]);
                        case "tau" -> searchTermActiveUntil(tokens[1], tokens[2]);
                        case "tst" -> searchTermStartTime(tokens[1], tokens[2]);
                        case "tet" -> searchTermEndTime(tokens[1], tokens[2]);
                        case "ca" -> searchByClassroomAttribute(tokens[1], tokens[2]);
                        case "ica" -> searchByIntegerClassroomAtttribute(tokens[1], tokens[2], tokens[3]);
                    }
                });
            }
        }

        System.out.println(searchResult);
    }

    @Override
    public void loadData(String filename) {
        File file = new File(filename);
        if (file.getName().endsWith(".csv"))
            readTermsFromCSV(file);
        else if (file.getName().endsWith(".json"))
            readFromJSON(file);
    }

    @Override
    public void loadClassrooms(String filename) {
        File file = new File(filename);
        if (file.getName().endsWith(".csv"))
            readClassroomsFromCSV(file);
    }

    @Override
    public void save(String s) {
    }

    private void readFromJSON(File file) {
    }

    private void readTermsFromCSV(File file) {

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
        Reader reader;
        Iterable<CSVRecord> records;
        try {
            reader = new FileReader(file);
            records = csvFormat.parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> header = ((CSVParser) records).getHeaderNames();

        for (CSVRecord record : records){

            String predmet = record.get(0);
            String tip = record.get("Tip");
            String nastavnik = record.get("Nastavnik");
            String grupe = record.get("Grupe");
            String dan = record.get("Dan");
            String termin = record.get("Termin");
            String[] time = parseTime(termin);
            String ucionica = record.get("Ucionica");

            WeeklyTerm weeklyTerm = new WeeklyTerm(classrooms.get(ucionica));
            weeklyTerm.setStartTime(LocalTime.parse(time[0]));
            weeklyTerm.setEndTime(LocalTime.parse(time[1]));

            if (header.contains("Od"))
                weeklyTerm.setActiveFrom(LocalDate.parse(record.get("Od")));
            else weeklyTerm.setActiveFrom(this.getActiveFrom());

            if (header.contains("Do"))
                weeklyTerm.setActiveUntil(LocalDate.parse(record.get("Do")));
            else weeklyTerm.setActiveUntil(this.getActiveUntil());

            weeklyTerm.getTermAttributes().put("Predmet", new TermAttribute("Predmet", predmet));
            weeklyTerm.getTermAttributes().put("Tip", new TermAttribute("Tip", tip));
            weeklyTerm.getTermAttributes().put("Grupe", new TermAttribute("Grupe", grupe));
            weeklyTerm.getTermAttributes().put("Nastavnik", new TermAttribute("Nastavnik", nastavnik));
            weeklyTerm.getTermAttributes().put("Dan", new TermAttribute("Dan", dan));
            weeklyTerm.getTermAttributes().put("Ucionica", new TermAttribute("Ucionica", ucionica));

            addTermAttributes(weeklyTerm, header, record);

            addTermFromCSV(weeklyTerm);
        }
    }

    public void addTermFromCSV(WeeklyTerm newTerm) {

        List<AbstractTerm> testTerms = new ArrayList<>();

        getTerms().forEach(weeklyTerm -> {
            if (weeklyTerm.getTermAttributes().get("Dan").getData().equals(newTerm.getTermAttributes().get("Dan").getData()) &&
                    weeklyTerm.getTermAttributes().get("Ucionica").getData().equals(newTerm.getTermAttributes().get("Ucionica").getData())){
                testTerms.add(weeklyTerm);
            }
        });

        for (AbstractTerm term : testTerms) {
            if (newTerm.getStartTime().isBefore(((WeeklyTerm) term).getEndTime()) &&
                    newTerm.getEndTime().isAfter(((WeeklyTerm) term).getStartTime()) &&
                    newTerm.getActiveFrom().isBefore(((WeeklyTerm) term).getActiveUntil()) &&
                    newTerm.getActiveUntil().isAfter(((WeeklyTerm) term).getActiveFrom())) {
                System.out.println("Not able to add " + newTerm);
                return;
            }
        }

        getTerms().add(newTerm);
    }

    private void readClassroomsFromCSV(File f){

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
        Reader reader;
        Iterable<CSVRecord> records;
        try {
            reader = new FileReader(f);
            records = csvFormat.parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> header = ((CSVParser) records).getHeaderNames();

        for (CSVRecord r : records){
            String name = r.get("ime");
            String capacity = r.get("kapacitet");

            Classroom classroom = new Classroom(name);
            addClassroomFromCSV(classroom);

            classroom.getAttributes().put("kapacitet", new ClassroomAttribute("kapacitet", capacity));
            classroom.setAttributesHeader(header.subList(1, header.size()));

            addClassroomAttributes(classroom, header, r);
        }

    }

    public void addClassroomFromCSV(AbstractClassroom abstractClassroom) {
        if (!getClassrooms().containsKey(abstractClassroom.getName()))
            getClassrooms().put(abstractClassroom.getName(), (Classroom) abstractClassroom);
    }

    private void addClassroomAttributes(AbstractClassroom classroom, List<String> attributes, CSVRecord record){

        if (attributes.size() < 3) return;

        for (int i = 2; i < attributes.size(); i++){
            ClassroomAttribute classroomAttribute = new ClassroomAttribute(attributes.get(i), record.get(i));
            classroom.getAttributes().put(attributes.get(i), classroomAttribute);
        }
    }

    private void addTermAttributes(AbstractTerm term, List<String> attributes, CSVRecord record){

        if (attributes.size() < 8) return;

        for (int i = 7; i < attributes.size(); i++){
            if (attributes.get(i).equals("Od") || attributes.get(i).equals("Do")) continue;
            String data = record.get(i);
            TermAttribute termAttribute = new TermAttribute(attributes.get(i), data);
            term.getTermAttributes().put(attributes.get(i), termAttribute);
        }
    }

    public void printFreeTimes(List<WeeklyTerm> terms, LocalDate startDate, LocalDate endDate){

        if (startDate == null)
            startDate = this.getActiveFrom();
        if (endDate == null)
            endDate = this.getActiveUntil();

        HashMap<LocalDate, HashMap<Classroom, List<WeeklyTerm>>> data = new HashMap<>();

        while (!startDate.isAfter(endDate)){

            HashMap<Classroom, List<WeeklyTerm>> dailyTerms = new HashMap<>();

            getClassrooms().forEach((s, classroom) -> {
                dailyTerms.put(classroom, new ArrayList<>());
            });

            data.put(startDate, dailyTerms);

            startDate = startDate.plusDays(1);
        }

        for (WeeklyTerm term : terms) {

            LocalDate termActiveFrom = term.getActiveFrom() == null ? this.activeFrom : term.getActiveFrom();
            LocalDate termActiveUntil = term.getActiveUntil() == null ? this.activeUntil : term.getActiveUntil();

            String day = (String) term.getTermAttributes().get("Dan").getData();
            DayOfWeek dayOfWeek = findDayOfWeek(day);

            data.forEach((localDate, classroomListHashMap) -> {

                if (localDate.getDayOfWeek() == dayOfWeek){

                    if (!localDate.isBefore(termActiveFrom)  && !localDate.isAfter(termActiveUntil)){
                        Classroom classroom = (Classroom) term.getClassroom();
                        data.get(localDate).get(classroom).add(term);
                    }
                }
            });
        }

        data.forEach((localDate, classroomListHashMap) -> {
            data.get(localDate).forEach((classroom, weeklyTerms) -> {
                data.get(localDate).get(classroom).sort((o1, o2) -> {
                    if (o1.getStartTime().isBefore(o2.getStartTime())) return  -1;
                    else return 1;
                });
            });
        });

        StringBuilder stringBuilder = new StringBuilder();

        data.forEach((localDate, classroomListHashMap) -> {

            stringBuilder.append(localDate + "\n\n_______________________\n");

            classroomListHashMap.forEach((classroom, weeklyTerms) -> {

                stringBuilder.append("\n\t" + classroom + "________________________\n");
                stringBuilder.append(freeTimeBetweenTerms(weeklyTerms));

            });

        });

        System.out.println(stringBuilder);


    }

    private DayOfWeek findDayOfWeek(String dayName){
        switch (dayName){
            case "PON" -> {
                return DayOfWeek.MONDAY;
            }
            case "UTO" -> {
                return DayOfWeek.TUESDAY;
            }
            case "SRE" -> {
                return DayOfWeek.WEDNESDAY;
            }
            case "CET" -> {
                return DayOfWeek.THURSDAY;
            }
            case "PET" -> {
                return DayOfWeek.FRIDAY;
            }
            case "SUB" -> {
                return DayOfWeek.SATURDAY;
            }
            case "NED" -> {
                return DayOfWeek.SUNDAY;
            }
            default -> {
                return null;
            }
        }
    }

    private String freeTimeBetweenTerms(List<WeeklyTerm> terms){

        StringBuilder stringBuilder = new StringBuilder();

        List<LocalTime> times = new ArrayList<>();

        times.add(0, LocalTime.parse("23:59"));
        times.add(0, LocalTime.parse("00:00"));

        terms.forEach(weeklyTerm -> {
            if (weeklyTerm.getStartTime().getHour() == (times.get(times.size()-2)).getHour())
                times.set((times.size()-2), weeklyTerm.getStartTime());
            else {
                times.add((times.size() - 1), weeklyTerm.getStartTime());
                times.add((times.size() - 1), weeklyTerm.getEndTime());
            }
        });

        for (int i = 0; i < times.size(); i+=2){
            stringBuilder.append(times.get(i) + "-").append(times.get(i+1) + "\n");
        }

        return stringBuilder.toString();
    }

    private String[] parseTime(String time){

        String[] parsed = time.split("-");

        String[] start = parsed[0].split(":");
        String[] end = parsed[1].split(":");

        String[] result = new String[2];

        if (start.length < 2)
            result[0] = start[0] + ":00:00";
        else result[0] = start[0] + ":" + start[1] + ":00";

        if (end.length < 2)
            result[1] = end[0] + ":00:00";
        else result[1] = end[0] + ":" + end[1] + ":00";

        return result;
    }

    private void searchByTermAttribute(String attributeName, String attributeValue){

        List<WeeklyTerm> newSearchResult = new ArrayList<>();
        String rebuilt = attributeValue.replace('-', ' ');


        searchResult.forEach(weeklyTerm -> {
            if (weeklyTerm.getTermAttributes().get(attributeName).getData().equals(rebuilt))
                newSearchResult.add(weeklyTerm);
        });

        searchResult = newSearchResult;
    }

    private void searchTermActiveFrom(String date, String compare){
        List<WeeklyTerm> newSearchResult = new ArrayList<>();

        searchResult.forEach(weeklyTerm -> {
            switch (compare){
                case ">" -> {
                    if(!weeklyTerm.getActiveFrom().isBefore(LocalDate.parse(date)))
                        newSearchResult.add(weeklyTerm);
                }
                case "<" -> {
                    if(weeklyTerm.getActiveFrom().isBefore(LocalDate.parse(date)))
                        newSearchResult.add(weeklyTerm);
                }
            }
        });

        searchResult = newSearchResult;
    }

    private void searchTermActiveUntil(String date, String compare){
        List<WeeklyTerm> newSearchResult = new ArrayList<>();

        searchResult.forEach(weeklyTerm -> {
            switch (compare){
                case ">" -> {
                    if(!weeklyTerm.getActiveUntil().isBefore(LocalDate.parse(date)))
                        newSearchResult.add(weeklyTerm);
                }
                case "<" -> {
                    if(weeklyTerm.getActiveUntil().isBefore(LocalDate.parse(date)))
                        newSearchResult.add(weeklyTerm);
                }
            }
        });

        searchResult = newSearchResult;
    }

    private void searchTermStartTime(String date, String compare) {
        List<WeeklyTerm> newSearchResult = new ArrayList<>();

        searchResult.forEach(weeklyTerm -> {
            switch (compare) {
                case ">" -> {
                    if (!weeklyTerm.getStartTime().isBefore(LocalTime.parse(date)))
                        newSearchResult.add(weeklyTerm);
                }
                case "<" -> {
                    if (weeklyTerm.getStartTime().isBefore(LocalTime.parse(date)))
                        newSearchResult.add(weeklyTerm);
                }
            }
        });

        searchResult = newSearchResult;
    }

        private void searchTermEndTime(String date, String compare){
            List<WeeklyTerm> newSearchResult = new ArrayList<>();

            searchResult.forEach(weeklyTerm -> {
                switch (compare){
                    case ">" -> {
                        if(!weeklyTerm.getEndTime().isBefore(LocalTime.parse(date)))
                            newSearchResult.add(weeklyTerm);
                    }
                    case "<" -> {
                        if(weeklyTerm.getEndTime().isBefore(LocalTime.parse(date)))
                            newSearchResult.add(weeklyTerm);
                    }
                }
            });

        searchResult = newSearchResult;
    }

    private void searchByClassroomAttribute(String attributeName, String attributeValue){
        List<WeeklyTerm> newSearchResult = new ArrayList<>();
        String rebuilt = attributeValue.replace('-', ' ');


        searchResult.forEach(weeklyTerm -> {
            if (weeklyTerm.getClassroom().getAttributes().get(attributeName).getData().equals(rebuilt))
                newSearchResult.add(weeklyTerm);
        });

        searchResult = newSearchResult;
    }

    private void searchByIntegerClassroomAtttribute(String attributeName, String data, String compare){
        List<WeeklyTerm> newSearchResult = new ArrayList<>();

        searchResult.forEach(weeklyTerm -> {
            switch (compare){
                case ">" -> {
                    if(Integer.parseInt((String) weeklyTerm.getClassroom().getAttributes().get(attributeName).getData()) > Integer.parseInt(data))
                        newSearchResult.add(weeklyTerm);
                }
                case "<" -> {
                    if(Integer.parseInt((String) weeklyTerm.getClassroom().getAttributes().get(attributeName).getData()) < Integer.parseInt(data))
                        newSearchResult.add(weeklyTerm);
                }
            }
        });

        searchResult = newSearchResult;
    }



    public List<WeeklyTerm> getTerms() {
        return terms;
    }

    public LocalDate getActiveFrom() {
        return activeFrom;
    }

    public LocalDate getActiveUntil() {
        return activeUntil;
    }

    public HashMap<String, Classroom> getClassrooms() {
        return classrooms;
    }
}
