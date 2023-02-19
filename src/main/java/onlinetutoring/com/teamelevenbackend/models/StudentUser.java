package onlinetutoring.com.teamelevenbackend.models;

import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;

import java.util.List;

public class StudentUser extends Users {
    private List<Integer> favouriteTutorIds;
    private Integer year;

    public List<Integer> getFavouriteTutorIds() {
        return favouriteTutorIds;
    }

    public void setFavouriteTutorIds(List<Integer> favouriteTutorIds) {
        this.favouriteTutorIds = favouriteTutorIds;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
