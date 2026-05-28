package ui.swing;

public class Comboitem {
    private Long id;       // 내부적으로 사용할 프로젝트 ID
    private String name;   // 화면에 보여줄 프로젝트 이름

    public Comboitem(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    @Override
    public String toString() {
        return name;
    }
}
