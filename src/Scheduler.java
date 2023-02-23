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
            ClassAndTeacher[] varS1 = listS1.get(0);
            // continues if variations does not have null
            if(!hasNull(varS1)) {
                object.setSchedule(varS1, "S1");
                // loops through the 6 classes
                for(int j = 0; j < varS1.length; j++) {
                    // loops through all the classes
                    for(int k = 0; k < classList.size(); k++) {
                        // looks for the class in the variation
                        if(varS1[j].className.equals(classList.get(k).className) && varS1[j].teacher.equals(classList.get(k).teacherName) && (classList.get(k).typeOfClass.equals("Y") || classList.get(k).typeOfClass.equals("S1"))) {
                            int checkPeriods = classList.get(k).whichPeriods;
                            String checkPeriodsString = Integer.toString(checkPeriods);
                            if(checkPeriodsString.contains(Integer.toString(j + 1))) { // checks if the "whichPeriods" has the period, for example there are two Algebra 2 classes but only one of them has it for period 1
                                // subtracts student
                                classList.get(k).subtractStudent(j + 1); // it is + 1 because it needs to align with the period key
                                System.out.println(classList.get(k).studentTable.get(j + 1) + classList.get(k).className + classList.get(k).whichPeriods);
                            }
                        }
                    }
                }
            }

            object.lookFurther("S2");
            ArrayList<ClassAndTeacher[]> listS2 = object.variationsS2; // all the variations
            ClassAndTeacher[] varS2 = listS2.get(0);
            // continues if variations does not have null
            if(!hasNull(varS2)) {
                object.setSchedule(varS2, "S2");
                // loops through the 6 classes
                for(int j = 0; j < varS2.length; j++) {
                    // loops through all the classes
                    for(int k = 0; k < classList.size(); k++) {
                        // looks for the class in the variation
                        if(varS2[j].className.equals(classList.get(k).className) && varS2[j].teacher.equals(classList.get(k).teacherName) && classList.get(k).typeOfClass.equals("S2")) {
                            int checkPeriods = classList.get(k).whichPeriods;
                            String checkPeriodsString = Integer.toString(checkPeriods);
                            if(checkPeriodsString.contains(Integer.toString(j + 1))) { // checks if the "whichPeriods" has the period, for example there are two Algebra 2 classes but only one of them has it for period 1
                                // subtracts student
                                classList.get(k).subtractStudent(j + 1); // it is + 1 because it needs to align with the period key
                                System.out.print(classList.get(k).studentTable.get(j + 1) + classList.get(k).className + classList.get(k).whichPeriods);
                            }
                        }
                    }
                }
            }
            
            //prints all variations
            for(int j = 0; j < listS1.size(); j++) {
                String[] classVariations = new String[6];
                ClassAndTeacher[] classCombo = listS1.get(j);
                for(int k = 0; k < classCombo.length; k++) {
                    if(classCombo[k] != null) {
                        classVariations[k] = classCombo[k].className + " " + classCombo[k].teacher;
                    } else {
                        classVariations[k] = null;
                    }
                }
                System.out.println(Arrays.toString(classVariations));
            }

            System.out.println();

            for(int j = 0; j < listS2.size(); j++) {
                String[] classVariations = new String[6];
                ClassAndTeacher[] classCombo = listS2.get(j);
                for(int k = 0; k < classCombo.length; k++) {
                    if(classCombo[k] != null) {
                        classVariations[k] = classCombo[k].className + " " + classCombo[k].teacher;
                    } else {
                        classVariations[k] = null;
                    }
                }
                System.out.println(Arrays.toString(classVariations));
            }
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

    // Organizes students to their schedule
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

    public void createSchedule(int period, String semester) {
        ArrayList<ClassAndTeacher> classPeriodS1 = new ArrayList<ClassAndTeacher>();
        ArrayList<ClassAndTeacher> classPeriodS2 = new ArrayList<ClassAndTeacher>();
        // Loops each period
        for(int i = 0; i < 6; i++) {
            if(semester.equals("S1")) {
                // Loops each class with the same name to get the "whichPeriods"
                for(int k = 0; k < this.dictPeriodsS1.get(classesS1[i]).size(); k++) {
                    classObject class_ = (classObject) this.dictPeriodsS1.get(classesS1[i]).get(k); // gets the class object
                    Integer periodsAvailable_ = class_.whichPeriods; // gets the "whichPeriods" for the class
                    String periodsAvailable = periodsAvailable_.toString(); // turns it into a string
                    char[] charInt = new char[periodsAvailable.length()];
                    // Loops through periodsAvailable/"whichPeriods"
                    for(int j = 0; j < periodsAvailable.length(); j++) {
                        charInt[j] = periodsAvailable.charAt(j); // seperate the "whichPeriods" into indiviual periods
                        if(Character.getNumericValue(charInt[j]) == period) { // checks if the individual periods are equal to the "period" I am looking for
                            if(class_.isFull()) { // checks if the class is full
                                ClassAndTeacher classNTeacher = new ClassAndTeacher(classesS1[i], class_.teacherName);
                                classPeriodS1.add(classNTeacher); 
                            }
                        }
                    } 
                }
            } else if(semester.equals("S2")) {
                // Loops each class with the same name to get the "whichPeriods"
                for(int k = 0; k < this.dictPeriodsS2.get(classesS2[i]).size(); k++) {
                    classObject class_ = (classObject) this.dictPeriodsS2.get(classesS2[i]).get(k); // gets the class object
                    Integer periodsAvailable_ = class_.whichPeriods; // gets the "whichPeriods" for the class
                    String periodsAvailable = periodsAvailable_.toString(); // turns it into a string
                    char[] charInt = new char[periodsAvailable.length()];
                    // Loops through periodsAvailable/"whichPeriods"
                    for(int j = 0; j < periodsAvailable.length(); j++) {
                        charInt[j] = periodsAvailable.charAt(j); // seperate the "whichPeriods" into indiviual periods
                        if(Character.getNumericValue(charInt[j]) == period) { // checks if the individual periods are equal to the "period" I am looking for
                            if(class_.isFull()) { // checks if the class is full
                                ClassAndTeacher classNTeacher = new ClassAndTeacher(classesS2[i], class_.teacherName);
                                classPeriodS2.add(classNTeacher); 
                            }
                        }
                    } 
                }
            }
        }
        if(semester.equals("S1")) {
            // Checks if there are any variations
            if(variationsS1.size() != 0) {
                int size = variationsS1.size();
                // Checks if the "classPeriod" is empty, meaning when no classes are equal to the "period" class I'm looking for
                if(!classPeriodS1.isEmpty()) {
                    // Loops through each variation
                    for(int j = 0; j < size; j++) {
                        outerloop:
                        // Loops through classes available for the "period" I am looking for
                        for(int k = 0; k < classPeriodS1.size(); k++) {
                            ClassAndTeacher[] var = new ClassAndTeacher[6];
                            var = variationsS1.get(j).clone(); // returns a copy, don't use get because it affects the entire arraylist
                            var[period - 1] = classPeriodS1.get(k);
                            // Loops through classes in variation
                            for(int l = 0; l < period - 1; l++) {
                                // if null just continue loop, this is here to prevent any bugs
                                if(var[l] == null) {
                                    continue;
                                // Checks if the class avaible for the "period" is equal to the classes in the variation
                                } else if(var[l].className.equals(classPeriodS1.get(k).className)) {
                                    continue outerloop;
                                }
                            }
                            variationsS1.add(var); // variation with "period" class I'm looking for
                        }       
                    }
                } else {
                    for(int j = 0; j < size; j++) {
                        ClassAndTeacher[] var = new ClassAndTeacher[6];
                        var = variationsS1.get(j).clone(); // returns a copy, don't use get because it affects the entire arraylist
                        var[period - 1] = null;
                        variationsS1.add(var); // variation with "period" class I'm looking for
                    }
                }
    
                // Checks if the size is the same. The if statement is here to prevent the variations having 0 elements
                if(variationsS1.size() != size) {
                    // Loops through old variations and removes it from "variations"
                    for(int j = 0; j < size; j++) {
                        variationsS1.remove(0);
                    }
                }
    
            } else {
                // Adds to "variations"
                for(int j = 0; j < classPeriodS1.size(); j++) {
                    ClassAndTeacher[] var = new ClassAndTeacher[6];
                    var[period - 1] = classPeriodS1.get(j);
                    variationsS1.add(var);
                }
            }            
        } else if(semester.equals("S2")) {
            // Checks if there are any variations
            if(variationsS2.size() != 0) {
                int size = variationsS2.size();
                // Checks if the "classPeriod" is empty, meaning when no classes are equal to the "period" class I'm looking for
                if(!classPeriodS2.isEmpty()) {
                    // Loops through each variation
                    for(int j = 0; j < size; j++) {
                        outerloop:
                        // Loops through classes available for the "period" I am looking for
                        for(int k = 0; k < classPeriodS2.size(); k++) {
                            ClassAndTeacher[] var = new ClassAndTeacher[6];
                            var = variationsS2.get(j).clone(); // returns a copy, don't use get because it affects the entire arraylist
                            var[period - 1] = classPeriodS2.get(k);
                            // Loops through classes in variation
                            for(int l = 0; l < period - 1; l++) {
                                // if null just continue loop, this is here to prevent any bugs
                                if(var[l] == null) {
                                    continue;
                                // Checks if the class avaible for the "period" is equal to the classes in the variation
                                } else if(var[l].className.equals(classPeriodS2.get(k).className)) {
                                    continue outerloop;
                                }
                            }
                            variationsS2.add(var); // variation with "period" class I'm looking for
                        }       
                    }
                } else {
                    for(int j = 0; j < size; j++) {
                        ClassAndTeacher[] var = new ClassAndTeacher[6];
                        var = variationsS2.get(j).clone(); // returns a copy, don't use get because it affects the entire arraylist
                        var[period - 1] = null;
                        variationsS2.add(var); // variation with "period" class I'm looking for
                    }
                }

                // Checks if the size is the same. The if statement is here to prevent the variations having 0 elements
                if(variationsS2.size() != size) {
                    // Loops through old variations and removes it from "variations"
                    for(int j = 0; j < size; j++) {
                        variationsS2.remove(0);
                    }
                }

            } else {
                // Adds to "variations"
                for(int j = 0; j < classPeriodS2.size(); j++) {
                    ClassAndTeacher[] var = new ClassAndTeacher[6];
                    var[period - 1] = classPeriodS2.get(j);
                    variationsS2.add(var);
                }
            }
        }
    }

    // More variations based on period availability/looks through variations with null
    public void lookFurther(String semester) {
        ArrayList<ClassAndTeacher[]> nullVariationsS1 = new ArrayList<ClassAndTeacher[]>();
        ArrayList<ClassAndTeacher[]> nullVariationsS2 = new ArrayList<ClassAndTeacher[]>();

        if(semester.equals("S1")) {
            // loops through each variaton
            for(int i = 0; i < variationsS1.size(); i++) {
                ClassAndTeacher[] variation = variationsS1.get(i);
                // loops through a variation
                for(int j = 0; j < 6; j++) {
                    // finds the null value
                    if(variation[j] == null) {
                        int place = j + 1;
                        // loops through variation again by period
                        for(int k = 0; k < 6; k++) {
                            // Loops each class with the same name
                            for(int t = 0; t < this.dictPeriodsS1.get(classesS1[k]).size(); t++) {
                                // find all the classes in that variations
                                if(variation[k] != null) {
                                    classObject object = (classObject) this.dictPeriodsS1.get(classesS1[k]).get(t);
                                    Integer num = object.whichPeriods;
                                    String nums = num.toString();
                                    // loops through the "whichPeriods"
                                    for(int l = 0; l < nums.length(); l++) {
                                        // Checks if the "whichPeriods" is equal to the place where the null was
                                        if(Character.getNumericValue(nums.charAt(l)) == place) {
                                            ClassAndTeacher[] newVariation = variation.clone();
                                            newVariation[k] = null;
                                            newVariation[j] = new ClassAndTeacher(object.className, object.teacherName);
                                            nullVariationsS1.add(newVariation); // adds to nullVariations bcs if variations the loop will be infinite as more variations will be added with null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            variationsS1.addAll(nullVariationsS1); // combines all the null variations with the variations list
        } else if (semester.equals("S2")) {
            // loops through each variaton
            for(int i = 0; i < variationsS2.size(); i++) {
                ClassAndTeacher[] variation = variationsS2.get(i);
                // loops through a variation
                for(int j = 0; j < 6; j++) {
                    // finds the null value
                    if(variation[j] == null) {
                        int place = j + 1;
                        // loops through variation again by period
                        for(int k = 0; k < 6; k++) {
                            // Loops each class with the same name
                            for(int t = 0; t < this.dictPeriodsS2.get(classesS2[k]).size(); t++) {
                                // find all the classes in that variations
                                if(variation[k] != null) {
                                    classObject object = (classObject) this.dictPeriodsS2.get(classesS2[k]).get(t);
                                    Integer num = object.whichPeriods;
                                    String nums = num.toString();
                                    // loops through the "whichPeriods"
                                    for(int l = 0; l < nums.length(); l++) {
                                        // Checks if the "whichPeriods" is equal to the place where the null was
                                        if(Character.getNumericValue(nums.charAt(l)) == place) {
                                            ClassAndTeacher[] newVariation = variation.clone();
                                            newVariation[k] = null;
                                            newVariation[j] = new ClassAndTeacher(object.className, object.teacherName);
                                            nullVariationsS2.add(newVariation); // adds to nullVariations bcs if variations the loop will be infinite as more variations will be added with null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            variationsS2.addAll(nullVariationsS2); // combines all the null variations with the variations list
        }
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
