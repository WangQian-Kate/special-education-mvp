package com.specialed.assistant.api.profile;

import com.specialed.assistant.auth.CurrentUserId;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public MyProfile getProfile(@CurrentUserId Long userId) {
        return service.getProfile(userId);
    }

    @GetMapping("/students")
    public List<StudentSummary> listStudents(@CurrentUserId Long userId) {
        return service.listStudents(userId);
    }

    @PutMapping("/current-student")
    public StudentSummary updateCurrentStudent(
            @CurrentUserId Long userId,
            @Valid @RequestBody UpdateCurrentStudentRequest request
    ) {
        return service.updateCurrentStudent(userId, request.studentId());
    }
}
