import java.io.File;
import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.*;
public class Scheduler {
    public static ArrayList<String> classData = new ArrayList<String>();
    public static ArrayList<classObject> classList = new ArrayList<classObject>();

    public static ArrayList<String> studentData = new ArrayList<String>();
    public static ArrayList<studentObject> studentList = new ArrayList<studentObject>();

    public static void main(String[] args) {
        // Scanner console = new Scanner(System.in);
        // askForClasses(console);
        classData = grabDataFromExcelFile("classlist.xlsx");
        turnToClassObject();

        studentData = grabDataFromExcelFile("studentlist.xlsx");
        turnToStudentObject();

        organizeStudentsToClasses("S1");
        organizeStudentsToClasses("S2");

        for(int i = 0; i < 1 ; i++) {
            studentObject object = studentList.get(i);
            object.lookFurther("S1");
            ArrayList<ClassAndTeacher[]> listS1 = object.variationsS1; // all the variations

            object.lookFurther("S2");
            ArrayList<ClassAndTeacher[]> listS2 = object.variationsS2; // all the variations

            findBestVariations(listS1, listS2);

            ClassAndTeacher[] varS1 = listS1.get(listS1.size() - 1); // gets last item of the list
            ClassAndTeacher[] varS2 = listS2.get(listS2.size() - 1); // gets last item of the list
            
            printVariation(varS1);
            printVariation(varS2);

            subtractTotalStudentsFromStudentSchedule(object, varS1, "S1");
            subtractTotalStudentsFromStudentSchedule(object, varS2, "S2");

            printVariations(listS1);
            System.out.println();
            printVariations(listS2);
        }
    }

    // Grabs data from excel file
    // https://www.youtube.com/watch?v=xabbFBBn6T8&ab_channel=ChargeAhead
    public static ArrayList<String> grabDataFromExcelFile(String path) {
        ArrayList<String> dataList = new ArrayList<>();
        try {
            FileInputStream file = new FileInputStream(new File(path));
            XSSFWorkbook workBook = new XSSFWorkbook(file);
            XSSFSheet sheet = workBook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();
            Iterator<Row> row = sheet.iterator();
            while(row.hasNext()){
                Row nrow = row.next();
                Iterator<Cell> cell = nrow.iterator();
                while(cell.hasNext()) {
                    Cell ncell = cell.next();
                    String value = dataFormatter.formatCellValue(ncell);
                    dataList.add(value);
                }
            }
            workBook.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }

    // Uses classData to be put into an object
    public static void turnToClassObject() {
        for(int i = 0; i < classData.size(); i += 6) {
            classObject object = new classObject(classData.get(i), 
                Integer.parseInt(classData.get(i + 1)), Integer.parseInt(classData.get(i + 2)), 
                Integer.parseInt(classData.get(i + 3)), classData.get(i + 4), classData.get(i + 5));
            classList.add(object);
        }
    }

    // Uses studentData to be put into an object where it is then organized by priority
    public static void turnToStudentObject() {
        for(int i = 0; i < studentData.size() - 1; i += 15) {
            studentObject object = new studentObject(studentData.get(i), 
                Integer.parseInt(studentData.get(i + 1)), studentData.get(i + 2),   
                studentData.get(i + 3), studentData.get(i + 4), studentData.get(i + 5), 
                studentData.get(i + 6), studentData.get(i + 7), studentData.get(i + 8),
                studentData.get(i + 9), studentData.get(i + 10), studentData.get(i + 11),
                studentData.get(i + 12), studentData.get(i + 13), 
                Integer.parseInt(studentData.get(i + 14)));
            studentList.add(object);
        }
        Collections.sort(studentList); // Sorts list based on priority level. References to compareTo().
    }

    // Creates student list and class list. In addition it also gives each student a variations of schedules
    public static void organizeStudentsToClasses(String semester) {
        // loops through the amount of students
        for(int i = 0; i < studentList.size(); i++) {
            // loops through the periods 
            for(int j = 0; j < 6; j++) {
                ArrayList<classObject> classNTeachers = new ArrayList<classObject>(); // adds multiple of the same class
                // loops through all the available classes
                for(int k = 0; k < classList.size(); k++) {
                    if(semester.equals("S1")) {
                        // Checks if "classes" is equal to the "classList"
                        if(studentList.get(i).classesS1[j].equals(classList.get(k).className) && (classList.get(k).typeOfClass.equals("S1") || classList.get(k).typeOfClass.equals("Y"))) {
                            classNTeachers.add(classList.get(k)); // updates by adding the classes with the same name
                            studentList.get(i).addToDict(classList.get(k).className, classNTeachers, "S1");
                        }
                    } else if(semester.equals("S2")) {
                        if(studentList.get(i).classesS2[j].equals(classList.get(k).className) && (classList.get(k).typeOfClass.equals("S2") || classList.get(k).typeOfClass.equals("Y"))) {
                            classNTeachers.add(classList.get(k)); // updates by adding the classes with the same name
                            studentList.get(i).addToDict(classList.get(k).className, classNTeachers, "S2");
                        }
                    }
                }
            }

            // Loops through the function that organizes each period.
            for(int j = 0; j < 6; j++) {
                studentList.get(i).createSchedule(j + 1, semester);
            }
        }
    }

    // checks if variation has null in it
    public static boolean hasNull(ClassAndTeacher[] variation) {
        for(int i = 0; i < variation.length; i++) {
            if(variation[i] == null) {
                return true;
            }
        }
        return false;
    }

    // The chosen schedule for that student will be used to subtract the total amount of slots available for that class
    public static void subtractTotalStudentsFromStudentSchedule(studentObject student, ClassAndTeacher[] schedule, String semester) {
        if(semester.equals("S1")) {
            // checks if null
            if(!hasNull(schedule)) {
                student.setSchedule(schedule, semester);
                // loops through the 6 classes
                for(int i = 0; i < schedule.length; i++) {
                    // loops through all the classes
                    for(int j = 0; j < classList.size(); j++) {
                        // looks for the class in the variation
                        if(schedule[i].className.equals(classList.get(j).className) && schedule[i].teacher.equals(classList.get(j).teacherName) && (classList.get(j).typeOfClass.equals("Y") || classList.get(j).typeOfClass.equals(semester))) {
                            int checkPeriods = classList.get(j).whichPeriods;
                            String checkPeriodsString = Integer.toString(checkPeriods);
                            if(checkPeriodsString.contains(Integer.toString(i + 1))) { // checks if the "whichPeriods" has the period, for example there are two Algebra 2 classes but only one of them has it for period 1
                                // subtracts student
                                classList.get(j).subtractStudent(i + 1); // it is + 1 because it needs to align with the period key
                                System.out.println(classList.get(j).studentTable.get(i + 1) + " " + classList.get(j).className + " " + classList.get(j).whichPeriods);
                            }
                        }
                    }
                }
            }
        } else if(semester.equals("S2")) {
            if(!hasNull(schedule)) {
                student.setSchedule(schedule, semester);
                // loops through the 6 classes
                for(int i = 0; i < schedule.length; i++) {
                    // loops through all the classes
                    for(int j = 0; j < classList.size(); j++) {
                        // looks for the class in the variation
                        if(schedule[i].className.equals(classList.get(j).className) && schedule[i].teacher.equals(classList.get(j).teacherName) && (classList.get(j).typeOfClass.equals(semester))) {
                            int checkPeriods = classList.get(j).whichPeriods;
                            String checkPeriodsString = Integer.toString(checkPeriods);
                            if(checkPeriodsString.contains(Integer.toString(i + 1))) { // checks if the "whichPeriods" has the period, for example there are two Algebra 2 classes but only one of them has it for period 1
                                // subtracts student
                                classList.get(j).subtractStudent(i + 1); // it is + 1 because it needs to align with the period key
                                System.out.println(classList.get(j).studentTable.get(i + 1) + " " + classList.get(j).className + " " + classList.get(j).whichPeriods);
                            }
                        }
                    }
                }
            }
        }
    }

    // looks for the best variations by comparing the list from variationsS1 and variationS2 based on class and teacher
    public static void findBestVariations(ArrayList<ClassAndTeacher[]> variationsS1, ArrayList<ClassAndTeacher[]> variationsS2) {
        int sizeS1 = variationsS1.size();
        int sizeS2 = variationsS2.size();
        int HighestSimilarityScore = 0;
        int count = 0; // use to keep track of similarity score
        // loops through variationS1
        for(int i = 0; i < sizeS1; i++) {
            // loops through variationS2
            for(int j = 0; j < sizeS2; j++) {
                count = 0;
                // loops through period
                for(int k = 0; k < 6; k++) {
                    // checks class name and class teacher
                    if(variationsS1.get(i)[k].className == variationsS2.get(j)[k].className && variationsS1.get(i)[k].teacher == variationsS2.get(j)[k].teacher) {
                        count++;
                    }
                }
                // checks if similarity score is better than the best score
                // The first couple in the variations list will be bad
                if(count >= HighestSimilarityScore) {
                    variationsS1.add(variationsS1.get(i));
                    variationsS2.add(variationsS2.get(j));
                    HighestSimilarityScore = count;
                }
            }
        }
        // removes old variationS1 variations
        for(int i = 0; i < sizeS1; i++) {
            variationsS1.remove(0);
        }
        // removes old variationS2 variations
        for(int i = 0; i < sizeS2; i++) {
            variationsS2.remove(0);
        }
    }

    // prints variations
    public static void printVariations(ArrayList<ClassAndTeacher[]> variations) {
        for(int i = 0; i < variations.size(); i++) {
            String[] classVariations = new String[6];
            ClassAndTeacher[] classCombo = variations.get(i);
            for(int j = 0; j < classCombo.length; j++) {
                if(classCombo[j] != null) {
                    classVariations[j] = classCombo[j].className + " " + classCombo[j].teacher;
                } else {
                    classVariations[j] = null;
                }
            }
            System.out.println(Arrays.toString(classVariations));
        }
    }

    // prints a single variation
    public static void printVariation(ClassAndTeacher[] variation) {
        String[] stringVariation = new String[6];
        for(int i = 0; i < variation.length; i++) {
            stringVariation[i] = variation[i].className + " " + variation[i].teacher;
        }
        System.out.println(Arrays.toString(stringVariation));
    }
}

// Class object
class classObject {
    public String className = "";
    public int numberOfClasses = 0;
    public int studentsPerClass = 0;
    public int whichPeriods = 0; // periods are based on number. For example, 126 represents periods 1, 2, and 6
    public int totalStudents = 0;
    public String teacherName = "";

    public String typeOfClass = "";

    public Hashtable<Integer, Integer> studentTable; // which periods correlated to number of students

    classObject(String name, int numClasses, int numStudents, int periods, String teacherName, String typeOfClass) {
        this.className = name;
        this.numberOfClasses = numClasses;
        this.studentsPerClass = numStudents;
        this.whichPeriods = periods;
        this.totalStudents = numClasses * studentsPerClass;
        this.teacherName = teacherName;

        this.typeOfClass = typeOfClass;

        this.studentTable = new Hashtable<Integer, Integer>(numClasses);

        // fills in "studentTable" and assigns amount of students each period
        Integer num = periods;
        String nums = num.toString();
        for(int i = 0; i < numClasses; i++) {
            this.studentTable.put(Character.getNumericValue(nums.charAt(i)), numStudents);
        }
    }

    // checks if class is full
    public boolean isFull() {
        if(studentsPerClass == 0) {
            return false;
        } else {
            return true;
        }
    }

    // subtracts the amount of students based on period
    public void subtractStudent(int period) {
        this.studentTable.replace(period, this.studentTable.get(period) - 1); // need to figure out how to check the correct class using the right teacher
    }
}

// Student object
class studentObject implements Comparable{
    public String studentName = "";
    public int gradeLevel = 9;
    public String[] classesS1 = new String[6]; // classes chosen
    public String[] classesS2 = new String[6];
    public int number = 0; // when he/she did the form. For example the first person to finish the form will be 1
    public int priorityLevel = 0;
    public Hashtable<String, ArrayList> dictPeriodsS1 = new Hashtable<String, ArrayList>(6); // the Arraylist if filled with class objects
    public Hashtable<String, ArrayList> dictPeriodsS2 = new Hashtable<String, ArrayList>(6);
    public ArrayList<ClassAndTeacher[]> variationsS1 = new ArrayList<ClassAndTeacher[]>(); // list of classes and teachers
    public ArrayList<ClassAndTeacher[]> variationsS2 = new ArrayList<ClassAndTeacher[]>(); // list of classes and teachers
    public ClassAndTeacher[] scheduleS1 = new ClassAndTeacher[6];
    public ClassAndTeacher[] scheduleS2 = new ClassAndTeacher[6];

    public studentObject(String studentName, int grade, String class1S1, String class2S1, 
        String class3S1, String class4S1, String class5S1, String class6S1, String class1S2,
        String class2S2, String class3S2, String class4S2, String class5S2, String class6S2, int num) {
        this.studentName = studentName;
        this.gradeLevel = grade;
        this.classesS1[0] = class1S1;
        this.classesS1[1] = class2S1;
        this.classesS1[2] = class3S1;
        this.classesS1[3] = class4S1;
        this.classesS1[4] = class5S1;
        this.classesS1[5] = class6S1;

        this.classesS2[0] = class1S2;
        this.classesS2[1] = class2S2;
        this.classesS2[2] = class3S2;
        this.classesS2[3] = class4S2;
        this.classesS2[4] = class5S2;
        this.classesS2[5] = class6S2;

        this.number = num;
        this.priorityLevel = givePriority();
    }

    // Caculates priority
    public int givePriority() {
        if(gradeLevel == 12) {
            return gradeLevel + number;
        } else if(gradeLevel == 11) {
            return gradeLevel + number + 1000;
        } else if(gradeLevel == 10) {
            return gradeLevel + number + 1000000;
        } else {
            return gradeLevel + number + 1000000000;
        }
    }

    // Returns priority level
    public int getPriorityLevel() {
        return priorityLevel;
    }

    // Sets all the classes into a hash table based on "classes"
    public void addToDict(String className, ArrayList objects, String semester) {
        if(semester.equals("S1")) {
            this.dictPeriodsS1.put(className, objects);
        } else if(semester.equals("S2")) {
            this.dictPeriodsS2.put(className, objects);
        }
    }

    // Creates the student's schedule variations for semester 1 and 2
    public void createSchedule(int period, String semester) {
        ArrayList<ClassAndTeacher> classPeriodS1 = new ArrayList<ClassAndTeacher>();
        ArrayList<ClassAndTeacher> classPeriodS2 = new ArrayList<ClassAndTeacher>();
        // Loops each period
        for(int i = 0; i < 6; i++) {
            if(semester.equals("S1")) {
                updateAvailableClassesForThePeriod(dictPeriodsS1, classPeriodS1, classesS1, period, i);
            } else if(semester.equals("S2")) {
                updateAvailableClassesForThePeriod(dictPeriodsS2, classPeriodS2, classesS2, period, i);
            }
        }
        if(semester.equals("S1")) {
            createVariations(variationsS1, classPeriodS1, period);     
        } else if(semester.equals("S2")) {
            createVariations(variationsS2, classPeriodS2, period);  
        }
    }

    // Creates a list of available classes based on whichPeriods and if the class is full in that period
    public void updateAvailableClassesForThePeriod(Hashtable<String, ArrayList> dictOfClasses, ArrayList<ClassAndTeacher> classesForPerSem, String[] StudChosenClassesForSem, int period, int increment) {
        // Loops each class with the same name to get the "whichPeriods"
        for(int i = 0; i < dictOfClasses.get(StudChosenClassesForSem[increment]).size(); i++) {
            classObject class_ = (classObject) dictOfClasses.get(StudChosenClassesForSem[increment]).get(i); // gets the class object
            Integer periodsAvailable_ = class_.whichPeriods; // gets the "whichPeriods" for the class
            String periodsAvailable = periodsAvailable_.toString(); // turns it into a string
            char[] charInt = new char[periodsAvailable.length()];
            // Loops through periodsAvailable/"whichPeriods"
            for(int j = 0; j < periodsAvailable.length(); j++) {
                charInt[j] = periodsAvailable.charAt(j); // seperate the "whichPeriods" into indiviual periods
                if(Character.getNumericValue(charInt[j]) == period) { // checks if the individual periods are equal to the "period" I am looking for
                    if(class_.isFull()) { // checks if the class is full
                        ClassAndTeacher classNTeacher = new ClassAndTeacher(StudChosenClassesForSem[increment], class_.teacherName);
                        classesForPerSem.add(classNTeacher); 
                    }
                }
            } 
        }
    }

    // Creates variations of classes
    public void createVariations(ArrayList<ClassAndTeacher[]> variations, ArrayList<ClassAndTeacher> classesForPerSem, int period) {
        // Checks if there are any variations
        if(variations.size() != 0) {
            int size = variations.size();
            // Checks if the "classPeriod" is empty, meaning when no classes are equal to the "period" class I'm looking for
            if(!classesForPerSem.isEmpty()) {
                // Loops through each variation
                for(int j = 0; j < size; j++) {
                    outerloop:
                    // Loops through classes available for the "period" I am looking for
                    for(int k = 0; k < classesForPerSem.size(); k++) {
                        ClassAndTeacher[] var = new ClassAndTeacher[6];
                        var = variations.get(j).clone(); // returns a copy, don't use get because it affects the entire arraylist
                        var[period - 1] = classesForPerSem.get(k);
                        // Loops through classes in variation
                        for(int l = 0; l < period - 1; l++) {
                            // if null just continue loop, this is here to prevent any bugs
                            if(var[l] == null) {
                                continue;
                            // Checks if the class avaible for the "period" is equal to the classes in the variation
                            } else if(var[l].className.equals(classesForPerSem.get(k).className)) {
                                continue outerloop;
                            }
                        }
                        variations.add(var); // variation with "period" class I'm looking for
                    }       
                }
            } else {
                for(int j = 0; j < size; j++) {
                    ClassAndTeacher[] var = new ClassAndTeacher[6];
                    var = variations.get(j).clone(); // returns a copy, don't use get because it affects the entire arraylist
                    var[period - 1] = null;
                    variations.add(var); // variation with "period" class I'm looking for
                }
            }

            // Checks if the size is the same. The if statement is here to prevent the variations having 0 elements
            if(variations.size() != size) {
                // Loops through old variations and removes it from "variations"
                for(int j = 0; j < size; j++) {
                    variations.remove(0);
                }
            }

        } else {
            // Adds to "variations"
            for(int j = 0; j < classesForPerSem.size(); j++) {
                ClassAndTeacher[] var = new ClassAndTeacher[6];
                var[period - 1] = classesForPerSem.get(j);
                variations.add(var);
            }
        }   
    }

    // More variations based on period availability/looks through variations with null
    public void lookFurther(String semester) {
        ArrayList<ClassAndTeacher[]> nullVariationsS1 = new ArrayList<ClassAndTeacher[]>();
        ArrayList<ClassAndTeacher[]> nullVariationsS2 = new ArrayList<ClassAndTeacher[]>();

        if(semester.equals("S1")) {
            createMoreVariationsOfNullElements(variationsS1, nullVariationsS1, dictPeriodsS1, classesS1);
        } else if (semester.equals("S2")) {
            createMoreVariationsOfNullElements(variationsS2, nullVariationsS2, dictPeriodsS2, classesS2);
        }
    }

    // Creates more variations that have null elements in them
    public void createMoreVariationsOfNullElements(ArrayList<ClassAndTeacher[]> variations, ArrayList<ClassAndTeacher[]> nullVariations, Hashtable<String, ArrayList> dictOfClasses, String[] StudChosenClassesForSem) {
        // loops through each variaton
        for(int i = 0; i < variations.size(); i++) {
            ClassAndTeacher[] variation = variations.get(i);
            // loops through a variation
            for(int j = 0; j < 6; j++) {
                // finds the null value
                if(variation[j] == null) {
                    int place = j + 1;
                    // loops through variation again by period
                    for(int k = 0; k < 6; k++) {
                        // Loops each class with the same name
                        for(int t = 0; t < dictOfClasses.get(StudChosenClassesForSem).size(); t++) {
                            // find all the classes in that variations
                            if(variation[k] != null) {
                                classObject object = (classObject) dictOfClasses.get(StudChosenClassesForSem[k]).get(t);
                                Integer num = object.whichPeriods;
                                String nums = num.toString();
                                // loops through the "whichPeriods"
                                for(int l = 0; l < nums.length(); l++) {
                                    // Checks if the "whichPeriods" is equal to the place where the null was
                                    if(Character.getNumericValue(nums.charAt(l)) == place) {
                                        ClassAndTeacher[] newVariation = variation.clone();
                                        newVariation[k] = null;
                                        newVariation[j] = new ClassAndTeacher(object.className, object.teacherName);
                                        nullVariations.add(newVariation); // adds to nullVariations bcs if variations the loop will be infinite as more variations will be added with null
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        variations.addAll(nullVariations); // combines all the null variations with the variations list
    }

    // set schedule for student
    public void setSchedule(ClassAndTeacher[] chosenScedule, String semester) {
        if(semester.equals("S1")) {
            this.scheduleS1 = chosenScedule;
        } else if(semester.equals("S2")) {
            this.scheduleS2 = chosenScedule;
        }
    }

    // Compares objects
    @Override
    public int compareTo(Object student) {
        int comparePriority = ((studentObject) student).getPriorityLevel();
        return  this.priorityLevel - comparePriority;
    }
}

// struct, just stores two values in one object
class ClassAndTeacher {
    String className;
    String teacher;
    public ClassAndTeacher(String className, String teacher) {
        this.className = className;
        this.teacher = teacher;
    }
}
