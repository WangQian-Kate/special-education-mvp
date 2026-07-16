package com.specialed.assistant.api.profile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.specialed.assistant.api.profile.ProfileModels.*;

@Validated
@RestController
@RequestMapping("/me")
public class ProfileController {
    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public MyProfile getProfile(@RequestHeader("X-User-Id") @Positive Long userId) {
        return service.getProfile(userId);
    }

    @GetMapping("/students")
    public List<StudentSummary> listStudents(@RequestHeader("X-User-Id") @Positive Long userId) {
        return service.listStudents(userId);
    }

    @PutMapping("/current-student")
    public StudentSummary updateCurrentStudent(
            @RequestHeader("X-User-Id") @Positive Long userId,
            @Valid @RequestBody UpdateCurrentStudentRequest request
    ) {
        return service.updateCurrentStudent(userId, request.studentId());
    }
}
