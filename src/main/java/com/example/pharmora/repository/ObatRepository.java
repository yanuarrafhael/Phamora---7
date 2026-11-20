package com.example.pharmora.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class ObatRepository {

    private static final String SELECT =
        "SELECT o.IdObat, o.NamaObat, o.IdKategori, COALESCE(k.NamaKategori, o.IdKategori) AS NamaKategori, " +
        "o.Stok, DATE_FORMAT(o.TanggalObatMasuk, '%d/%m/%Y') AS TglMasuk, " +
        "DATE_FORMAT(o.TanggalObatKeluar, '%d/%m/%Y') AS TglKeluar, " +
        "DATE_FORMAT(o.TanggalExpired, '%d/%m/%Y') AS TglExpired, " +
        "DATE_FORMAT(o.TanggalExpired, '%Y-%m-%d') AS TglExpiredIso, o.Deskripsi " +
        "FROM obat o LEFT JOIN kategori k ON k.IdKategori = o.IdKategori ";

    private final JdbcTemplate jdbc;

    public ObatRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> daftar() {
        return jdbc.queryForList(SELECT + "ORDER BY o.IdObat");
    }

    public Map<String, Object> cari(String id) {
        List<Map<String, Object>> h = jdbc.queryForList(SELECT + "WHERE o.IdObat = ?", id);
        return h.isEmpty() ? null : h.get(0);
    }

    public List<Map<String, Object>> daftarKategori() {
        return jdbc.queryForList("SELECT IdKategori, NamaKategori FROM kategori ORDER BY IdKategori");
    }

    public boolean kategoriAda(String id) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM kategori WHERE IdKategori = ?", Integer.class, id);
        return n != null && n > 0;
    }

    public boolean namaAda(String nama, String kecualiId) {
        Integer n = jdbc.queryForObject(
            "SELECT COUNT(*) FROM obat WHERE NamaObat = ? AND IdObat <> ?", Integer.class, nama, kecualiId);
        return n != null && n > 0;
    }

    public String buatIdBaru() {
        Integer t = jdbc.queryForObject(
            "SELECT COALESCE(MAX(CAST(SUBSTRING(IdObat, 3) AS UNSIGNED)), 0) FROM obat", Integer.class);
        return String.format("OB%03d", (t == null ? 0 : t) + 1);
    }

    public void simpan(String id, String nama, String kat, int stok,
                       LocalDate masuk, LocalDate keluar, LocalDate exp, String desk) {
        jdbc.update(
            "INSERT INTO obat (IdObat, IdKategori, NamaObat, Harga, Stok, TanggalExpired, Deskripsi, " +
            "TanggalObatMasuk, TanggalObatKeluar, JumlahKeluar) VALUES (?, ?, ?, 0, ?, ?, ?, ?, ?, 0)",
            id, kat, nama, stok, Date.valueOf(exp), desk,
            Timestamp.valueOf(masuk.atStartOfDay()),
            keluar == null ? null : Timestamp.valueOf(keluar.atStartOfDay()));
    }

    public int ubah(String id, String nama, String kat, int stok,
                    LocalDate masuk, LocalDate keluar, LocalDate exp, String desk) {
        return jdbc.update(
            "UPDATE obat SET NamaObat = ?, IdKategori = ?, Stok = ?, TanggalExpired = ?, Deskripsi = ?, " +
            "TanggalObatMasuk = ?, TanggalObatKeluar = ? WHERE IdObat = ?",
            nama, kat, stok, Date.valueOf(exp), desk,
            Timestamp.valueOf(masuk.atStartOfDay()),
            keluar == null ? null : Timestamp.valueOf(keluar.atStartOfDay()), id);
    }

    public int hapus(String id) {
        return jdbc.update("DELETE FROM obat WHERE IdObat = ?", id);
    }
}