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

        organizeStudentsToClasses();

        for(int i = 0; i < 1 ; i++) {
            studentObject object = studentList.get(i);
            object.lookFurther();
            ArrayList<ClassAndTeacher[]> list = object.variations; // all the variations
            ClassAndTeacher[] var = list.get(0);
            // continues if variations does not have null
            if(!hasNull(var)) {
                object.setSchedule(var);
                // loops through the 6 classes
                for(int j = 0; j < var.length; j++) {
                    // loops through all the classes
                    for(int k = 0; k < classList.size(); k++) {
                        // looks for the class in the variation
                        if(var[j].className.equals(classList.get(k).className) && var[j].teacher.equals(classList.get(k).teacherName)) {
                            int checkPeriods = classList.get(k).whichPeriods;
                            String checkPeriodsString = Integer.toString(checkPeriods);
                            if(checkPeriodsString.contains(Integer.toString(j + 1))) { // checks if the "whichPeriods" has the period, for example there are two Algebra 2 classes but only one of them has it for period 1
                                // subtracts student
                                classList.get(k).subtractStudent(j + 1); // it is + 1 because it needs to align with the period key
                                System.out.println(classList.get(k).studentTable.get(j + 1));
                            }
                        }
                    }
                }
            }
            
            //prints all variations
            for(int j = 0; j < list.size(); j++) {
                String[] classVariations = new String[6];
                ClassAndTeacher[] classCombo = list.get(j);
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
        for(int i = 0; i < classData.size(); i += 5) {
            classObject object = new classObject(classData.get(i), Integer.parseInt(classData.get(i + 1)), Integer.parseInt(classData.get(i + 2)), Integer.parseInt(classData.get(i + 3)), classData.get(i + 4));
            classList.add(object);
        }
    }

    // Uses studentData to be put into an object where it is then organized by priority
    public static void turnToStudentObject() {
        for(int i = 0; i < studentData.size() - 1; i += 9) {
            studentObject object = new studentObject(studentData.get(i), Integer.parseInt(studentData.get(i + 1)), studentData.get(i + 2), studentData.get(i + 3), studentData.get(i + 4), studentData.get(i + 5), studentData.get(i + 6), studentData.get(i + 7), Integer.parseInt(studentData.get(i + 8)));
            studentList.add(object);
        }
        Collections.sort(studentList); // Sorts list based on priority level. References to compareTo().
    }

    // Organizes students to their schedule
    public static void organizeStudentsToClasses() {
        // loops through the amount of students
        for(int i = 0; i < studentList.size(); i++) {
            // loops through the periods 
            for(int j = 0; j < 6; j++) {
                ArrayList<classObject> classNTeachers = new ArrayList<classObject>(); // adds multiple of the same class
                // loops through all the available classes
                for(int k = 0; k < classList.size(); k++) {
                    // Checks if "classes" is equal to the "classList"
                    if(studentList.get(i).classes[j].equals(classList.get(k).className)) {
                        classNTeachers.add(classList.get(k)); // updates by adding the classes with the same name
                        studentList.get(i).addToDict(classList.get(k).className, classNTeachers);
                    }
                }
            }

            // Loops through the function that organizes each period.
            for(int j = 0; j < 6; j++) {
                studentList.get(i).createSchedule(j + 1);
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

    public Hashtable<Integer, Integer> studentTable; // which periods correlated to number of students

    classObject(String name, int numClasses, int numStudents, int periods, String teacherName) {
        this.className = name;
        this.numberOfClasses = numClasses;
        this.studentsPerClass = numStudents;
        this.whichPeriods = periods;
        this.totalStudents = numClasses * studentsPerClass;
        this.teacherName = teacherName;
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
    public String[] classes = new String[6]; // classes chosen
    public int number = 0; // when he/she did the form. For example the first person to finish the form will be 1
    public int priorityLevel = 0;
    public Hashtable<String, ArrayList> dictPeriods = new Hashtable<String, ArrayList>(6); // the Arraylist if filled with class objects

    public ArrayList<ClassAndTeacher[]> variations = new ArrayList<ClassAndTeacher[]>(); // list of classes and teachers

    public ClassAndTeacher[] schedule = new ClassAndTeacher[6];

    public studentObject(String studentName, int grade, String class1, String class2, String class3, String class4, String class5, String class6, int num) {
        this.studentName = studentName;
        this.gradeLevel = grade;
        this.classes[0] = class1;
        this.classes[1] = class2;
        this.classes[2] = class3;
        this.classes[3] = class4;
        this.classes[4] = class5;
        this.classes[5] = class6;
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
    public void addToDict(String className, ArrayList objects) {
        this.dictPeriods.put(className, objects);
    }

    public void createSchedule(int period) {
        ArrayList<ClassAndTeacher> classPeriod = new ArrayList<ClassAndTeacher>();
        // Loops each period
        for(int i = 0; i < 6; i++) {
            // Loops each class with the same name to get the "whichPeriods"
            for(int k = 0; k < this.dictPeriods.get(classes[i]).size(); k++) {
                classObject class_ = (classObject) this.dictPeriods.get(classes[i]).get(k); // gets the class object
                Integer periodsAvailable_ = class_.whichPeriods; // gets the "whichPeriods" for the class
                String periodsAvailable = periodsAvailable_.toString(); // turns it into a string
                char[] charInt = new char[periodsAvailable.length()];
                // Loops through periodsAvailable/"whichPeriods"
                for(int j = 0; j < periodsAvailable.length(); j++) {
                    charInt[j] = periodsAvailable.charAt(j); // seperate the "whichPeriods" into indiviual periods
                    if(Character.getNumericValue(charInt[j]) == period) { // checks if the individual periods are equal to the "period" I am looking for
                        if(class_.isFull()) { // checks if the class is full
                            ClassAndTeacher classNTeacher = new ClassAndTeacher(classes[i], class_.teacherName);
                            classPeriod.add(classNTeacher); 
                        }
                    }
                } 
            }
        }
        // Checks if there are any variations
        if(variations.size() != 0) {
            int size = variations.size();
            // Checks if the "classPeriod" is empty, meaning when no classes are equal to the "period" class I'm looking for
            if(!classPeriod.isEmpty()) {
                // Loops through each variation
                for(int j = 0; j < size; j++) {
                    outerloop:
                    // Loops through classes available for the "period" I am looking for
                    for(int k = 0; k < classPeriod.size(); k++) {
                        ClassAndTeacher[] var = new ClassAndTeacher[6];
                        var = variations.get(j).clone(); // returns a copy, don't use get because it affects the entire arraylist
                        var[period - 1] = classPeriod.get(k);
                        // Loops through classes in variation
                        for(int l = 0; l < period - 1; l++) {
                            // if null just continue loop, this is here to prevent any bugs
                            if(var[l] == null) {
                                continue;
                            // Checks if the class avaible for the "period" is equal to the classes in the variation
                            } else if(var[l].className.equals(classPeriod.get(k).className)) {
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
            for(int j = 0; j < classPeriod.size(); j++) {
                ClassAndTeacher[] var = new ClassAndTeacher[6];
                var[period - 1] = classPeriod.get(j);
                variations.add(var);
            }
        }
    }

    // More variations based on period availability/looks through variations with null
    public void lookFurther() {
        ArrayList<ClassAndTeacher[]> nullVariations = new ArrayList<ClassAndTeacher[]>();

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
                        for(int t = 0; t < this.dictPeriods.get(classes[k]).size(); t++) {
                            // find all the classes in that variations
                            if(variation[k] != null) {
                                classObject object = (classObject) this.dictPeriods.get(classes[k]).get(t);
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
    public void setSchedule(ClassAndTeacher[] chosenScedule) {
        this.schedule = chosenScedule;
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
