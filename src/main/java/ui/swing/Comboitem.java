package ui.swing;

import lombok.Getter;

public class Comboitem {
    @Getter
    private Long id;       // 내부적으로 사용할 프로젝트 ID
    private String name;   // 화면에 보여줄 프로젝트 이름

    public Comboitem(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
