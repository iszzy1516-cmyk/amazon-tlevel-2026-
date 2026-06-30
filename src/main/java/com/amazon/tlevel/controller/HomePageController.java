package com.amazon.tlevel.controller;

import com.amazon.tlevel.model.Course;
import com.amazon.tlevel.service.CourseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class HomePageController {

    private final CourseService courseService;

    public HomePageController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/courses")
    public List<Course> getAllCourses() {
        return courseService.fetchAllCourses();
    }

    @GetMapping("/courses/search")
    public List<Course> searchCourses(@RequestParam(required = false) String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return getAllCourses();
        }

        String lowerQuery = searchQuery.trim().toLowerCase();

        return courseService.fetchAllCourses().stream()
                .filter(course -> course.getName().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    @GetMapping("/courses/filter")
    public List<Course> filterCourses(@RequestParam(required = false) String filterSubject,
                                       @RequestParam(required = false) String filterLocation) {
        List<Course> courses = courseService.fetchAllCourses();

        if (filterSubject != null && !filterSubject.trim().isEmpty()) {
            String lowerSubject = filterSubject.trim().toLowerCase();
            courses = courses.stream()
                    .filter(c -> c.getSubject().toLowerCase().contains(lowerSubject))
                    .collect(Collectors.toList());
        }

        if (filterLocation != null && !filterLocation.trim().isEmpty()) {
            String lowerLocation = filterLocation.trim().toLowerCase();
            courses = courses.stream()
                    .filter(c -> c.getLocation().toLowerCase().contains(lowerLocation))
                    .collect(Collectors.toList());
        }

        return courses;
    }

    public boolean isUserAuthenticated(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        return Boolean.TRUE.equals(isAuthenticated);
    }
}
