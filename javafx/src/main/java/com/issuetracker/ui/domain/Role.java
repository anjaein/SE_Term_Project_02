package com.issuetracker.ui.domain;

public enum Role {
    ADMIN, PL, DEV, TESTER;

    public String label(){
        switch(this){
            case ADMIN:
                return "Admin";
            case PL:
                return "Project Lead";
            case DEV:
                return "Developer";
            case TESTER:
                return "Tester";
            default:
                return "";
        }
    }
}
