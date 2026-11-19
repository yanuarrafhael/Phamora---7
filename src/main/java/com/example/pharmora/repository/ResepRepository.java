package com.example.pharmora.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class ResepRepository {

    public record ObatStok(String nama, int stok) {}

    private final JdbcTemplate jdbc;

    public ResepRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> daftarObat() {
        return jdbc.queryForList("SELECT IdObat, NamaObat, Stok FROM obat ORDER BY IdObat");
    }

    public String cariIdPasien(String nama) {
        List<String> hasil = jdbc.queryForList(
            "SELECT IdPasien FROM pasien WHERE NamaPasien = ? LIMIT 1", String.class, nama);
        return hasil.isEmpty() ? null : hasil.get(0);
    }

    public ObatStok cariObat(String idObat) {
        List<ObatStok> hasil = jdbc.query(
            "SELECT NamaObat, Stok FROM obat WHERE IdObat = ?",
            (rs, i) -> new ObatStok(rs.getString("NamaObat"), rs.getInt("Stok")),
            idObat);
        return hasil.isEmpty() ? null : hasil.get(0);
    }

    public String buatIdBaru() {
        Integer terakhir = jdbc.queryForObject(
            "SELECT COALESCE(MAX(CAST(SUBSTRING(IdResep, 3) AS UNSIGNED)), 0) FROM resep",
            Integer.class);
        return String.format("RS%03d", (terakhir == null ? 0 : terakhir) + 1);
    }

    public void simpanResep(String idResep, String idPasien, String idAdmin, LocalDate tgl, String status) {
        jdbc.update(
            "INSERT INTO resep (IdResep, IdPasien, IdAdmin, TglResep, Status) VALUES (?, ?, ?, ?, ?)",
            idResep, idPasien, idAdmin, java.sql.Date.valueOf(tgl), status);
    }

    public void simpanDetail(String idResep, String idObat, int jumlah) {
        jdbc.update(
            "INSERT INTO detail_resep (IdResep, IdObat, Jumlah) VALUES (?, ?, ?)",
            idResep, idObat, jumlah);
    }

    public void kurangiStok(String idObat, int jumlah) {
        jdbc.update(
            "UPDATE obat SET Stok = Stok - ?, JumlahKeluar = JumlahKeluar + ?, TanggalObatKeluar = NOW() WHERE IdObat = ?",
            jumlah, jumlah, idObat);
    }
        public List<Map<String, Object>> daftarResep() {
        return jdbc.queryForList(
            "SELECT r.IdResep, p.NamaPasien, DATE_FORMAT(r.TglResep, '%Y-%m-%d') AS TglResep, r.Status " +
            "FROM resep r JOIN pasien p ON p.IdPasien = r.IdPasien ORDER BY r.IdResep");
    }

    public int hapusResep(String idResep) {
        // kembalikan stok obat dari detail resep
        jdbc.update(
            "UPDATE obat o JOIN detail_resep d ON d.IdObat = o.IdObat " +
            "SET o.Stok = o.Stok + d.Jumlah, o.JumlahKeluar = GREATEST(o.JumlahKeluar - d.Jumlah, 0) " +
            "WHERE d.IdResep = ?", idResep);
        jdbc.update("DELETE FROM detail_resep WHERE IdResep = ?", idResep);
        return jdbc.update("DELETE FROM resep WHERE IdResep = ?", idResep);
    }
}