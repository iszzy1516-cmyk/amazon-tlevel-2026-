package com.amazon.tlevel.service;

import com.amazon.tlevel.model.Course;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CourseService {
    private final Map<String, Course> courses = new HashMap<>();

    public CourseService() {
        addCourse("Digital Production, Design and Development", "Digital", "London",
            "Learn software development, UX design, and digital project management with hands-on industry placements at Amazon.");
        addCourse("Health and Science", "Health", "Manchester",
            "Healthcare science fundamentals, lab techniques, and clinical skills for NHS and private sector pathways.");
        addCourse("Engineering, Manufacturing, Processing & Control", "Engineering", "Birmingham",
            "Mechanical and electrical engineering principles, CAD, and modern manufacturing processes.");
        addCourse("Business Management and Administration", "Business", "Bristol",
            "Project management, finance, marketing, and operations with real business case studies.");
        addCourse("Legal Services", "Legal", "Leeds",
            "Understanding UK law, legal research, and pathways to solicitor and paralegal careers.");
        addCourse("Agriculture, Land Management and Production", "Agriculture", "Exeter",
            "Sustainable farming, land management, and agri-tech innovations.");
        addCourse("Media, Broadcast and Production", "Media", "Glasgow",
            "Content creation, video production, editing, and broadcast technologies.");
        addCourse("Education and Early Years", "Education", "Liverpool",
            "Child development, teaching assistance, and early years curriculum planning.");
        addCourse("Construction: Design, Surveying and Planning", "Construction", "Sheffield",
            "Architectural design, surveying, BIM, and construction project planning.");
        addCourse("Science", "Science", "Cardiff",
            "Biology, chemistry, and physics laboratory skills with pharmaceutical industry links.");
    }

    private void addCourse(String name, String subject, String location, String description) {
        Course c = new Course();
        c.setCourseId(UUID.randomUUID().toString());
        c.setName(name);
        c.setSubject(subject);
        c.setLocation(location);
        c.setDescription(description);
        courses.put(c.getCourseId(), c);
    }

    public List<Course> fetchAllCourses() { return new ArrayList<>(courses.values()); }
    public Course findById(String courseId) { return courses.get(courseId); }
}
