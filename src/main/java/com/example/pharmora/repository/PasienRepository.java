package com.example.pharmora.repository;

import com.example.pharmora.model.Pasien;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class PasienRepository {

    private static final String KOLOM =
        "IdPasien, NamaPasien, DATE_FORMAT(TglLahir, '%Y-%m-%d') AS TglLahir, JenisKelamin, Alamat, NoHP";

    private final JdbcTemplate jdbc;

    public PasienRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> daftar() {
        return jdbc.queryForList("SELECT " + KOLOM + " FROM pasien ORDER BY IdPasien");
    }

    public Map<String, Object> cari(String id) {
        List<Map<String, Object>> hasil =
            jdbc.queryForList("SELECT " + KOLOM + " FROM pasien WHERE IdPasien = ?", id);
        return hasil.isEmpty() ? null : hasil.get(0);
    }

    public boolean sudahAda(String nama, LocalDate tglLahir) {
        Integer jumlah = jdbc.queryForObject(
            "SELECT COUNT(*) FROM pasien WHERE NamaPasien = ? AND TglLahir = ?",
            Integer.class, nama, Date.valueOf(tglLahir));
        return jumlah != null && jumlah > 0;
    }

    public boolean sudahAdaSelain(String nama, LocalDate tglLahir, String id) {
        Integer jumlah = jdbc.queryForObject(
            "SELECT COUNT(*) FROM pasien WHERE NamaPasien = ? AND TglLahir = ? AND IdPasien <> ?",
            Integer.class, nama, Date.valueOf(tglLahir), id);
        return jumlah != null && jumlah > 0;
    }

    public String buatIdBaru() {
        Integer terakhir = jdbc.queryForObject(
            "SELECT COALESCE(MAX(CAST(SUBSTRING(IdPasien, 3) AS UNSIGNED)), 0) FROM pasien",
            Integer.class);
        return String.format("PS%03d", (terakhir == null ? 0 : terakhir) + 1);
    }

    public void simpan(String id, Pasien p, LocalDate tglLahir) {
        jdbc.update(
            "INSERT INTO pasien (IdPasien, NamaPasien, TglLahir, JenisKelamin, Alamat, NoHP) VALUES (?, ?, ?, ?, ?, ?)",
            id, p.nama(), Date.valueOf(tglLahir), p.jenisKelamin(), p.alamat(), p.hp());
    }

    public int ubah(String id, Pasien p, LocalDate tglLahir) {
        return jdbc.update(
            "UPDATE pasien SET NamaPasien = ?, TglLahir = ?, JenisKelamin = ?, Alamat = ?, NoHP = ? WHERE IdPasien = ?",
            p.nama(), Date.valueOf(tglLahir), p.jenisKelamin(), p.alamat(), p.hp(), id);
    }

    public int hapus(String id) {
        return jdbc.update("DELETE FROM pasien WHERE IdPasien = ?", id);
    }    
}