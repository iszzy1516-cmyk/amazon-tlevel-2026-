package com.amazon.tlevel.controller;

import com.amazon.tlevel.model.Course;
import com.amazon.tlevel.service.CourseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ContentPageController {

    private final CourseService courseService;

    public ContentPageController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/course/{courseId}")
    public CourseDetailResult getCourseDetails(@PathVariable String courseId) {

        if (courseId == null || courseId.trim().isEmpty()) {
            return new CourseDetailResult(false, "ERROR: No course selected.", null);
        }

        Course course = courseService.findById(courseId.trim());

        if (course == null) {
            return new CourseDetailResult(false,
                    "ERROR: Course not found. It may have been removed.", null);
        }

        return new CourseDetailResult(true, "Course found.", course);
    }

    @GetMapping("/courses/subject/{subject}")
    public java.util.List<Course> getCoursesBySubject(@PathVariable String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return courseService.fetchAllCourses();
        }
        return courseService.fetchAllCourses().stream()
                .filter(c -> c.getSubject().equalsIgnoreCase(subject.trim()))
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/courses/location/{location}")
    public java.util.List<Course> getCoursesByLocation(@PathVariable String location) {
        if (location == null || location.trim().isEmpty()) {
            return courseService.fetchAllCourses();
        }
        return courseService.fetchAllCourses().stream()
                .filter(c -> c.getLocation().equalsIgnoreCase(location.trim()))
                .collect(java.util.stream.Collectors.toList());
    }

    public static class CourseDetailResult {
        private final boolean success;
        private final String message;
        private final Course course;

        public CourseDetailResult(boolean success, String message, Course course) {
            this.success = success;
            this.message = message;
            this.course  = course;
        }

        public boolean isSuccess() { return success; }
        public String getMessage()  { return message; }
        public Course getCourse()   { return course; }

        @Override
        public String toString() {
            return "CourseDetailResult{success=" + success +
                   ", course=" + (course != null ? course.getName() : "null") + "}";
        }
    }
}
