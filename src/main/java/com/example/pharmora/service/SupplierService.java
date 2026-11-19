package com.example.pharmora.service;

import com.example.pharmora.exception.ValidasiException;
import com.example.pharmora.model.Supplier;
import com.example.pharmora.repository.SupplierRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class SupplierService {

    private static final Pattern POLA_NAMA  = Pattern.compile("^[\\p{L}0-9 .,&\\-]+$");
    private static final Pattern POLA_ANGKA = Pattern.compile("^[0-9]+$");
    private static final Pattern POLA_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final SupplierRepository repo;

    public SupplierService(SupplierRepository repo) {
        this.repo = repo;
    }

    public List<Map<String, Object>> daftar() {
        return repo.daftar();
    }

    public Map<String, Object> ambil(String id) {
        Map<String, Object> s = repo.cari(id);
        if (s == null) {
            throw new ValidasiException(404, "Supplier tidak ditemukan");
        }
        return s;
    }

    private Supplier validasi(Supplier req) {
        String nama   = bersihkan(req.nama());
        String alamat = bersihkan(req.alamat());
        String hp     = bersihkan(req.hp());
        String email  = bersihkan(req.email());

        if (nama.isEmpty() || alamat.isEmpty() || hp.isEmpty() || email.isEmpty()) {
            throw new ValidasiException(400, "Lengkapi Data Terlebih Dahulu");
        }
        if (!POLA_NAMA.matcher(nama).matches()) {
            throw new ValidasiException(400, "Nama supplier tidak valid");
        }
        if (!POLA_ANGKA.matcher(hp).matches()) {
            throw new ValidasiException(400, "No HP hanya boleh angka");
        }
        if (hp.length() < 10 || hp.length() > 13) {
            throw new ValidasiException(400, "No HP harus 10 sampai 13 digit");
        }
        if (!POLA_EMAIL.matcher(email).matches()) {
            throw new ValidasiException(400, "Format email tidak valid");
        }
        return new Supplier(nama, alamat, hp, email);
    }

    public void simpan(Supplier req) {
        Supplier s = validasi(req);
        if (repo.namaSudahAda(s.nama())) {
            throw new ValidasiException(409, "Supplier sudah terdaftar");
        }
        repo.simpan(repo.buatIdBaru(), s);
    }

    public void ubah(String id, Supplier req) {
        Supplier s = validasi(req);
        if (repo.cari(id) == null) {
            throw new ValidasiException(404, "Supplier tidak ditemukan");
        }
        if (repo.namaDipakaiLain(s.nama(), id)) {
            throw new ValidasiException(409, "Supplier sudah terdaftar");
        }
        repo.ubah(id, s);
    }

    public void hapus(String id) {
        int terhapus;
        try {
            terhapus = repo.hapus(id);
        } catch (DataAccessException e) {
            throw new ValidasiException(409, "Supplier masih dipakai data obat");
        }
        if (terhapus == 0) {
            throw new ValidasiException(404, "Supplier tidak ditemukan");
        }
    }

    private String bersihkan(String teks) {
        return teks == null ? "" : teks.trim();
    }
}