package ui.javafx.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private Long projectId;
    private String name;
    private Long createdBy;
    private LocalDateTime createdDate;
}
