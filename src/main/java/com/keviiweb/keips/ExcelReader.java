package com.keviiweb.keips;

import static com.keviiweb.keips.StudentManager.EXCELSHEET_MATRIC_INDEX;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * Hello world!
 *
 * Code adapted from: https://www.mkyong.com/java/apache-poi-reading-and-writing-excel-file-in-java/
 * and https://poi.apache.org/components/spreadsheet/quick-guide.html
 */
public class ExcelReader {
    private FileInputStream excelFile;
    private Workbook workbook;
    private DataFormatter formatter;
    private StudentManager manager;

    public static final String MASTER_FILE = "READDATA.txt";
    private static final String ROOM_DRAW_FILE = "roomdraw_points_ay1819.xlsx";
    public static final String DIRECTORY_SHEET = "sheets/";
    public static final int EXCELSHEET_MAIN_INDEX = 0;
    public static final int EXCELSHEET_BONUS_INDEX = 1;


    public static void main(String[] args) {

        ExcelReader newReader = new ExcelReader();
        newReader.run();

    }

    private void run() {
        Scanner sc = new Scanner(System.in);
        boolean isResidentList = true;
        this.manager = new StudentManager();

        while (true) {
            String fileName = sc.nextLine();
            System.out.println("User input: " + fileName);

            //terminate if user input is -1
            if (fileName.equals("-1")) {
                System.out.println("Terminating program ...");
                return;
            }

            //all in one method to read all files
            if (fileName.equals("readallfiles")) {

                try {
                    File masterfile = new File(DIRECTORY_SHEET + MASTER_FILE);
                    FileReader reader = new FileReader(masterfile);
                    BufferedReader br = new BufferedReader(reader);
                    String s = br.readLine();
                    parseResidentList(DIRECTORY_SHEET + s);
                    parseRoomDrawFile(DIRECTORY_SHEET + ROOM_DRAW_FILE);
                    s = br.readLine();
                    while (s != null) {
                        parseFile(DIRECTORY_SHEET +s);
                        s = br.readLine();
                    }
                    continue;
                } catch (IOException e) {
                    System.out.println("Cannot find " + DIRECTORY_SHEET + MASTER_FILE);
                    continue;
                }
            }

            //print out to console
            if (fileName.equals("print")) {
                manager.printStudentList();
                continue;
            }

            //print to a json object and file
            if (fileName.equals("printtojson")) {
                String json = manager.printasjson();
                //System.out.println(json);

                try {
                    Files.write(Paths.get("output.json"), json.getBytes());
                } catch (IOException e) {
                    System.err.println(e);
                }

                continue;
            }

            //creates a new excel file called output.xlsx and prints out the students total points
            if (fileName.equals("printtoexcel")) {
                try {
                    FileOutputStream outFile = new FileOutputStream(new File("output.xlsx"));
                    Workbook outWorkbook = new XSSFWorkbook();
                    Sheet sheet = outWorkbook.createSheet();
                    List<Student> sortedList = manager.getSortedList();
                    for (int i = 0; i < sortedList.size(); i++) {
                        addStudentToSheet(sortedList.get(i), sheet, i);
                    }
                    outWorkbook.write(outFile);
                    workbook.close();
                    outFile.close();
                } catch (FileNotFoundException e) {
                    System.out.println(e);
                } catch (IOException e) {
                    System.out.println(e);
                }
                continue;
            }
            fileName = DIRECTORY_SHEET + fileName;
            //first file is the resident list
            if (isResidentList) {

                isResidentList = false;
                if (!parseResidentList(fileName)) {
                    System.out.println("Cannot find resident list file.");
                    return;
                }
                continue;
            }

            if (!parseFile(fileName)) {
                System.out.println("Error parsing file.");
            } else {
                System.out.println("Successfully parsed file.");
            }
        }
    }

    private boolean parseResidentList(String fileName) {
        try {
            excelFile = new FileInputStream(new File(fileName));
            workbook = WorkbookFactory.create(excelFile);
            formatter = new DataFormatter();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("Parsing resident list...");
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        if (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();

            while (rowIterator.hasNext()) {
                Row currentRow = rowIterator.next();
                //create an iterator for the row
                Iterator<Cell> cellIterator = currentRow.iterator();
                List<String> nameRow = new ArrayList<>();

                while (cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();
                    String cellText = formatter.formatCellValue(currentCell);
                    nameRow.add(cellText);
                }

                parseNewStudent(nameRow);
            }

        }

        try {
            excelFile.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return true;
    }

    private void parseNewStudent(List<String> row) {
        String matric = row.get(StudentManager.RESIDENT_LIST_MATRIC_INDEX);
        String magicNumber = row.get(StudentManager.RESIDENT_LIST_MAGICNUMBER_INDEX);
        String studentName = row.get(StudentManager.RESIDENT_LIST_NAME_INDEX);
        String gender = row.get(StudentManager.RESIDENT_LIST_GENDER_INDEX);
        String semester = row.get(StudentManager.RESIDENT_LIST_SEMESTER_INDEX);

        Student newStudent = new Student(matric, magicNumber, studentName, gender, semester);

         if (manager.addToStudentList(newStudent)) {
             System.out.println("added new student: " + magicNumber + " name = " + studentName + " matric = "
                     + " gender = " + gender);
         } else {
             System.out.println("Encountered duplicate student: " + matric);
         }
    }

    //takes in as input the filename and parses the file
    private boolean parseFile(String fileName) {
        //set up the excelfile
        try {
            excelFile = new FileInputStream(new File(fileName));
            workbook = WorkbookFactory.create(excelFile);
            formatter = new DataFormatter();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("Parsing sheet: " + fileName);
        Iterator<Sheet> sheetiterator = workbook.sheetIterator();

        //parse the first sheet of data
        if (sheetiterator.hasNext()) {
            if (!parseSheet(sheetiterator.next(), EXCELSHEET_MAIN_INDEX)) {
                System.out.println("Error parsing main sheet.");
                return false;
            } else {
                System.out.println("successfully parsed main sheet.");
            }
        }
        //parse the second sheet of data
        if (sheetiterator.hasNext()) {
            if (!parseSheet(sheetiterator.next(), EXCELSHEET_BONUS_INDEX)) {
                System.out.println("Error parsing bonus sheet.");
                return false;
            } else {
                System.out.println("successfully parsed bonus sheet.");
            }

        }

        try {
            excelFile.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        return true;
    }

    //takes in as input a sheet and parses it
    private boolean parseSheet(Sheet sheet, int sheetNumber) {
        Iterator<Row> iterator = sheet.iterator();
        iterator.next();

        while (iterator.hasNext()) {
            Row currentRow = iterator.next();
            //create an iterator for the row
            Iterator<Cell> cellIterator = currentRow.iterator();
            List<String> nameRow = new ArrayList<>();

            while (cellIterator.hasNext()) {
                Cell currentCell = cellIterator.next();
                String cellText = formatter.formatCellValue(currentCell);
                // stops reading when cell is marked
                if (cellText.equals("done")) {
                    return true;
                }
                nameRow.add(cellText);
            }
            if (nameRow.size() > 2) {
                manager.ProcessStudent(nameRow, sheetNumber);
            }

        }
        return true;
    }

    private boolean parseRoomDrawFile(String fileName) {
        try {
            excelFile = new FileInputStream(new File(fileName));
            workbook = WorkbookFactory.create(excelFile);
            formatter = new DataFormatter();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("Parsing room draw file: " + fileName);
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        if (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();

            while (rowIterator.hasNext()) {
                Row currentRow = rowIterator.next();
                //create an iterator for the row
                Iterator<Cell> cellIterator = currentRow.iterator();
                List<String> nameRow = new ArrayList<>();

                while (cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();
                    String cellText = formatter.formatCellValue(currentCell);
                    nameRow.add(cellText);
                }
                manager.ProcessStudentRoomDraw(nameRow);
            }
        }

        try {
            excelFile.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return true;
    }

    //adds a student to a xlsx workbook sheet in the specified index
    private void addStudentToSheet(Student student, Sheet sheet, int index) {
        student.setPercentile(student.getRank());

        Row row = sheet.createRow(index);
        Cell cell = row.createCell(0);
        cell.setCellValue(student.getMagicNumber());

        cell = row.createCell(1);
        cell.setCellValue(student.getName());

        cell = row.createCell(2);
        cell.setCellValue(student.getOSAPoints());

        cell = row.createCell(3);
        cell.setCellValue(student.getTotalPoints());

        cell = row.createCell(4);
        cell.setCellValue(student.getRank());

        cell = row.createCell(5);
        cell.setCellValue(student.getPercentile());
    }

}
