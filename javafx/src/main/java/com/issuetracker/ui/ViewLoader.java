package com.issuetracker.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.Objects;

//1. FXML 경로를 받음
//2. FXMLLoader로 FXML 로드
//3. 화면 루트 Parent를 꺼냄
//4. 연결된 Controller를 꺼냄
//5. root+controller를 LoadedView로 묶어서 반환

public final class ViewLoader {
    private ViewLoader(){}

    public static <T> LoadedView<T> load(String path){
        try {
            FXMLLoader loader=new FXMLLoader(Objects.requireNonNull(
                ViewLoader.class.getResource(path),
                "FXML not found: "+path));
            Parent root=loader.load();
            return new LoadedView<>(root, loader.getController());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load FXML: "+path, e);
        }
    }

    public record LoadedView<T>(Parent root, T controller){}
}
