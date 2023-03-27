/*
 * This file is generated by jOOQ.
 */
package onlinetutoring.com.teamelevenbackend.entity.tables.daos;


import java.time.LocalDateTime;
import java.util.List;

import onlinetutoring.com.teamelevenbackend.entity.tables.Appointments;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.AppointmentsRecord;

import org.jooq.Configuration;
import org.jooq.Record3;
import org.jooq.impl.DAOImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AppointmentsDao extends DAOImpl<AppointmentsRecord, onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments, Record3<Integer, Integer, String>> {

    /**
     * Create a new AppointmentsDao without any configuration
     */
    public AppointmentsDao() {
        super(Appointments.APPOINTMENTS, onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments.class);
    }

    /**
     * Create a new AppointmentsDao with an attached configuration
     */
    public AppointmentsDao(Configuration configuration) {
        super(Appointments.APPOINTMENTS, onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments.class, configuration);
    }

    @Override
    public Record3<Integer, Integer, String> getId(onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments object) {
        return compositeKeyRecord(object.getTutorId(), object.getStudentId(), object.getSubject());
    }

    /**
     * Fetch records that have <code>tutor_id BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments> fetchRangeOfTutorId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Appointments.APPOINTMENTS.TUTOR_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>tutor_id IN (values)</code>
     */
    public List<onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments> fetchByTutorId(Integer... values) {
        return fetch(Appointments.APPOINTMENTS.TUTOR_ID, values);
    }

    /**
     * Fetch records that have <code>student_id BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments> fetchRangeOfStudentId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Appointments.APPOINTMENTS.STUDENT_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>student_id IN (values)</code>
     */
    public List<onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments> fetchByStudentId(Integer... values) {
        return fetch(Appointments.APPOINTMENTS.STUDENT_ID, values);
    }

    /**
     * Fetch records that have <code>start_time BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments> fetchRangeOfStartTime(LocalDateTime lowerInclusive, LocalDateTime upperInclusive) {
        return fetchRange(Appointments.APPOINTMENTS.START_TIME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>start_time IN (values)</code>
     */
    public List<onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments> fetchByStartTime(LocalDateTime... values) {
        return fetch(Appointments.APPOINTMENTS.START_TIME, values);
    }

    /**
     * Fetch records that have <code>end_time BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments> fetchRangeOfEndTime(LocalDateTime lowerInclusive, LocalDateTime upperInclusive) {
        return fetchRange(Appointments.APPOINTMENTS.END_TIME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>end_time IN (values)</code>
     */
    public List<onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments> fetchByEndTime(LocalDateTime... values) {
        return fetch(Appointments.APPOINTMENTS.END_TIME, values);
    }

    /**
     * Fetch records that have <code>subject BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments> fetchRangeOfSubject(String lowerInclusive, String upperInclusive) {
        return fetchRange(Appointments.APPOINTMENTS.SUBJECT, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>subject IN (values)</code>
     */
    public List<onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments> fetchBySubject(String... values) {
        return fetch(Appointments.APPOINTMENTS.SUBJECT, values);
    }
}
