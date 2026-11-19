package com.example.pharmora.repository;

import com.example.pharmora.model.Supplier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class SupplierRepository {

    private final JdbcTemplate jdbc;

    public SupplierRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> daftar() {
        return jdbc.queryForList(
            "SELECT IdSupplier, NamaSupplier, Alamat, NoHP, Email FROM supplier ORDER BY IdSupplier");
    }

    public boolean namaSudahAda(String nama) {
        Integer jumlah = jdbc.queryForObject(
            "SELECT COUNT(*) FROM supplier WHERE NamaSupplier = ?",
            Integer.class, nama);
        return jumlah != null && jumlah > 0;
    }

    public String buatIdBaru() {
        Integer terakhir = jdbc.queryForObject(
            "SELECT COALESCE(MAX(CAST(SUBSTRING(IdSupplier, 3) AS UNSIGNED)), 0) FROM supplier",
            Integer.class);
        return String.format("SP%03d", (terakhir == null ? 0 : terakhir) + 1);
    }

    public void simpan(String id, Supplier s) {
        jdbc.update(
            "INSERT INTO supplier (IdSupplier, NamaSupplier, Alamat, NoHP, Email) VALUES (?, ?, ?, ?, ?)",
            id, s.nama(), s.alamat(), s.hp(), s.email());
    }

    public int hapus(String id) {
        return jdbc.update("DELETE FROM supplier WHERE IdSupplier = ?", id);
    }
        public Map<String, Object> cari(String id) {
        List<Map<String, Object>> hasil = jdbc.queryForList(
            "SELECT IdSupplier, NamaSupplier, Alamat, NoHP, Email FROM supplier WHERE IdSupplier = ?", id);
        return hasil.isEmpty() ? null : hasil.get(0);
    }

    public boolean namaDipakaiLain(String nama, String id) {
        Integer jumlah = jdbc.queryForObject(
            "SELECT COUNT(*) FROM supplier WHERE NamaSupplier = ? AND IdSupplier <> ?",
            Integer.class, nama, id);
        return jumlah != null && jumlah > 0;
    }

    public int ubah(String id, Supplier s) {
        return jdbc.update(
            "UPDATE supplier SET NamaSupplier = ?, Alamat = ?, NoHP = ?, Email = ? WHERE IdSupplier = ?",
            s.nama(), s.alamat(), s.hp(), s.email(), id);
    }
}