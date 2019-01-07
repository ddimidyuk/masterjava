package ru.javaops.masterjava.persist.model;

import lombok.*;

import java.util.List;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Project extends BaseEntity {

    private @NonNull String name;
    private String description;
    private List<Group> groups;

    public Project(String name, String description, List<Group> groups) {
        this(name);
        this.description = description;
        this.groups = groups;
    }
}
