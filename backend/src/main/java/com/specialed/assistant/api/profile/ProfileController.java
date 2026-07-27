package com.specialed.assistant.api.profile;

import com.specialed.assistant.auth.CurrentUserId;
import tools.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.specialed.assistant.api.profile.ProfileModels.*;

@RestController
@RequestMapping("/me")
public class ProfileController {
    private final ProfileService service;
    private static final ObjectMapper mapper = new ObjectMapper();

    public ProfileController(ProfileService service) { this.service = service; }

    @GetMapping
    public MyProfile getProfile(@CurrentUserId Long userId) { return service.getProfile(userId); }

    @GetMapping("/students")
    public List<StudentSummary> listStudents(@CurrentUserId Long userId) { return service.listStudents(userId); }

    @PutMapping("/current-student")
    public StudentSummary updateCurrentStudent(@CurrentUserId Long userId, @Valid @RequestBody UpdateCurrentStudentRequest r) {
        return service.updateCurrentStudent(userId, r.studentId());
    }

    @GetMapping("/students/{studentId}")
    public StudentDetail getStudent(@CurrentUserId Long userId, @PathVariable Long studentId) {
        return service.getStudentDetail(userId, studentId);
    }

    @PutMapping("/students/{studentId}")
    public StudentDetail updateStudent(@CurrentUserId Long userId, @PathVariable Long studentId, @RequestBody String raw) throws Exception {
        return service.updateStudent(userId, studentId, mapper.readValue(raw, UpdateStudentRequest.class));
    }

    @PostMapping("/students")
    public StudentSummary createStudent(@CurrentUserId Long userId, @RequestBody String raw) throws Exception {
        var node = mapper.readTree(raw);
        var r = new UpdateStudentRequest(
            text(node, "name"), genderEnum(node), intNode(node, "age"),
            text(node, "className"), text(node, "disabilityType"), text(node, "remark"),
            text(node, "socialAdaptation"), text(node, "selfManagement"), text(node, "cognitiveLevel"),
            text(node, "languageComprehension"), text(node, "expressionAbility"), text(node, "hobbies"));
        return service.createStudent(userId, r);
    }

    private String text(tools.jackson.databind.JsonNode n, String k) { var v = n.get(k); return v != null && !v.isNull() ? v.asText() : null; }
    private Gender genderEnum(tools.jackson.databind.JsonNode n) { var v = text(n, "gender"); return v != null ? Gender.valueOf(v) : null; }
    private Integer intNode(tools.jackson.databind.JsonNode n, String k) { var v = n.get(k); return v != null && !v.isNull() ? v.asInt() : null; }

    @PutMapping("/profile")
    public void updateProfile(@CurrentUserId Long userId, @RequestBody String raw) throws Exception {
        service.updateProfile(userId, mapper.readValue(raw, UpdateProfileRequest.class));
    }
}
