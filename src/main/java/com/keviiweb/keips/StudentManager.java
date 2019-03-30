package com.keviiweb.keips;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class StudentManager {

	private List<Student> studentList = new LinkedList<>();
	private HashSet<String> matricHashSet = new HashSet<>();

	public static final int RESIDENT_LIST_MATRIC_INDEX = 0;
    public static final int RESIDENT_LIST_MAGICNUMBER_INDEX = 1;
    public static final int RESIDENT_LIST_NAME_INDEX = 2;
    public static final int RESIDENT_LIST_GENDER_INDEX = 3;
    public static final int RESIDENT_LIST_SEMESTER_INDEX = 4;

	public static final int EXCELSHEET_NUSNET_INDEX = 0;
	public static final int EXCELSHEET_MATRIC_INDEX = 1;
	public static final int EXCELSHEET_NAME_INDEX = 2;
	public static final int EXCELSHEET_GENDER_INDEX = 3;
	public static final int EXCELSHEET_SEMESTER_INDEX = 4;
	public static final int EXCELSHEET_CATEGORY_INDEX = 5;
	public static final int EXCELSHEET_CCANAME_INDEX = 6;
	public static final int EXCELSHEET_ATTENDANCE_INDEX = 7;
	public static final int EXCELSHEET_PERFORMANCE_INDEX = 8;
	public static final int EXCELSHEET_OUTSTANDING_INDEX = 9;

	public static final int EXCELSHEET_BONUS_INDEX = 3;
	public static final int EXCELSHEET_BONUS_DESCP_INDEX = 4;

	public static final int ROOMDRAWSHEET_MATRIC_INDEX = 1;
	public static final int ROOMDRAWSHEET_EXISTINGPOINTS_INDEX = 4;

	/*
	 *Takes in the data of a student and calculates the OSA points as well as the room draw points
	 *
	 *Returns updated student object
	 */
	public void ProcessStudent(List<String> nameRow, int sheetNumber) {
		String matric = nameRow.get(EXCELSHEET_MATRIC_INDEX);
		String name = nameRow.get(EXCELSHEET_NAME_INDEX);
		Student thisStudent = null;
		int i;
		for (i = 0; i < studentList.size(); i++) {
			if (!matric.equalsIgnoreCase(studentList.get(i).getMatric())) {
				continue;
			} else {	//once a student is found, start to process the info
				thisStudent = studentList.get(i);

				if (sheetNumber == ExcelReader.EXCELSHEET_MAIN_INDEX) {
					String ccaName = nameRow.get(EXCELSHEET_CCANAME_INDEX);
					String category = nameRow.get(EXCELSHEET_CATEGORY_INDEX);
					String attendancePts = nameRow.get(EXCELSHEET_ATTENDANCE_INDEX);
					String performancePts = nameRow.get(EXCELSHEET_PERFORMANCE_INDEX);
					String outstandingPts = nameRow.get(EXCELSHEET_OUTSTANDING_INDEX);

					processCca(thisStudent, ccaName, category, attendancePts, performancePts, outstandingPts);

					System.out.println("Parsed " + matric + ": CCA = " + ccaName + " attendance = " + attendancePts +
							"performance = " + performancePts + " outstanding = " + outstandingPts);
				}

				else if (sheetNumber == ExcelReader.EXCELSHEET_BONUS_INDEX) {
					String bonusPts = nameRow.get(EXCELSHEET_BONUS_INDEX);
					String descp = nameRow.get(EXCELSHEET_BONUS_DESCP_INDEX);

					processBonusPts(thisStudent, bonusPts, descp);

					System.out.println("Parsed " + matric + ": Bonus Pts = " + bonusPts + " description = " + descp);
				}

				else {
					System.out.println("Unknown error encountered: invalid sheet number.");
					return;
				}
				break;
			}
		}
		if (i == studentList.size()) {
			//if we cant find the student, return an error
			String s = String.format("Unable to find student: %s with matric: %s", name, matric);
			System.out.println(s);
		}
	}

	public boolean contains(String student) {
	    for (Student s : studentList) {
	        if (student.equals(s.getName())) {
	            return true;
            }
        }
        return false;
    }

	private void processBonusPts(Student stu, String bonusPts, String description) {
		int bonusPtsInteger = Integer.parseInt(bonusPts);
		BonusCCA bonusCca = new BonusCCA(bonusPtsInteger, description);
		stu.addToBonusCcaList(bonusCca);
	}

	private void processCca(Student stu, String ccaName, String category, String attendancePts,
							String performancePts, String outstandingPts) {

		int attendanceInteger = Integer.parseInt(attendancePts);
		int performanceInteger = Integer.parseInt(performancePts);
		int outstandingInteger = Integer.parseInt(outstandingPts);

		CCA newCCA = new CCA(ccaName, category, attendanceInteger, performanceInteger, outstandingInteger);

		stu.addToCcaList(newCCA);
	}

	public boolean addToStudentList(Student student) {
		//check that the resident is not already inside the list of residents
		//coz i was too lazy and my test data has duplicates
		String studentMatric = student.getMatric();
		if ((!studentMatric.equals("")) && matricHashSet.contains(studentMatric)) {
			return false;
		}
		matricHashSet.add(studentMatric);
		this.studentList.add(student);
		return true;
	}

	public void printStudentList() {
		//for (int i = 0; i < studentList.size(); i++) {
		//	System.out.println(studentList.get(i).toString());
		//}
        List<Student> sortedList = getSortedList(studentList);
        for (Student s : sortedList) {
        	s.setPercentile(s.getRank());
            System.out.println(s.toString());
        }
	}

	public List<Student> getAllStudents() {
		return this.studentList;
	}

	//converts the students list into a json object and returns it
	public String printasjson() {
		Map<String, Student> studentsMap = new HashMap<>();

		List<Student> sortedList = getSortedList(studentList);

		for (Student stu : sortedList) {
			Student clone = new Student(stu);
			clone.setPercentile(stu.getRank());
			studentsMap.put(clone.getMagicNumber(), clone);
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(studentsMap);
		return json;
	}

	public List<Student> getSortedList() {
		List<Student> clonedList = new ArrayList<>();
		for (Student stu : studentList) {
			Student clone = new Student(stu);
			clonedList.add(clone);
		}
		return getSortedList(clonedList);
	}

	private List<Student> getSortedList(List<Student> originalList) {

	    List<Student> copy = new ArrayList<>();
	    for (int i = 0; i < originalList.size(); i++) {
	        copy.add(new Student(originalList.get(i)));
        }

        Collections.sort(copy,new Comparator<Student>() {
            @Override
            public int compare(Student o1, Student o2) {
                if (o1.calculateTotalPoints() < o2.calculateTotalPoints()) {
                    return 1;
                } else if (o1.calculateTotalPoints() > o2.calculateTotalPoints()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        } );

	    //give everyone their original index
	    for (int i = 0; i < copy.size(); i++) {
            copy.get(i).setRank(i + 1);
        }
        //for those with same points as the previous one set the same rank
        for (int i = 1; i < copy.size(); i++) {
	        if (copy.get(i).calculateTotalPoints() >= copy.get(i - 1).calculateTotalPoints()) {
	            copy.get(i).setRank(copy.get(i-1).getRank());
            }
        }

	    return copy;

    }

	/*
	 * Adds previous year's room draw points to each Student
	 */
	public void ProcessStudentRoomDraw(List<String> nameRow) {
		String matric = nameRow.get(ROOMDRAWSHEET_MATRIC_INDEX);
		Student thisStudent = null;
		int i;
		for (i = 0; i < studentList.size(); i++) {
			if (!matric.equalsIgnoreCase(studentList.get(i).getMatric())) {
				continue;
			} else {	//once a student is found, start to process the info
				thisStudent = studentList.get(i);
				int existingRoomDrawPoints = Integer.parseInt(nameRow.get(ROOMDRAWSHEET_EXISTINGPOINTS_INDEX));
				thisStudent.setOldRoomDrawPoints(existingRoomDrawPoints);
				return;
			}
		}
		if (i == studentList.size()) {
			//if we cant find the student, return an error
			System.out.println("Unable to find student when adding points: " + matric);
		}
	}
}
